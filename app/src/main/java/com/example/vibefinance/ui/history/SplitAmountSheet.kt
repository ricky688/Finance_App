@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.postPressIconBounce

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.theme.ChartColors
import com.example.vibefinance.theme.VibeMotion
import com.example.vibefinance.ui.components.AppButtonDefaults
import com.example.vibefinance.ui.components.AppModalBottomSheet
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressOutlinedButton
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.RollingNumberText
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.CoordinatedPreferences
import com.example.vibefinance.util.VibeHaptics
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class SplitParticipant(
    val id: String,
    val name: String,
    val photoPath: String? = null,
    val isSelf: Boolean = false
)

data class SplitBreakdown(
    val totalAmount: Double,
    val yourShare: Double,
    val owedToYou: Double,
    val sharesByParticipantId: Map<String, Double>
)

/**
 * Splits [baseAmount] + [tipPercent]% cleanly in whole cents, distributing any remaining odd cents
 * 1 cent at a time across the first seated participants so shares always sum to the exact total.
 */
fun calculateExactSplitShares(
    baseAmount: Double,
    tipPercent: Int,
    participantIds: List<String>
): SplitBreakdown {
    if (!baseAmount.isFinite() || baseAmount <= 0.0 || participantIds.isEmpty()) {
        return SplitBreakdown(0.0, 0.0, 0.0, emptyMap())
    }
    val totalCents = BigDecimal.valueOf(baseAmount)
        .multiply(BigDecimal(100 + tipPercent.coerceAtLeast(0)))
        .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        .movePointRight(2)
        .toLong()
        .coerceAtLeast(0L)
    val totalAmount = BigDecimal.valueOf(totalCents, 2).toDouble()
    val count = participantIds.size
    val baseCents = totalCents / count
    val remainder = (totalCents % count).toInt()
    val shares = linkedMapOf<String, Double>()
    var yourCents = 0L
    participantIds.forEachIndexed { index, id ->
        val personCents = baseCents + if (index < remainder) 1L else 0L
        if (index == 0) yourCents = personCents
        shares[id] = BigDecimal.valueOf(personCents, 2).toDouble()
    }
    val owedCents = (totalCents - yourCents).coerceAtLeast(0L)
    return SplitBreakdown(
        totalAmount = totalAmount,
        yourShare = BigDecimal.valueOf(yourCents, 2).toDouble(),
        owedToYou = BigDecimal.valueOf(owedCents, 2).toDouble(),
        sharesByParticipantId = shares
    )
}

private const val SPLIT_BENCH_PREFS = "split_amount_bench"
private const val SPLIT_BENCH_KEY = "participants_v1"

private fun loadSavedBench(context: Context): List<SplitParticipant> {
    val prefs = CoordinatedPreferences.get(context, SPLIT_BENCH_PREFS)
    val raw = prefs.getString(SPLIT_BENCH_KEY, "[]").orEmpty()
    return runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { i ->
            val obj = array.optJSONObject(i) ?: return@mapNotNull null
            val id = obj.optString("id")
            val name = obj.optString("name")
            if (id.isBlank() || name.isBlank()) return@mapNotNull null
            val photoPath = obj.optString("photoPath").takeIf { it.isNotBlank() && File(it).exists() }
            SplitParticipant(id = id, name = name, photoPath = photoPath, isSelf = false)
        }
    }.getOrDefault(emptyList())
}

private fun saveBench(context: Context, participants: List<SplitParticipant>) {
    val array = JSONArray()
    participants.filterNot { it.isSelf }.take(24).forEach { p ->
        array.put(JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("photoPath", p.photoPath.orEmpty())
        })
    }
    CoordinatedPreferences.get(context, SPLIT_BENCH_PREFS)
        .edit()
        .putString(SPLIT_BENCH_KEY, array.toString())
        .apply()
}

private fun resolvePickedContact(context: Context, contactUri: Uri): SplitParticipant? {
    val resolver = context.contentResolver
    var contactId: String? = null
    var displayName: String? = null
    resolver.query(
        contactUri,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            if (idIdx >= 0) contactId = cursor.getString(idIdx)
            if (nameIdx >= 0) displayName = cursor.getString(nameIdx)
        }
    }
    val safeName = displayName?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val stableId = "contact:${contactId ?: safeName}"
    val dir = File(context.filesDir, "split_contacts").apply { if (!exists()) mkdirs() }
    val outFile = File(dir, "avatar_${stableId.hashCode()}.png")
    var savedPhotoPath: String? = outFile.takeIf { it.exists() }?.absolutePath
    runCatching {
        val photoStream = ContactsContract.Contacts.openContactPhotoInputStream(resolver, contactUri, true)
            ?: ContactsContract.Contacts.openContactPhotoInputStream(resolver, contactUri, false)
        photoStream?.use { stream ->
            val bmp = BitmapFactory.decodeStream(stream)
            if (bmp != null) {
                FileOutputStream(outFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
                bmp.recycle()
                if (outFile.exists() && outFile.length() > 0L) {
                    savedPhotoPath = outFile.absolutePath
                }
            }
        }
    }
    return SplitParticipant(id = stableId, name = safeName, photoPath = savedPhotoPath, isSelf = false)
}

@Composable
private fun ContactAvatarBadge(
    participant: SplitParticipant,
    accentColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(participant.photoPath) {
        participant.photoPath?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
            }.getOrNull()
        }
    }
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = if (participant.isSelf) MaterialTheme.colorScheme.primary else accentColor.copy(alpha = 0.20f),
        border = BorderStroke(
            width = 2.dp,
            color = if (participant.isSelf) MaterialTheme.colorScheme.primary else accentColor
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = participant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                val initial = participant.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = (size.value * 0.42f).sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (participant.isSelf) MaterialTheme.colorScheme.onPrimary else accentColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SplitAmountSheet(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onRecordReimbursement: ((Double, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = MaterialTheme.colorScheme.surface.let {
        (it.red * 0.299f + it.green * 0.587f + it.blue * 0.114f) < 0.5f
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selfName = stringResource(R.string.split_bill_you)
    val selfParticipant = remember(selfName) {
        SplitParticipant(id = "self", name = selfName, isSelf = true)
    }

    val benchParticipants = remember {
        mutableStateListOf<SplitParticipant>().apply {
            addAll(loadSavedBench(context))
        }
    }
    val seatedIds = remember(transaction.id) {
        mutableStateListOf("self")
    }
    var tipPercent by remember(transaction.id) { mutableIntStateOf(0) }
    var showAddNameDialog by remember { mutableStateOf(false) }
    var manualNameInput by remember { mutableStateOf("") }
    var reimbursementRecorded by remember(transaction.id) { mutableStateOf(false) }

    val contactErrorMsg = stringResource(R.string.split_bill_pick_contact_error)
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val picked = resolvePickedContact(context, uri)
            if (picked != null) {
                VibeHaptics.successConfirm(view)
                val existingIdx = benchParticipants.indexOfFirst { it.id == picked.id }
                if (existingIdx >= 0) {
                    benchParticipants[existingIdx] = picked
                } else {
                    benchParticipants.add(0, picked)
                }
                saveBench(context, benchParticipants)
                if (picked.id !in seatedIds) {
                    seatedIds.add(picked.id)
                }
            } else {
                Toast.makeText(context, contactErrorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val allParticipantsById = remember(selfParticipant, benchParticipants.toList()) {
        buildMap {
            put(selfParticipant.id, selfParticipant)
            benchParticipants.forEach { put(it.id, it) }
        }
    }
    val seatedParticipants = seatedIds.mapNotNull { allParticipantsById[it] }
    val baseAmount = abs(transaction.amount)
    val breakdown = remember(baseAmount, tipPercent, seatedIds.toList()) {
        calculateExactSplitShares(baseAmount, tipPercent, seatedParticipants.map { it.id })
    }

    val slicePalette = remember(isDark) {
        ChartColors.getFallbackPalette(isDark)
    }
    val primaryColor = MaterialTheme.colorScheme.primary

    fun sliceColorFor(index: Int, isSelf: Boolean): Color =
        if (isSelf) primaryColor else slicePalette[(index - 1).mod(slicePalette.size)]

    if (showAddNameDialog) {
        AlertDialog(
            onDismissRequest = { showAddNameDialog = false },
            title = { Text(stringResource(R.string.split_bill_add_person)) },
            text = {
                OutlinedTextField(
                    value = manualNameInput,
                    onValueChange = { manualNameInput = it },
                    placeholder = { Text(stringResource(R.string.split_bill_add_person_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                CompletePressButton(
                    onClick = {
                        val trimmed = manualNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            val person = SplitParticipant(
                                id = "manual:${UUID.randomUUID()}",
                                name = trimmed
                            )
                            benchParticipants.add(0, person)
                            saveBench(context, benchParticipants)
                            seatedIds.add(person.id)
                            manualNameInput = ""
                            showAddNameDialog = false
                        }
                    },
                    shapes = AppButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                CompletePressTextButton(
                    onClick = { showAddNameDialog = false },
                    shapes = AppButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("SplitAmountSheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with bill name and total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.split_bill_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = transaction.description.ifBlank {
                            com.example.vibefinance.ui.home.getCategoryDisplayName(transaction.category)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.split_bill_total),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RollingNumberText(
                        text = String.format(Locale.US, "HK$%,.2f", breakdown.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Remold UI Circular Table with animated arc slices & spring-positioned avatars
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val tableDiameter = minOf(maxWidth - 96.dp, 168.dp).coerceAtLeast(132.dp)
                val avatarSize = 44.dp
                val orbitRadiusDp = tableDiameter / 2f + 34.dp
                val count = seatedParticipants.size.coerceAtLeast(1)
                val gapDeg = if (count > 1) 4f else 0f
                val sliceSweepDeg by animateFloatAsState(
                    targetValue = (360f / count) - gapDeg,
                    animationSpec = VibeMotion.defaultSpatial(),
                    label = "tableSliceSweep"
                )

                // Table ring slices
                Canvas(
                    modifier = Modifier.size(tableDiameter)
                ) {
                    val strokeWidth = 18.dp.toPx()
                    val inset = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    seatedParticipants.forEachIndexed { idx, person ->
                        val stepDeg = 360f / count
                        val startDeg = -90f + idx * stepDeg + (gapDeg / 2f)
                        drawArc(
                            color = sliceColorFor(idx, person.isSelf),
                            startAngle = startDeg,
                            sweepAngle = sliceSweepDeg.coerceAtLeast(4f),
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Odometer Share Center Callout inside Table Ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.split_bill_your_share_of, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RollingNumberText(
                        text = String.format(Locale.US, "HK$%,.2f", breakdown.yourShare),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    if (count > 1) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${stringResource(R.string.split_bill_to_collect)} HK$${String.format(Locale.US, "%,.2f", breakdown.owedToYou)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Seated Participants orbiting the circular table
                val orbitPx = with(density) { orbitRadiusDp.toPx() }
                seatedParticipants.forEachIndexed { idx, person ->
                    val targetAngleDeg = -90f + idx * (360f / count)
                    val animatedAngleDeg by animateFloatAsState(
                        targetValue = targetAngleDeg,
                        animationSpec = VibeMotion.defaultSpatial(),
                        label = "seatAngle_${person.id}"
                    )
                    val rad = animatedAngleDeg * PI / 180.0
                    val xOff = (orbitPx * cos(rad)).roundToInt()
                    val yOff = (orbitPx * sin(rad)).roundToInt()
                    val accentColor = sliceColorFor(idx, person.isSelf)
                    val personShare = breakdown.sharesByParticipantId[person.id] ?: 0.0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset { IntOffset(xOff, yOff) }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !person.isSelf) {
                                VibeHaptics.selection(view)
                                seatedIds.remove(person.id)
                            }
                            .padding(2.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            ContactAvatarBadge(
                                participant = person,
                                accentColor = accentColor,
                                size = avatarSize
                            )
                            if (!person.isSelf) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.split_bill_tap_to_remove),
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
                        ) {
                            Text(
                                text = "${person.name.take(8)} · $${String.format(Locale.US, "%.2f", personShare)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Tip / Service Charge Toggle Pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.split_bill_tip),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 10, 15, 20).forEach { pct ->
                        val selected = tipPercent == pct
                        FilterChip(
                            selected = selected,
                            onClick = {
                                VibeHaptics.lightTick(view)
                                tipPercent = pct
                            },
                            label = {
                                Text(
                                    text = if (pct == 0) stringResource(R.string.split_bill_tip_none) else "+$pct%",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bench Contacts + Native Selective Contact Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.split_bill_bench_hint),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CompletePressFilledTonalButton(
                            onClick = {
                                VibeHaptics.selection(view)
                                contactPickerLauncher.launch(null)
                            },
                            shapes = AppButtonDefaults.shapes(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("SplitPickContactButton")
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp).postPressIconBounce())
                            Spacer(Modifier.width(5.dp))
                            Text(stringResource(R.string.split_bill_pick_contact), style = MaterialTheme.typography.labelMedium)
                        }
                        CompletePressOutlinedButton(
                            onClick = {
                                VibeHaptics.selection(view)
                                showAddNameDialog = true
                            },
                            shapes = AppButtonDefaults.shapes(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("SplitAddPersonButton")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp).postPressIconBounce())
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.split_bill_add_person), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (benchParticipants.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        benchParticipants.forEachIndexed { idx, person ->
                            val isSeated = person.id in seatedIds
                            val badgeColor = slicePalette[idx.mod(slicePalette.size)]
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSeated) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSeated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        VibeHaptics.selection(view)
                                        if (isSeated) seatedIds.remove(person.id) else seatedIds.add(person.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ContactAvatarBadge(
                                        participant = person,
                                        accentColor = badgeColor,
                                        size = 32.dp
                                    )
                                    Text(
                                        text = person.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSeated) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    AnimatedContent(
                                        targetState = isSeated,
                                        transitionSpec = {
                                            (fadeIn(VibeMotion.fastEffects()) + scaleIn(VibeMotion.fastSpatial(), initialScale = 0.7f))
                                                .togetherWith(fadeOut(VibeMotion.fastEffects()) + scaleOut(VibeMotion.fastSpatial(), targetScale = 0.7f))
                                        },
                                        label = "benchState_${person.id}"
                                    ) { seated ->
                                        Icon(
                                            imageVector = if (seated) Icons.Default.Check else Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (seated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary + Actions
            val seatedCount = seatedParticipants.size.coerceAtLeast(1)
            val othersCount = (seatedCount - 1).coerceAtLeast(0)
            if (othersCount > 0) {
                val perOtherFormatted = String.format(Locale.US, "HK$%,.2f", breakdown.yourShare)
                val owedFormatted = String.format(Locale.US, "HK$%,.2f", breakdown.owedToYou)
                val totalFormatted = String.format(Locale.US, "HK$%,.2f", breakdown.totalAmount)
                val billLabel = transaction.description.ifBlank {
                    com.example.vibefinance.ui.home.getCategoryDisplayName(transaction.category)
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompletePressButton(
                        onClick = {
                            VibeHaptics.successConfirm(view)
                            val shareMessage = context.getString(
                                R.string.split_bill_share_message,
                                billLabel,
                                totalFormatted,
                                seatedCount,
                                perOtherFormatted,
                                owedFormatted
                            )
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.split_bill_title)))
                        },
                        shapes = AppButtonDefaults.largeShapes(),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp).postPressIconBounce())
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.split_bill_request_btn, owedFormatted, othersCount),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onRecordReimbursement != null && !reimbursementRecorded) {
                        CompletePressFilledTonalButton(
                            onClick = {
                                VibeHaptics.successConfirm(view)
                                reimbursementRecorded = true
                                val names = seatedParticipants.drop(1).joinToString(", ") { it.name }
                                val desc = "$billLabel (${context.getString(R.string.split_bill_action)}: $names)"
                                onRecordReimbursement(breakdown.owedToYou, desc)
                                Toast.makeText(context, context.getString(R.string.split_bill_recorded), Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            shapes = AppButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("SplitRecordReimbursementButton")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp).postPressIconBounce())
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.split_bill_record_reimbursement, owedFormatted),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
