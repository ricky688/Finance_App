package com.example.vibefinance.wear

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.toArgb
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.vibefinance.wear.contract.BudgetStatus
import java.time.Instant
import kotlin.math.roundToInt

internal data class ComplicationDisplayState(
    val shortText: String,
    val shortTitle: String,
    val longText: String,
    val rangedValue: Float,
    val percent: Int,
    val isOverspent: Boolean,
    val hasMoney: Boolean,
    val validFrom: Long = 0L,
    val validUntil: Long = 0L
)

/**
 * Watch Face Complication data source so VibeFinance slots natively into OEM official watch faces
 * (Samsung Galaxy Watch One UI Watch, Google Pixel Watch, etc.) for both circle and long/bar slots.
 *
 * Dynamic color behavior:
 * - Uses MonochromaticImage so the host watch face tints the icon with its active dynamic color.
 * - Omits ColorRamp in healthy state so Samsung/Google watch faces paint progress arcs/bars in
 *   their exact watch-face dynamic theme color.
 * - Uses the system dynamic error color ramp only when overspent so over-budget stays unmistakable.
 */
class BudgetComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val preview = ComplicationDisplayState(
            shortText = "$320",
            shortTitle = getString(R.string.complication_today),
            longText = "HK$ 320 · 72% · ${getString(R.string.days_left, 5)}",
            rangedValue = 0.72f,
            percent = 72,
            isOverspent = false,
            hasMoney = true
        )
        return buildComplication(type, preview, tapAction = null)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val companion = CompanionCache(this).read()
        val legacy = BudgetCache(this).read()
        val display = resolveComplicationDisplay(
            companion = companion,
            legacy = legacy,
            todayLabel = getString(R.string.complication_today),
            hiddenLabel = getString(R.string.complication_hidden),
            syncLabel = getString(R.string.complication_expired),
            daysShort = { d -> getString(R.string.days_left_short, d) },
            daysLong = { d -> getString(R.string.days_left, d) }
        )
        val launchIntent = Intent(this, BudgetActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapAction = PendingIntent.getActivity(
            this,
            request.complicationInstanceId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return buildComplication(request.complicationType, display, tapAction)
    }

    private fun buildComplication(
        type: ComplicationType,
        state: ComplicationDisplayState,
        tapAction: PendingIntent?
    ): ComplicationData? {
        val contentDesc = PlainComplicationText.Builder(getString(R.string.complication_desc)).build()
        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(this, R.drawable.ic_launcher_monochrome)
        ).build()
        val validRange = if (state.hasMoney && state.validFrom > 0L && state.validUntil > state.validFrom) {
            TimeRange.between(Instant.ofEpochMilli(state.validFrom), Instant.ofEpochMilli(state.validUntil))
        } else null

        // Leave ColorRamp null when healthy so Samsung & Google OEM watch faces apply their own
        // dynamic theme color; apply a harmonized dynamic error color ramp only when overspent.
        val overspentRamp = if (state.isOverspent) {
            val errorArgb = runCatching { dynamicColorScheme(this)?.error?.toArgb() }.getOrNull()
                ?: FALLBACK_ERROR_COLOR
            ColorRamp(intArrayOf(errorArgb), interpolated = false)
        } else null

        val percentValue = (state.rangedValue.coerceIn(0f, 1f) * 100f)

        return when (type) {
            ComplicationType.GOAL_PROGRESS -> GoalProgressComplicationData.Builder(
                value = percentValue,
                targetValue = 100f,
                contentDescription = contentDesc
            )
                .setText(PlainComplicationText.Builder(state.shortText).build())
                .setTitle(PlainComplicationText.Builder(state.shortTitle).build())
                .setMonochromaticImage(icon)
                .setTapAction(tapAction)
                .apply {
                    if (overspentRamp != null) setColorRamp(overspentRamp)
                    if (validRange != null) setValidTimeRange(validRange)
                }
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = percentValue,
                min = 0f,
                max = 100f,
                contentDescription = contentDesc
            )
                .setValueType(RangedValueComplicationData.TYPE_PERCENTAGE)
                .setText(PlainComplicationText.Builder(state.shortText).build())
                .setTitle(PlainComplicationText.Builder(state.shortTitle).build())
                .setMonochromaticImage(icon)
                .setTapAction(tapAction)
                .apply {
                    if (overspentRamp != null) setColorRamp(overspentRamp)
                    if (validRange != null) setValidTimeRange(validRange)
                }
                .build()

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(state.shortText).build(),
                contentDescription = contentDesc
            )
                .setTitle(PlainComplicationText.Builder(state.shortTitle).build())
                .setMonochromaticImage(icon)
                .setTapAction(tapAction)
                .apply { if (validRange != null) setValidTimeRange(validRange) }
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(state.longText).build(),
                contentDescription = contentDesc
            )
                .setTitle(PlainComplicationText.Builder(getString(R.string.budget_title)).build())
                .setMonochromaticImage(icon)
                .setTapAction(tapAction)
                .apply { if (validRange != null) setValidTimeRange(validRange) }
                .build()

            else -> null
        }
    }

    companion object {
        private const val FALLBACK_ERROR_COLOR = 0xffffb4ab.toInt()

        fun requestUpdate(context: Context) {
            runCatching {
                val component = ComponentName(context, BudgetComplicationService::class.java)
                ComplicationDataSourceUpdateRequester.create(context.applicationContext, component)
                    .requestUpdateAll()
            }
        }

        internal fun resolveComplicationDisplay(
            companion: CachedCompanion,
            legacy: CachedBudget,
            todayLabel: String,
            hiddenLabel: String,
            syncLabel: String,
            daysShort: (Int) -> String,
            daysLong: (Int) -> String
        ): ComplicationDisplayState {
            val comp = companion.snapshot
            if (comp != null && !companion.expired) {
                return when (comp.status) {
                    BudgetStatus.READY -> {
                        val compact = comp.compactDailyAmount.ifBlank { comp.dailyAmount.replace("HK", "") }
                        val shortVal = if (compact.length <= 7) compact else daysShort(comp.daysLeft)
                        val title = if (compact.length <= 7) daysShort(comp.daysLeft) else todayLabel
                        val clamped = comp.dailyProgress.coerceIn(0f, 1f)
                        val pct = (clamped * 100f).roundToInt()
                        val longDisplay = if (comp.isOverspent) {
                            "${comp.dailyAmount} · ${daysLong(comp.daysLeft)}"
                        } else {
                            "${comp.dailyAmount} · ${pct}% · ${daysLong(comp.daysLeft)}"
                        }
                        ComplicationDisplayState(
                            shortText = shortVal,
                            shortTitle = title,
                            longText = longDisplay,
                            rangedValue = clamped,
                            percent = pct,
                            isOverspent = comp.isOverspent,
                            hasMoney = true,
                            validFrom = comp.generatedAt,
                            validUntil = comp.expiresAt
                        )
                    }
                    BudgetStatus.HIDDEN -> ComplicationDisplayState(
                        shortText = "--",
                        shortTitle = hiddenLabel,
                        longText = hiddenLabel,
                        rangedValue = 0f,
                        percent = 0,
                        isOverspent = false,
                        hasMoney = false
                    )
                    else -> ComplicationDisplayState(
                        shortText = "--",
                        shortTitle = todayLabel,
                        longText = syncLabel,
                        rangedValue = 0f,
                        percent = 0,
                        isOverspent = false,
                        hasMoney = false
                    )
                }
            }
            val snap = legacy.snapshot
            if (snap != null && !legacy.expired && snap.status == BudgetStatus.READY) {
                val compact = snap.dailyAmount.replace("HK", "").replace(" ", "")
                val shortVal = if (compact.length <= 7) compact else daysShort(snap.daysLeft)
                val overspent = snap.dailyAmount.startsWith("-")
                val progress = if (overspent) 0f else 0.5f
                return ComplicationDisplayState(
                    shortText = shortVal,
                    shortTitle = daysShort(snap.daysLeft),
                    longText = "${snap.dailyAmount} · ${daysLong(snap.daysLeft)}",
                    rangedValue = progress,
                    percent = (progress * 100f).roundToInt(),
                    isOverspent = overspent,
                    hasMoney = true,
                    validFrom = snap.generatedAt,
                    validUntil = snap.expiresAt
                )
            }
            val isHidden = comp?.status == BudgetStatus.HIDDEN || snap?.status == BudgetStatus.HIDDEN
            return ComplicationDisplayState(
                shortText = "--",
                shortTitle = if (isHidden) hiddenLabel else syncLabel,
                longText = if (isHidden) hiddenLabel else syncLabel,
                rangedValue = 0f,
                percent = 0,
                isOverspent = false,
                hasMoney = false
            )
        }
    }
}
