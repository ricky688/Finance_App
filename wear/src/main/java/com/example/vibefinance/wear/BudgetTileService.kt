package com.example.vibefinance.wear

import android.graphics.Paint
import androidx.wear.protolayout.*
import androidx.wear.protolayout.DimensionBuilders.*
import androidx.wear.protolayout.LayoutElementBuilders.*
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.vibefinance.wear.contract.BudgetStatus
import androidx.concurrent.futures.CallbackToFutureAdapter
import com.google.common.util.concurrent.ListenableFuture
import java.text.DateFormat
import java.util.Date

class BudgetTileService : TileService() {
    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        immediate(ResourceBuilders.Resources.Builder().setVersion("1").build())

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val cached = BudgetCache(this).read()
        val snapshot = cached.snapshot
        val fontScale = requestParams.deviceConfiguration.fontScale.takeIf { it.isFinite() && it > 0 } ?: 1f
        val width = (requestParams.deviceConfiguration.screenWidthDp - 48).coerceAtLeast(80) / fontScale
        val column = Column.Builder().setWidth(expand()).setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
        fun text(value: String, size: Float, primary: Boolean = false, maxLines: Int = 1) {
            column.addContent(Text.Builder().setText(value).setMaxLines(maxLines)
                .setFontStyle(FontStyle.Builder().setSize(sp(size))
                    .setColor(ColorBuilders.argb(if (primary) 0xffb9f4cb.toInt() else 0xffe1e3df.toInt())).build()).build())
        }
        if (snapshot?.status == BudgetStatus.READY) {
            text(getString(R.string.daily_title), 12f)
            text(fittingAmount(snapshot.dailyAmount, width), amountSize(snapshot.dailyAmount, width), true)
            text(getString(R.string.days_left, snapshot.daysLeft), 14f)
            text(getString(R.string.updated, DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(snapshot.generatedAt))), 10f)
        } else {
            text(getString(R.string.budget_title), 20f, true)
            // Tile status stays brief; the app supplies the full explanation and refresh action.
            text(getString(when {
                cached.expired -> R.string.tile_expired
                snapshot?.status == BudgetStatus.HIDDEN -> R.string.tile_hidden
                snapshot?.status == BudgetStatus.NO_BUDGET -> R.string.tile_no_budget
                else -> R.string.tile_open_phone
            }), 14f, maxLines = 2)
        }
        val launch = ActionBuilders.LaunchAction.Builder().setAndroidActivity(
            ActionBuilders.AndroidActivity.Builder().setPackageName(packageName)
                .setClassName(BudgetActivity::class.java.name).build()).build()
        fun layout(content: LayoutElement): Layout = Layout.Builder().setRoot(
            Box.Builder().setWidth(expand()).setHeight(expand())
                .setModifiers(ModifiersBuilders.Modifiers.Builder()
                    .setBackground(ModifiersBuilders.Background.Builder().setColor(ColorBuilders.argb(0xff101418.toInt())).build())
                    .setClickable(ModifiersBuilders.Clickable.Builder().setId("open_budget").setOnClick(launch).build())
                    .setSemantics(ModifiersBuilders.Semantics.Builder().setContentDescription(getString(R.string.budget_title)).build())
                    .setPadding(ModifiersBuilders.Padding.Builder().setAll(dp(24f)).build()).build())
                .addContent(content).build()).build()
        val timeline = TimelineBuilders.Timeline.Builder()
        // An unconditional fallback contains no money, even when no update callback runs at expiry.
        timeline.addTimelineEntry(TimelineBuilders.TimelineEntry.Builder().setLayout(layout(
            Text.Builder().setText(getString(R.string.tile_expired))
                .setFontStyle(FontStyle.Builder().setSize(sp(14f)).setColor(ColorBuilders.argb(0xffe1e3df.toInt())).build()).build())).build())
        if (snapshot != null) {
            timeline.addTimelineEntry(TimelineBuilders.TimelineEntry.Builder().setLayout(layout(column.build()))
                .setValidity(TimelineBuilders.TimeInterval.Builder().setStartMillis(snapshot.generatedAt)
                    .setEndMillis(snapshot.expiresAt).build()).build())
        } else if (!cached.expired) {
            // No sensitive data: first-use guidance may remain until a snapshot arrives.
            return immediate(TileBuilders.Tile.Builder().setResourcesVersion("1")
                .setTileTimeline(TimelineBuilders.Timeline.Builder().addTimelineEntry(
                    TimelineBuilders.TimelineEntry.Builder().setLayout(layout(column.build())).build()).build()).build())
        }
        return immediate(TileBuilders.Tile.Builder().setResourcesVersion("1")
            .setFreshnessIntervalMillis(60_000).setTileTimeline(timeline.build()).build())
    }

    private fun amountSize(value: String, width: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var size = 32f
        while (size > 14f) {
            paint.textSize = size
            if (paint.measureText(value) <= width) return size
            size -= 1f
        }
        return 14f
    }

    private fun <T> immediate(value: T): ListenableFuture<T> = CallbackToFutureAdapter.getFuture { completer ->
        completer.set(value)
        "BudgetTile"
    }

    private fun fittingAmount(value: String, width: Float): String {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = amountSize(value, width) }
        return if (paint.measureText(value) <= width) value else getString(R.string.open_amount)
    }
}
