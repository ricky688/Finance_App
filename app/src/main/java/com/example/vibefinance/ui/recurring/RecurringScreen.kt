package com.example.vibefinance.ui.recurring

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.home.CategoryIcon
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header spacer
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(88.dp)
                )
            }

            // Stats summary card
            item {
                val totalMonthlyRecurring = state.subscriptions.sumOf { sub ->
                    if (sub.frequency.lowercase() == "weekly") sub.amount * 4.33 else sub.amount
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "ESTIMATED MONTHLY RECURRING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("$%,.2f", totalMonthlyRecurring),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.subscriptions.size} active subscriptions tracked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Subscriptions list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tracked Subscriptions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Placeholder when empty
            if (state.subscriptions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No subscriptions tracked yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Render subscriptions items (Connected M3 List)
            itemsIndexed(state.subscriptions) { index, sub ->
                val isFirst = index == 0
                val isLast = index == state.subscriptions.size - 1
                val itemShape = remember(isFirst, isLast) {
                    when {
                        isFirst && isLast -> RoundedCornerShape(24.dp)
                        isFirst -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        isLast -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                        else -> RoundedCornerShape(4.dp)
                    }
                }

                val associatedCardName = state.accounts.find { it.id == sub.accountId }?.name ?: "Unknown Card"
                
                val today = LocalDate.now()
                val paymentLocalDate = Instant.ofEpochMilli(sub.nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
                val daysRemaining = ChronoUnit.DAYS.between(today, paymentLocalDate).toInt()
                
                val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8F9FA)
                
                val countdownText = when {
                    daysRemaining < 0 -> "Overdue"
                    daysRemaining == 0 -> "Renews today"
                    daysRemaining == 1 -> "Renews tomorrow"
                    else -> "Renews in $daysRemaining days"
                }

                val badgeColor = when {
                    daysRemaining <= 0 -> if (isDark) Color(0xFFFFB3AD) else Color(0xFFD93025) // Red alert
                    daysRemaining == 1 -> if (isDark) Color(0xFFFFD43A) else Color(0xFFE37400) // Orange warning
                    else -> MaterialTheme.colorScheme.secondary // Standard
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                        .clickable {
                            editingSubscription = sub
                            showAddEditDialog = true
                        },
                    shape = itemShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CategoryIcon(
                                        category = sub.category,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = sub.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Charged to: $associatedCardName",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("$%,.2f", sub.amount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "/" + sub.frequency.lowercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val formattedDate = paymentLocalDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))
                                Text(
                                    text = "Next charge: $formattedDate",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(badgeColor.copy(alpha = 0.15f))
                                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = countdownText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        editingSubscription = sub
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Subscription",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add Subscription trigger button
            item {
                Button(
                    onClick = {
                        editingSubscription = null
                        showAddEditDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add/Edit Dialog
    if (showAddEditDialog) {
        var nameText by remember { mutableStateOf(editingSubscription?.name ?: "") }
        var amountText by remember { mutableStateOf(editingSubscription?.amount?.toString() ?: "") }
        var categoryText by remember { mutableStateOf(editingSubscription?.category ?: "Entertainment") }
        var frequencyText by remember { mutableStateOf(editingSubscription?.frequency ?: "Monthly") }
        var accountIdText by remember {
            val defaultAcc = state.accounts.firstOrNull()?.id ?: 0L
            mutableStateOf(editingSubscription?.accountId ?: defaultAcc)
        }
        var nextPaymentDateState by remember { mutableStateOf(editingSubscription?.nextPaymentDate ?: System.currentTimeMillis()) }

        var categoryDropdownExpanded by remember { mutableStateOf(false) }
        var frequencyDropdownExpanded by remember { mutableStateOf(false) }
        var accountDropdownExpanded by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (editingSubscription == null) "Add Subscription" else "Modify ${editingSubscription?.name}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Subscription Name
                    item {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Subscription Name (e.g. Netflix)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Charge Amount
                    item {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category
                    item {
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = categoryText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                listOf("Entertainment", "Utilities", "Food", "Other").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            categoryText = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Frequency
                    item {
                        ExposedDropdownMenuBox(
                            expanded = frequencyDropdownExpanded,
                            onExpandedChange = { frequencyDropdownExpanded = !frequencyDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = frequencyText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Billing Cycle") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = frequencyDropdownExpanded,
                                onDismissRequest = { frequencyDropdownExpanded = false }
                            ) {
                                listOf("Weekly", "Monthly").forEach { freq ->
                                    DropdownMenuItem(
                                        text = { Text(freq) },
                                        onClick = {
                                            frequencyText = freq
                                            frequencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Associated payment account/card
                    item {
                        val currentAccountName = state.accounts.find { it.id == accountIdText }?.name ?: "Select Payment Card"
                        ExposedDropdownMenuBox(
                            expanded = accountDropdownExpanded,
                            onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = currentAccountName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pay From Card / Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = accountDropdownExpanded,
                                onDismissRequest = { accountDropdownExpanded = false }
                            ) {
                                state.accounts.forEach { acc ->
                                    val balanceLabel = if (acc.type == com.example.vibefinance.data.entity.AccountType.CC) {
                                        String.format(Locale.US, "Outstanding: $%,.2f", acc.balance)
                                    } else {
                                        String.format(Locale.US, "Balance: $%,.2f", acc.balance)
                                    }
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} ($balanceLabel)") },
                                        onClick = {
                                            accountIdText = acc.id
                                            accountDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Next Payment Date DatePicker trigger
                    item {
                        val localDateStr = Instant.ofEpochMilli(nextPaymentDateState).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.US))
                        OutlinedTextField(
                            value = localDateStr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Next Payment Date") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (nameText.isNotEmpty() && amt > 0.0 && accountIdText != 0L) {
                            val subscription = SubscriptionEntity(
                                id = editingSubscription?.id ?: 0L,
                                name = nameText,
                                amount = amt,
                                category = categoryText,
                                frequency = frequencyText,
                                nextPaymentDate = nextPaymentDateState,
                                accountId = accountIdText
                            )
                            onIntent(FinanceIntent.SaveSubscription(subscription))
                            showAddEditDialog = false
                        }
                    }
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editingSubscription != null) {
                        TextButton(
                            onClick = {
                                onIntent(FinanceIntent.DeleteSubscription(editingSubscription!!))
                                showAddEditDialog = false
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showAddEditDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp))
        )

        // Date Picker Modal
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = nextPaymentDateState
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                nextPaymentDateState = it
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Select")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
