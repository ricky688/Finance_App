package com.example.vibefinance.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.core.content.FileProvider
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/** User-initiated calendar snapshots. Never reads or writes a calendar provider directly. */
object CalendarSyncHelper {
    private val localTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss", Locale.US)
    private val utcTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US).withZone(ZoneOffset.UTC)
    fun monthlyRule(day: Int): String = if (day <= 28) "FREQ=MONTHLY;BYMONTHDAY=$day"
        else "FREQ=MONTHLY;BYMONTHDAY=$day,-1;BYSETPOS=1"
    internal fun escape(value: String): String = value.replace("\\", "\\\\").replace("\r\n", "\n").replace("\r", "\n")
        .replace("\n", "\\n").replace(";", "\\;").replace(",", "\\,")
    internal fun fold(value: String): String {
        val out = StringBuilder()
        var bytes = 0
        var index = 0
        while (index < value.length) {
            val cp = value.codePointAt(index)
            val character = String(Character.toChars(cp))
            val count = character.toByteArray(Charsets.UTF_8).size
            if (bytes + count > 75) { out.append("\r\n "); bytes = 1 }
            out.append(character); bytes += count; index += Character.charCount(cp)
        }
        return out.toString()
    }
    fun generateIcsContent(context: Context, accounts: List<AccountEntity>, subscriptions: List<SubscriptionEntity>,
        clock: Clock = Clock.systemDefaultZone(), zone: ZoneId = clock.zone): String {
        val calculator = CreditCardCycleCalculator(clock, zone)
        val today = LocalDate.now(clock.withZone(zone))
        val lines = mutableListOf("BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//VibeFinance//Reminders//EN", "CALSCALE:GREGORIAN",
            "X-WR-CALNAME:${escape(context.appString(R.string.calendar_name))}")
        fun event(uid: String, date: LocalDate, title: String, description: String, recurrence: List<String>, alarms: Boolean) {
            val start = date.atTime(9, 0)
            lines += listOf("BEGIN:VEVENT", "UID:$uid@vibefinance", "DTSTAMP:${utcTime.format(clock.instant())}",
                "DTSTART:${localTime.format(start)}", "DTEND:${localTime.format(start.plusMinutes(30))}",
                "SUMMARY:${escape(title)}", "DESCRIPTION:${escape(description)}")
            lines += recurrence
            if (alarms) listOf("-P2D", "PT0S").forEach { trigger ->
                lines += listOf("BEGIN:VALARM", "ACTION:DISPLAY", "TRIGGER:$trigger", "DESCRIPTION:${escape(title)}", "END:VALARM")
            }
            lines += "END:VEVENT"
        }
        accounts.filter { it.type == AccountType.CC }.forEach { card ->
            val day = calculator.paymentDay(card) ?: return@forEach
            val date = calculator.nextDue(card) ?: return@forEach
            val title = context.appString(R.string.calendar_card_title, card.nickname?.takeIf { it.isNotBlank() } ?: card.name)
            val description = card.billingDate?.takeIf { it in 1..31 }?.let { context.appString(R.string.calendar_card_description, it) }
                ?: context.appString(R.string.calendar_card_description_unset)
            event("card-${card.id}", date, title, description, listOf("RRULE:${monthlyRule(day)}"), true)
        }
        subscriptions.forEach { sub ->
            var next = Instant.ofEpochMilli(sub.nextPaymentDate).atZone(zone).toLocalDate()
            val weekly = sub.frequency.equals("Weekly", ignoreCase = true)
            // Advance exactly as the app does; no persisted monthly anchor or schedule mutation.
            val monthEnd = !weekly && next.dayOfMonth > 28
            fun advance(date: LocalDate) = if (weekly) date.plusWeeks(1) else date.plusMonths(1)
            if (next < today) {
                // Jump near today before stepping; preserve monthly clamping over the skipped interval.
                if (weekly) next = next.plusWeeks((java.time.temporal.ChronoUnit.DAYS.between(next, today) / 7).coerceAtLeast(0))
                else {
                    val months = java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.from(next), YearMonth.from(today))
                    if (months > 24) {
                        repeat(24) { next = advance(next) }
                        next = next.plusMonths(months - 24)
                    }
                }
                while (next < today) next = advance(next)
            }
            if (monthEnd && next >= today.plusMonths(12)) return@forEach
            val recurrence = if (monthEnd) {
                val dates = mutableListOf<LocalDate>()
                var occurrence = advance(next)
                while (occurrence < today.plusMonths(12)) { dates += occurrence; occurrence = advance(occurrence) }
                if (dates.isEmpty()) emptyList() else listOf("RDATE:" + dates.joinToString(",") { localTime.format(it.atTime(9, 0)) })
            } else listOf("RRULE:FREQ=${if (weekly) "WEEKLY" else "MONTHLY"}")
            event("subscription-${sub.id}", next, context.appString(R.string.calendar_subscription_title, sub.name),
                context.appString(R.string.calendar_subscription_description, sub.amount), recurrence, false)
        }
        lines += "END:VCALENDAR"
        return lines.joinToString("\r\n", postfix = "\r\n") { fold(it) }
    }
    suspend fun exportAndShareIcs(context: Context, accounts: List<AccountEntity>, subscriptions: List<SubscriptionEntity>) {
        val uri = withContext(Dispatchers.IO) {
            require(accounts.any { it.type == AccountType.CC && CreditCardCycleCalculator().paymentDay(it) != null } || subscriptions.isNotEmpty())
            val file = File(context.cacheDir, "vibe_finance_calendar.ics")
            try { file.writeText(generateIcsContent(context, accounts, subscriptions), Charsets.UTF_8) }
            catch (e: Exception) { file.delete(); throw e }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        withContext(Dispatchers.Main) {
            val intent = Intent(Intent.ACTION_SEND).setType("text/calendar").putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply { clipData = ClipData.newRawUri("calendar", uri) }
            context.startActivity(Intent.createChooser(intent, context.appString(R.string.calendar_export)))
        }
    }
    fun cardDeadlineIntent(context: Context, account: AccountEntity, date: LocalDate = requireNotNull(CreditCardCycleCalculator().nextDue(account)), zone: ZoneId = ZoneId.systemDefault()): Intent {
        val day = requireNotNull(CreditCardCycleCalculator().paymentDay(account))
        val title = context.appString(R.string.calendar_card_title, account.nickname?.takeIf { it.isNotBlank() } ?: account.name)
        val description = account.billingDate?.takeIf { it in 1..31 }?.let { context.appString(R.string.calendar_card_description, it) }
            ?: context.appString(R.string.calendar_card_description_unset)
        return Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, title).putExtra(CalendarContract.Events.DESCRIPTION, description)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.atTime(9, 0).atZone(zone).toInstant().toEpochMilli())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.atTime(9, 30).atZone(zone).toInstant().toEpochMilli())
            .putExtra(CalendarContract.Events.RRULE, monthlyRule(day))
    }
    fun addCardDeadlineToSystemCalendar(context: Context, account: AccountEntity, date: LocalDate = requireNotNull(CreditCardCycleCalculator().nextDue(account))) {
        try { context.startActivity(cardDeadlineIntent(context, account, date)) }
        catch (_: android.content.ActivityNotFoundException) { android.widget.Toast.makeText(context, context.appString(R.string.calendar_unavailable), android.widget.Toast.LENGTH_LONG).show() }
    }
}
