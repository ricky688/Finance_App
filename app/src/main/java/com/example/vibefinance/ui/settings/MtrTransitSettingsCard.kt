package com.example.vibefinance.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.transit.MtrPassengerType
import com.example.vibefinance.transit.MtrTransitSettings
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.util.VibeHaptics

@Composable
fun MtrTransitSettingsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    var isEnabled by remember { mutableStateOf(MtrTransitSettings.isEnabled(context)) }
    var selectedAccountId by remember { mutableStateOf(MtrTransitSettings.getOctopusAccountId(context)) }
    var passengerType by remember { mutableStateOf(MtrTransitSettings.getPassengerType(context)) }

    val accounts by InMemoryDatabase.accounts.collectAsStateWithLifecycle()
    var showAccountDialog by remember { mutableStateOf(false) }

    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id.toString() == selectedAccountId }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsSubway,
                            contentDescription = "MTR",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "港鐵出行起點自動追蹤",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "出閘時根據扣款票價與歷史足跡自動反推起點",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                AppSwitch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        VibeHaptics.lightTick(view)
                        isEnabled = checked
                        MtrTransitSettings.setEnabled(context, checked)
                    }
                )
            }

            // Expanded Settings Content
            AnimatedVisibility(
                visible = isEnabled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    NfcOriginSettings()

                    // 1. Account Picker
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "八達通卡種 / 資產帳戶綁定",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            onClick = {
                                VibeHaptics.lightTick(view)
                                showAccountDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedAccount?.name ?: "自動識別八達通帳戶 (Auto-detect)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (selectedAccount != null) {
                                            Text(
                                                text = "專屬綁定此帳戶進行港鐵行程反推",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 2. Passenger Type Selector (2x2 Grid with zero awkward text wrap)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "乘車身分 / 票價模式",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PassengerTypeOptionCard(
                                title = "成人",
                                subtitle = "標準票價",
                                selected = passengerType == MtrPassengerType.ADULT,
                                onClick = {
                                    VibeHaptics.lightTick(view)
                                    passengerType = MtrPassengerType.ADULT
                                    MtrTransitSettings.setPassengerType(context, MtrPassengerType.ADULT)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PassengerTypeOptionCard(
                                title = "學生",
                                subtitle = "約 50% 優惠",
                                selected = passengerType == MtrPassengerType.STUDENT,
                                onClick = {
                                    VibeHaptics.lightTick(view)
                                    passengerType = MtrPassengerType.STUDENT
                                    MtrTransitSettings.setPassengerType(context, MtrPassengerType.STUDENT)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PassengerTypeOptionCard(
                                title = "小童",
                                subtitle = "約 50% 特惠",
                                selected = passengerType == MtrPassengerType.CHILD,
                                onClick = {
                                    VibeHaptics.lightTick(view)
                                    passengerType = MtrPassengerType.CHILD
                                    MtrTransitSettings.setPassengerType(context, MtrPassengerType.CHILD)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PassengerTypeOptionCard(
                                title = "樂悠 / 長者",
                                subtitle = "HK$2 劃一優惠",
                                selected = passengerType == MtrPassengerType.JOYYOU,
                                onClick = {
                                    VibeHaptics.lightTick(view)
                                    passengerType = MtrPassengerType.JOYYOU
                                    MtrTransitSettings.setPassengerType(context, MtrPassengerType.JOYYOU)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = when (passengerType) {
                                MtrPassengerType.ADULT -> "適用於標準成人八達通與信用卡搭乘"
                                MtrPassengerType.STUDENT -> "學生八達通享有約 50% 港鐵重鐵乘車優惠"
                                MtrPassengerType.CHILD -> "適用於 3 至 11 歲小童特惠八達通"
                                MtrPassengerType.JOYYOU -> "60歲以上樂悠咭享有政府 $2.00 劃一優惠計劃"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // Account Selection Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text(
                    text = "選擇 Smart Octopus / 手機八達通帳戶",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option: Auto-detect
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedAccountId == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        onClick = {
                            VibeHaptics.lightTick(view)
                            selectedAccountId = null
                            MtrTransitSettings.setOctopusAccountId(context, null)
                            showAccountDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "自動識別八達通帳戶 (Auto-detect)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedAccountId == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAccountId == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Existing User Accounts
                    accounts.forEach { acc ->
                        val isCurrent = selectedAccountId == acc.id.toString()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            onClick = {
                                VibeHaptics.lightTick(view)
                                selectedAccountId = acc.id.toString()
                                MtrTransitSettings.setOctopusAccountId(context, acc.id.toString())
                                showAccountDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!acc.cardLast4.isNullOrBlank()) {
                                        Text(
                                            text = "末四碼: •••• ${acc.cardLast4}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("完成")
                }
            }
        )
    }
}

@Composable
private fun PassengerTypeOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
