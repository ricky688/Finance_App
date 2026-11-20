package com.example.vibefinance.wear

import android.graphics.Paint
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.Layout
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.ButtonDefaults
import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.material3.ProgressIndicatorColors
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.circularProgressIndicator
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.types.argb
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.vibefinance.wear.contract.BudgetStatus
import com.google.common.util.concurrent.ListenableFuture
import java.text.DateFormat
import java.util.Date

/**
 * Material 3 Expressive Wear OS Tile powered by ProtoLayout Material 3 (1.4.2):
 * - Native Wear OS 6 dynamic watch-face theming (allowDynamicTheme = true) for Samsung & Google watches
 * - 270-degree bezel circularProgressIndicator with semantic overspend/low colors
 * - Curved bottom textEdgeButton deep-linking straight into Pending Review or Quick Log
 * - Strict zero-money fallback timeline entry on hard expiry
 */
class BudgetTileService : TileService() {
    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        immediate(ResourceBuilders.Resources.Builder().setVersion("3").build())

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val companion = CompanionCache(this).read()
        val legacy = BudgetCache(this).read()
        val compSnap = companion.snapshot
        val legacySnap = legacy.snapshot

        val status = compSnap?.status ?: legacySnap?.status
        val isExpired = if (compSnap != null) companion.expired else legacy.expired
        val dailyAmount = compSnap?.dailyAmount ?: legacySnap?.dailyAmount.orEmpty()
        val daysLeft = compSnap?.daysLeft ?: legacySnap?.daysLeft ?: 0
        val generatedAt = compSnap?.generatedAt ?: legacySnap?.generatedAt ?: 0L
        val expiresAt = compSnap?.expiresAt ?: legacySnap?.expiresAt ?: 0L
        val dailyProgress = compSnap?.dailyProgress ?: if (dailyAmount.startsWith("-")) 0f else 0.5f
        val isOverspent = compSnap?.isOverspent ?: dailyAmount.startsWith("-")
        val pendingCount = compSnap?.pendingItems?.size ?: 0
        val canQuickAdd = compSnap?.recentEnabled == true && compSnap.quickEntryAccounts.isNotEmpty()
        val isStale = companion.stale

        val deviceConfig = requestParams.deviceConfiguration
        val fontScale = deviceConfig.fontScale.takeIf { it.isFinite() && it > 0 } ?: 1f
        val width = (deviceConfig.screenWidthDp - 64).coerceAtLeast(80) / fontScale

        fun launchClickable(id: String, route: String? = null): ModifiersBuilders.Clickable {
            val activityBuilder = ActionBuilders.AndroidActivity.Builder()
                .setPackageName(packageName)
                .setClassName(BudgetActivity::class.java.name)
            if (route != null) {
                activityBuilder.addKeyToExtraMapping(
                    EXTRA_WEAR_ROUTE,
                    ActionBuilders.AndroidStringExtra.Builder().setValue(route).build()
                )
            }
            val action = ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(activityBuilder.build())
                .build()
            return ModifiersBuilders.Clickable.Builder()
                .setId(id)
                .setOnClick(action)
                .build()
        }

        val fallbackScheme = ColorScheme(
            primary = 0xffb9f4cb.toInt().argb,
            onPrimary = 0xff00391e.toInt().argb,
            primaryContainer = 0xff14512e.toInt().argb,
            onPrimaryContainer = 0xffb9f4cb.toInt().argb
        )

        fun buildReadyOrStatusLayout(): Layout {
            val root = materialScope(
                context = this,
                deviceConfiguration = deviceConfig,
                allowDynamicTheme = true,
                defaultColorScheme = fallbackScheme
            ) {
                val indicatorColor = when {
                    isOverspent -> colorScheme.error
                    dailyProgress < 0.2f -> colorScheme.tertiary
                    else -> colorScheme.primary
                }
                val progressValue = if (isOverspent) 1f else dailyProgress.coerceIn(0f, 1f)

                val body = if (status == BudgetStatus.READY && !isExpired) {
                    val displayAmount = fittingAmount(dailyAmount, width)
                    val amountTypography = selectAmountTypography(displayAmount, width)
                    val timeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(generatedAt))
                    val subtitle = if (isStale) {
                        getString(R.string.stale_notice, timeStr)
                    } else {
                        "${getString(R.string.days_left, daysLeft)} · $timeStr"
                    }
                    val edgeRoute = when {
                        pendingCount > 0 -> ROUTE_PENDING
                        canQuickAdd -> ROUTE_QUICK_ADD
                        else -> null
                    }
                    val edgeLabel = when {
                        pendingCount > 0 -> getString(R.string.tile_edge_pending, pendingCount)
                        canQuickAdd -> getString(R.string.tile_edge_log)
                        else -> getString(R.string.tile_edge_open)
                    }

                    val tileContent = primaryLayout(
                        titleSlot = {
                            text(
                                text = getString(R.string.daily_title).layoutString,
                                typography = Typography.TITLE_SMALL,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        },
                        mainSlot = {
                            Column.Builder()
                                .setWidth(expand())
                                .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                                .addContent(
                                    text(
                                        text = displayAmount.layoutString,
                                        typography = amountTypography,
                                        color = if (isStale) colorScheme.onSurfaceVariant else indicatorColor,
                                        maxLines = 1
                                    )
                                )
                                .addContent(
                                    text(
                                        text = subtitle.layoutString,
                                        typography = Typography.BODY_EXTRA_SMALL,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                )
                                .build()
                        },
                        bottomSlot = {
                            textEdgeButton(
                                onClick = launchClickable("edge_action", edgeRoute),
                                colors = with(ButtonDefaults) {
                                    if (pendingCount > 0) filledButtonColors() else filledTonalButtonColors()
                                }
                            ) {
                                text(
                                    text = edgeLabel.layoutString,
                                    maxLines = 1
                                )
                            }
                        },
                        onClick = launchClickable("open_budget")
                    )

                    Box.Builder()
                        .setWidth(expand())
                        .setHeight(expand())
                        .addContent(
                            circularProgressIndicator(
                                staticProgress = progressValue,
                                startAngleDegrees = -135f,
                                endAngleDegrees = 135f,
                                strokeWidth = 5f,
                                colors = ProgressIndicatorColors(
                                    indicatorColor = indicatorColor,
                                    trackColor = colorScheme.surfaceContainerHigh
                                )
                            )
                        )
                        .addContent(tileContent)
                        .build()
                } else {
                    val statusMsg = getString(
                        when {
                            isExpired -> R.string.tile_expired
                            status == BudgetStatus.HIDDEN -> R.string.tile_hidden
                            status == BudgetStatus.NO_BUDGET -> R.string.tile_no_budget
                            else -> R.string.tile_open_phone
                        }
                    )
                    primaryLayout(
                        titleSlot = {
                            text(
                                text = getString(R.string.budget_title).layoutString,
                                typography = Typography.TITLE_MEDIUM,
                                color = colorScheme.primary,
                                maxLines = 1
                            )
                        },
                        mainSlot = {
                            text(
                                text = statusMsg.layoutString,
                                typography = Typography.BODY_MEDIUM,
                                color = colorScheme.onSurface,
                                maxLines = 2
                            )
                        },
                        bottomSlot = {
                            textEdgeButton(
                                onClick = launchClickable("open_budget_status"),
                                colors = with(ButtonDefaults) { filledTonalButtonColors() }
                            ) {
                                text(text = getString(R.string.tile_edge_open).layoutString, maxLines = 1)
                            }
                        },
                        onClick = launchClickable("open_budget_status_root")
                    )
                }
                body
            }
            return Layout.Builder().setRoot(root).build()
        }

        fun buildExpiredFallbackLayout(): Layout {
            val root = materialScope(
                context = this,
                deviceConfiguration = deviceConfig,
                allowDynamicTheme = true,
                defaultColorScheme = fallbackScheme
            ) {
                primaryLayout(
                    titleSlot = {
                        text(
                            text = getString(R.string.budget_title).layoutString,
                            typography = Typography.TITLE_SMALL,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    },
                    mainSlot = {
                        text(
                            text = getString(R.string.tile_expired).layoutString,
                            typography = Typography.BODY_MEDIUM,
                            color = colorScheme.onSurface,
                            maxLines = 2
                        )
                    },
                    bottomSlot = {
                        textEdgeButton(
                            onClick = launchClickable("open_budget_expired"),
                            colors = with(ButtonDefaults) { filledTonalButtonColors() }
                        ) {
                            text(text = getString(R.string.refresh).layoutString, maxLines = 1)
                        }
                    },
                    onClick = launchClickable("open_budget_expired_root")
                )
            }
            return Layout.Builder().setRoot(root).build()
        }

        val timeline = TimelineBuilders.Timeline.Builder()
        // Unconditional zero-money fallback entry guarantees no expired amount ever lingers.
        timeline.addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(buildExpiredFallbackLayout())
                .build()
        )
        if (generatedAt > 0L && expiresAt > generatedAt && !isExpired) {
            timeline.addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(buildReadyOrStatusLayout())
                    .setValidity(
                        TimelineBuilders.TimeInterval.Builder()
                            .setStartMillis(generatedAt)
                            .setEndMillis(expiresAt)
                            .build()
                    ).build()
            )
        } else if (!isExpired) {
            return immediate(
                TileBuilders.Tile.Builder()
                    .setResourcesVersion("3")
                    .setTileTimeline(
                        TimelineBuilders.Timeline.Builder().addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(buildReadyOrStatusLayout())
                                .build()
                        ).build()
                    ).build()
            )
        }
        return immediate(
            TileBuilders.Tile.Builder()
                .setResourcesVersion("3")
                .setFreshnessIntervalMillis(60_000)
                .setTileTimeline(timeline.build())
                .build()
        )
    }

    private fun selectAmountTypography(value: String, width: Float): Int {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 28f
        if (paint.measureText(value) <= width) return Typography.NUMERAL_LARGE
        paint.textSize = 22f
        if (paint.measureText(value) <= width) return Typography.NUMERAL_MEDIUM
        return Typography.NUMERAL_SMALL
    }

    private fun fittingAmount(value: String, width: Float): String {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f }
        return if (paint.measureText(value) <= width) value else getString(R.string.open_amount)
    }

    private fun <T> immediate(value: T): ListenableFuture<T> = CallbackToFutureAdapter.getFuture { completer ->
        completer.set(value)
        "BudgetTile"
    }

    companion object {
        const val EXTRA_WEAR_ROUTE = "wear_route"
        const val ROUTE_PENDING = "pending"
        const val ROUTE_QUICK_ADD = "quick_add"
    }
}
