@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.components.AppButtonDefaults
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressOutlinedButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.main.ModeSwitcherAnimatedIcon
import com.example.vibefinance.ui.main.TransactionMode
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.VibeHaptics
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Layers
import androidx.compose.ui.platform.LocalView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    accounts: List<AccountEntity>,
    onDismissRequest: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onUpdateTransaction: ((TransactionEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    var currentTx by remember(transaction.id, transaction.latitude, transaction.longitude) { mutableStateOf(transaction) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPresetSheet by remember { mutableStateOf(false) }

    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val sourceAccount = remember(currentTx.accountId, accounts) {
        accounts.firstOrNull { it.id == currentTx.accountId }
    }
    val destinationAccount = remember(currentTx.toAccountId, accounts) {
        accounts.firstOrNull { it.id == currentTx.toAccountId }
    }

    val isTransfer = currentTx.toAccountId != null
    val isIncome = currentTx.amount < 0 && !isTransfer
    val isExpense = currentTx.amount >= 0 && !isTransfer
    val isReimbursement = currentTx.isReimbursement

    val displayAmount = abs(currentTx.amount)
    val amountPrefix = when {
        isTransfer -> ""
        isIncome -> "+"
        else -> "-"
    }
    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.primary
        isIncome -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.error
    }

    val dialogScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "detailDialogContainerTransform"
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("TransactionDetailDialog"),
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var iconClickTrigger by remember { mutableStateOf(1) }
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                iconClickTrigger++
                            },
                        shape = CircleShape,
                        color = when {
                            isReimbursement -> MaterialTheme.colorScheme.secondaryContainer
                            isTransfer -> MaterialTheme.colorScheme.primaryContainer
                            isIncome -> Color(0xFFE8F5E9)
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isReimbursement) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                    contentDescription = stringResource(R.string.reimbursement_title),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            } else {
                                val mode = when {
                                    isTransfer -> TransactionMode.TRANSFER
                                    isIncome -> TransactionMode.INCOME
                                    else -> TransactionMode.EXPENSE
                                }
                                val iconTint = when {
                                    isTransfer -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isIncome -> Color(0xFF1B5E20)
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                                ModeSwitcherAnimatedIcon(
                                    mode = mode,
                                    tint = iconTint,
                                    isSelected = true,
                                    clickTrigger = iconClickTrigger,
                                    iconSize = 20.dp
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = when {
                                isReimbursement -> stringResource(R.string.reimbursement_title)
                                isTransfer -> stringResource(R.string.filter_transfer)
                                isIncome -> stringResource(R.string.filter_income)
                                else -> com.example.vibefinance.ui.home.getCategoryDisplayName(currentTx.category)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(currentTx.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Hero Amount Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%sHK$%,.2f", amountPrefix, displayAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = amountColor,
                            maxLines = 1
                        )

                        if (currentTx.description.isNotBlank()) {
                            Text(
                                text = currentTx.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. OpenStreetMap (OSM) Location Card (Graceful display when coordinates exist)
                val lat = currentTx.latitude
                val lng = currentTx.longitude
                if (lat != null && lng != null) {
                    val rawLocName = currentTx.locationName
                    val locLabel = rawLocName?.takeIf { it.isNotBlank() }
                        ?: String.format(Locale.US, "%.5f, %.5f", lat, lng)

                    val startStationName = if (!currentTx.startLocationName.isNullOrBlank()) {
                        currentTx.startLocationName
                    } else if (rawLocName?.contains(" ➔ ") == true) {
                        rawLocName.substringBefore(" ➔ ").trim()
                    } else null

                    val endStationName = if (rawLocName?.contains(" ➔ ") == true) {
                        rawLocName.substringAfter(" ➔ ").trim()
                    } else null

                    val startStationObj = if (startStationName != null) {
                        com.example.vibefinance.transit.MtrStationRegistry.getStationByName(context, startStationName)
                    } else null
                    val effectiveStartLat = currentTx.startLatitude ?: startStationObj?.latitude
                    val effectiveStartLng = currentTx.startLongitude ?: startStationObj?.longitude

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("OsmLocationCard"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.tx_detail_location),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    CompletePressTextButton(
                                        onClick = {
                                            val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(currentTx.description.ifBlank { currentTx.category })})")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            runCatching { context.startActivity(mapIntent) }
                                        },
                                        shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(R.string.tx_detail_open_in_map), style = MaterialTheme.typography.labelSmall)
                                    }

                                    if (onUpdateTransaction != null) {
                                        CompletePressTextButton(
                                            onClick = { showPresetSheet = true },
                                            shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text(stringResource(R.string.tx_detail_retag_location), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }

                            // Transit Route Bento Card
                            if (startStationName != null && endStationName != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFF00C853)))
                                            Text(
                                                text = startStationName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.DirectionsSubway,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFFD50000)))
                                            Text(
                                                text = endStationName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "港鐵出行",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            TransactionLocationMap(
                                latitude = lat, longitude = lng,
                                startLatitude = effectiveStartLat, startLongitude = effectiveStartLng,
                                description = locLabel,
                                modifier = Modifier.fillMaxWidth().height(180.dp)
                            )

                            // Location Address / Street description
                            Text(
                                text = locLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (onUpdateTransaction != null) {
                    var isTagging by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("OsmNoLocationCard"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.tx_detail_no_location_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.tx_detail_no_location_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompletePressButton(
                                    onClick = {
                                        if (!isTagging) {
                                            isTagging = true
                                            scope.launch {
                                                val freshLoc = com.example.vibefinance.util.GnssLocationManager.resolveFreshLocation(context)
                                                    ?: com.example.vibefinance.util.GnssLocationManager.getInstantLocation(context)
                                                isTagging = false
                                                if (freshLoc != null) {
                                                    val updated = currentTx.copy(
                                                        latitude = freshLoc.latitude,
                                                        longitude = freshLoc.longitude,
                                                        locationName = freshLoc.locationName
                                                    )
                                                    currentTx = updated
                                                    onUpdateTransaction(updated)
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        context.getString(R.string.tx_detail_location_success),
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        context.getString(R.string.tx_detail_location_failed),
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                    showPresetSheet = true
                                                }
                                            }
                                        }
                                    },
                                    shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                    enabled = !isTagging,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                ) {
                                    if (isTagging) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.tx_detail_tag_location),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                CompletePressOutlinedButton(
                                    onClick = { showPresetSheet = true },
                                    shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.tx_detail_tag_preset),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Detail Info Grid
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Account row
                        DetailRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = stringResource(R.string.label_account),
                            value = when {
                                isTransfer -> "${sourceAccount?.name ?: "#${currentTx.accountId}"} → ${destinationAccount?.name ?: "#${currentTx.toAccountId}"}"
                                else -> sourceAccount?.name ?: "#${currentTx.accountId}"
                            }
                        )

                        // Category row
                        if (!isTransfer) {
                            DetailRow(
                                icon = Icons.Default.Category,
                                label = stringResource(R.string.cat_others),
                                value = com.example.vibefinance.ui.home.getCategoryDisplayName(currentTx.category)
                            )
                        }

                        // Daily budget status
                        if (isExpense) {
                            DetailRow(
                                icon = if (currentTx.isExcludedFromDailyBudget) Icons.Outlined.EventBusy else Icons.Outlined.EventAvailable,
                                label = stringResource(R.string.daily_budget_toggle_title),
                                value = if (currentTx.isExcludedFromDailyBudget) {
                                    stringResource(R.string.daily_budget_toggle_desc_off)
                                } else {
                                    stringResource(R.string.daily_budget_toggle_desc_on)
                                }
                            )
                        }

                        // Reimbursement status
                        if (currentTx.isReimbursement) {
                            DetailRow(
                                icon = Icons.AutoMirrored.Outlined.AssignmentReturn,
                                label = stringResource(R.string.reimbursement_title),
                                value = if (currentTx.reimbursementOf != null) {
                                    "#${currentTx.reimbursementOf}"
                                } else {
                                    stringResource(R.string.reimbursement_unlink)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button (Error tone)
                CompletePressTextButton(
                    onClick = { showDeleteConfirm = true },
                    shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("DetailDeleteButton")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_delete))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Close button
                    CompletePressOutlinedButton(
                        onClick = onDismissRequest,
                        shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                        modifier = Modifier.testTag("DetailCloseButton")
                    ) {
                        Text(stringResource(R.string.btn_close))
                    }

                    // Edit button
                    CompletePressButton(
                        onClick = {
                            onDismissRequest()
                            onEdit(currentTx)
                        },
                        shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                        modifier = Modifier.testTag("DetailEditButton")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_edit))
                    }
                }
            }
        }
    )

    // District / Location Preset Sheet Dialog
    if (showPresetSheet && onUpdateTransaction != null) {
        AlertDialog(
            onDismissRequest = { showPresetSheet = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(R.string.tx_detail_select_district_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.vibefinance.util.GnssLocationManager.PRESETS.forEach { preset ->
                        Surface(
                            onClick = {
                                showPresetSheet = false
                                val updated = currentTx.copy(
                                    latitude = preset.latitude,
                                    longitude = preset.longitude,
                                    locationName = preset.title
                                )
                                currentTx = updated
                                onUpdateTransaction(updated)
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.tx_detail_location_success) + ": " + preset.title,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Clear Location option if location currently exists
                    if (currentTx.latitude != null) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            onClick = {
                                showPresetSheet = false
                                val updated = currentTx.copy(
                                    latitude = null,
                                    longitude = null,
                                    locationName = null
                                )
                                currentTx = updated
                                onUpdateTransaction(updated)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tx_detail_remove_location),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetSheet = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.tx_detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.tx_detail_delete_confirm_desc)) },
            confirmButton = {
                CompletePressButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDismissRequest()
                        onDelete(currentTx)
                    },
                    shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                CompletePressTextButton(
                    onClick = { showDeleteConfirm = false },
                    shapes = AppButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32))
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(16.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

