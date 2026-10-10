@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.radar

import com.example.vibefinance.ui.components.AppModalBottomSheet as ModalBottomSheet

import com.example.vibefinance.ui.components.AppSwitch

import androidx.compose.ui.text.style.TextOverflow
import com.example.vibefinance.ui.components.CompletePressButton

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.vibefinance.ui.common.pressBounce
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.DiscountShop
import com.example.vibefinance.data.entity.ShopDiscountOffer
import com.example.vibefinance.util.GnssLocationManager
import com.example.vibefinance.util.GnssPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiscountShopSheet(
    accounts: List<AccountEntity>,
    currentGnssPoint: GnssPoint,
    onDismiss: () -> Unit,
    onSaveShop: (DiscountShop) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var shopName by remember { mutableStateOf("") }
    var aspect by remember { mutableStateOf("Coffee & Cafe") }
    var address by remember { mutableStateOf("") }
    var promoDescription by remember { mutableStateOf("") }

    var selectedAccountId by remember {
        mutableStateOf(accounts.firstOrNull { it.type == AccountType.CC }?.id ?: accounts.firstOrNull()?.id ?: 0L)
    }

    var discountRateText by remember { mutableStateOf("5.0") }
    var useCurrentGnss by remember { mutableStateOf(true) }
    var customLat by remember { mutableStateOf(currentGnssPoint.latitude.toString()) }
    var customLng by remember { mutableStateOf(currentGnssPoint.longitude.toString()) }

    val aspectOptions = listOf(
        "Coffee & Cafe",
        "Supermarket",
        "Dining",
        "Gas & Fuel",
        "Electronics",
        "Retail & Shopping",
        "Entertainment",
        "Services"
    )

    var aspectExpanded by remember { mutableStateOf(false) }

    val creditCards = accounts.filter { it.type == AccountType.CC || (it.creditLimit != null && it.creditLimit > 0) }
    val displayCards = if (creditCards.isNotEmpty()) creditCards else accounts

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.ui_radar_sheet_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.ui_radar_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.btn_close))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Shop Name
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text(stringResource(R.string.ui_radar_shop_name)) },
                placeholder = { Text(stringResource(R.string.ui_radar_shop_name_hint)) },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            val savedAspects by com.example.vibefinance.data.InMemoryDatabase.savedAspects.collectAsStateWithLifecycle()

            // Shop Aspect / Category Dropdown
            ExposedDropdownMenuBox(
                expanded = aspectExpanded,
                onExpandedChange = { aspectExpanded = !aspectExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = radarAspectLabel(aspect),
                    onValueChange = {
                        aspect = it
                        aspectExpanded = true
                    },
                    readOnly = false,
                    label = { Text(stringResource(R.string.ui_radar_shop_category)) },
                    placeholder = { Text(stringResource(R.string.ui_radar_shop_category_hint)) },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aspectExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = aspectExpanded,
                    onDismissRequest = { aspectExpanded = false }
                ) {
                    savedAspects.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(radarAspectLabel(option)) },
                            onClick = {
                                aspect = option
                                aspectExpanded = false
                            }
                        )
                    }
                }
            }

            // Address Field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.ui_radar_shop_address)) },
                placeholder = { Text(stringResource(R.string.ui_radar_shop_address_hint)) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Credit Card Discount Offer Section
            Text(
                text = stringResource(R.string.ui_radar_shop_perk),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Card Selector Chips
            Text(
                text = stringResource(R.string.ui_radar_shop_select_card),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayCards.forEach { card ->
                    val isSelected = selectedAccountId == card.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAccountId = card.id },
                        label = { Text(card.name, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Discount Rate Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = discountRateText,
                    onValueChange = { discountRateText = it },
                    label = { Text(stringResource(R.string.ui_radar_shop_discount_rate)) },
                    placeholder = { Text("5.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.height(56.dp).padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val rate = discountRateText.toDoubleOrNull() ?: 0.0
                        Text(
                            text = stringResource(R.string.ui_radar_shop_savings, rate.toString()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Promo Description
            OutlinedTextField(
                value = promoDescription,
                onValueChange = { promoDescription = it },
                label = { Text(stringResource(R.string.ui_radar_shop_promo_note)) },
                placeholder = { Text(stringResource(R.string.ui_radar_shop_promo_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // GNSS Location Setting
            Text(
                text = stringResource(R.string.ui_radar_shop_coordinates),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ui_radar_use_current_location, radarDistanceLabel(0.0)),
                    style = MaterialTheme.typography.bodyMedium
                )
                AppSwitch(
                    checked = useCurrentGnss,
                    onCheckedChange = { useCurrentGnss = it }
                )
            }

            AnimatedVisibility(visible = !useCurrentGnss) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customLat,
                        onValueChange = { customLat = it },
                        label = { Text(stringResource(R.string.ui_radar_shop_latitude)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customLng,
                        onValueChange = { customLng = it },
                        label = { Text(stringResource(R.string.ui_radar_shop_longitude)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            val submitInteraction = remember { MutableInteractionSource() }
            CompletePressButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val lat = if (useCurrentGnss) currentGnssPoint.latitude else customLat.toDoubleOrNull() ?: currentGnssPoint.latitude
                    val lng = if (useCurrentGnss) currentGnssPoint.longitude else customLng.toDoubleOrNull() ?: currentGnssPoint.longitude
                    val rate = discountRateText.toDoubleOrNull() ?: 5.0

                    if (shopName.isNotBlank() && aspect.isNotBlank()) {
                        com.example.vibefinance.data.InMemoryDatabase.saveAspect(aspect)
                        val newShop = DiscountShop(
                            name = shopName.trim(),
                            aspect = aspect.trim(),
                            latitude = lat,
                            longitude = lng,
                            address = address.ifBlank { "Registered Store Location" },
                            offers = listOf(
                                ShopDiscountOffer(
                                    accountId = selectedAccountId,
                                    discountRate = rate,
                                    promoDescription = promoDescription.ifBlank { "${rate}% Discount / Cashback Perk" }
                                )
                            ),
                            isUserCreated = true
                        )
                        onSaveShop(newShop)
                        onDismiss()
                    }
                },
                enabled = shopName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .pressBounce(interactionSource = submitInteraction),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
                shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(27.dp), pressedShape = RoundedCornerShape(16.dp)),
                interactionSource = submitInteraction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ui_radar_shop_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
