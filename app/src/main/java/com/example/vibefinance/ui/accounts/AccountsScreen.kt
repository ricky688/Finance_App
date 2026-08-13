package com.example.vibefinance.ui.accounts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.Slider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.vibefinance.ui.common.bouncyClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceIntent

import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.MoneySeparationChart
import com.example.vibefinance.ui.components.InteractiveDonutChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun getCardGradient(themeName: String?, isDark: Boolean): Brush {
    val colors = when (themeName?.lowercase(Locale.US)) {
        "ocean", "ocean breeze" -> listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
        "emerald", "emerald forest" -> listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
        "sunset", "sunset glow" -> listOf(Color(0xFFE65100), Color(0xFFF57C00))
        "purple", "purple neon" -> listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
        "rose" -> listOf(Color(0xFF880E4F), Color(0xFFC2185B))
        "crimson" -> listOf(Color(0xFFB71C1C), Color(0xFFD32F2F))
        "dark stealth" -> listOf(Color(0xFF1A1D20), Color(0xFF0D0E10))
        else -> listOf(Color(0xFF161B22), Color(0xFF242C37)) // Rich Dark Metallic Slate for Default Cards
    }
    return Brush.verticalGradient(colors)
}

enum class AssetFilter(val label: String) {
    ALL("All Assets"),
    CASH_BANK("Cash & Bank"),
    CREDIT_CARDS("Credit Cards")
}

@Composable
fun AssetCardItem(
    account: AccountEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val customImageBitmap: ImageBitmap? = remember(account.cardImageUri) {
        account.cardImageUri?.let { uriStr ->
            try {
                if (uriStr.equals("sample", ignoreCase = true)) {
                    createSampleCardBitmap(context)?.asImageBitmap()
                } else {
                    val file = File(uriStr)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    val hasCustomImage = customImageBitmap != null
    val last4 = account.cardLast4 ?: (account.id + 4000).toString()

    val cardTypeName = when (account.type) {
        AccountType.CC -> "CREDIT CARD"
        AccountType.CASH -> "CASH WALLET"
        AccountType.BANK -> "SAVINGS BANK"
    }

    Card(
        modifier = modifier
            .width(260.dp)
            .height(156.dp)
            .clip(RoundedCornerShape(22.dp))
            .bouncyClickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawCardBackground(
                    bitmap = customImageBitmap,
                    themeName = if (hasCustomImage) null else (account.cardTheme ?: "default"),
                    patternName = account.cardPattern ?: "cyber grid",
                    isDark = isDark
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row: Card Type Word Badge & Protocol/Issuer logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.32f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = cardTypeName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (account.cardProtocol != null) {
                        CardProtocolLogo(protocol = account.cardProtocol)
                    } else {
                        Icon(
                            imageVector = when (account.type) {
                                AccountType.CC -> Icons.Default.CreditCard
                                AccountType.CASH -> Icons.Default.Savings
                                AccountType.BANK -> Icons.Default.AccountBalance
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Middle Word Display: Account Name
                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (account.type == AccountType.CC) "•••• $last4" else "Account #${account.id + 1000}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                // Footer: Balance Amount Display & M3 Circular Progress Indicator for Credit Utilization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (account.type == AccountType.CC) "OUTSTANDING DEBT" else "CURRENT BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                        com.example.vibefinance.ui.components.RollingNumberText(
                            text = String.format(Locale.US, "$%,.2f", account.balance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    val limit = account.creditLimit
                    if (account.type == AccountType.CC && limit != null && limit > 0.0) {
                        val ratio = (account.balance / limit).coerceIn(0.0, 1.0).toFloat()
                        val percent = (ratio * 100).toInt()
                        val indicatorColor = when {
                            ratio > 0.70f -> Color(0xFFFF6B6B)
                            ratio > 0.30f -> Color(0xFFFFB74D)
                            else -> Color(0xFF81C784)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(36.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxSize(),
                                color = indicatorColor,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                strokeWidth = 3.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val totalAssets = state.accounts.filter { it.type != AccountType.CC }.sumOf { it.balance }
    val totalDebt = state.accounts.filter { it.type == AccountType.CC }.sumOf { it.balance }
    val netWorth = totalAssets - totalDebt

    val cashbackRules by com.example.vibefinance.data.InMemoryDatabase.cashbackRules.collectAsStateWithLifecycle()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedAssetFilter by remember { mutableStateOf(AssetFilter.ALL) }

    val filteredAccounts = remember(state.accounts, selectedAssetFilter) {
        when (selectedAssetFilter) {
            AssetFilter.ALL -> state.accounts
            AssetFilter.CASH_BANK -> state.accounts.filter { it.type != AccountType.CC }
            AssetFilter.CREDIT_CARDS -> state.accounts.filter { it.type == AccountType.CC }
        }
    }
    val creditCards = remember(state.accounts) { state.accounts.filter { it.type == AccountType.CC } }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "accountsSkeletonCrossfade"
        ) { isLoading ->
            if (isLoading) {
                com.example.vibefinance.ui.components.AccountsScreenSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
            // 1. Assets Overview Title Header Spacer
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(88.dp)
                )
            }

            // 2. Premium Net Asset Value Header Card with Rolling Number Text
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8F9FA)
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
                            // Top Row: Net Asset Value text stats on left, Interactive Donut Chart on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NET ASSET VALUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    com.example.vibefinance.ui.components.RollingNumberText(
                                        text = String.format(Locale.US, "$%,.2f", netWorth),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                InteractiveDonutChart(
                                    accounts = state.accounts,
                                    size = 96f
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Cash/Bank",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    com.example.vibefinance.ui.components.RollingNumberText(
                                        text = String.format(Locale.US, "$%,.2f", totalAssets),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Card Debt",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    com.example.vibefinance.ui.components.RollingNumberText(
                                        text = String.format(Locale.US, "$%,.2f", totalDebt),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )

                            // Money Separation Chart & Proportional Allocation Bar
                            MoneySeparationChart(
                                accounts = state.accounts
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2.5 GNSS Radar Shortcut Banner
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = "Radar Scan",
                                        tint = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "GNSS Nearby Discount Radar 🎯",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Scan nearby shops & match card cashback perks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }



            // 2.8. M3 Expressive Asset Category Filter Chips Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(AssetFilter.values()) { filter ->
                            val isSelected = selectedAssetFilter == filter
                            val count = when (filter) {
                                AssetFilter.ALL -> state.accounts.size
                                AssetFilter.CASH_BANK -> state.accounts.count { it.type != AccountType.CC }
                                AssetFilter.CREDIT_CARDS -> state.accounts.count { it.type == AccountType.CC }
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAssetFilter = filter },
                                label = {
                                    Text(
                                        text = "${filter.label} ($count)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = CircleShape
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Expressive Header & Add Asset Button
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Assets & Cards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = {
                                editingAccount = null
                                showAddEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Account",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Asset",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3.5 Physical Credit Card Wallet Carousel
            if (creditCards.isNotEmpty() && (selectedAssetFilter == AssetFilter.ALL || selectedAssetFilter == AssetFilter.CREDIT_CARDS)) {
                item {
                    CreditCardWalletCarousel(
                        creditCards = creditCards,
                        onEditCard = { card ->
                            editingAccount = card
                            showAddEditDialog = true
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // 4. Edge-to-Edge Full-Bleed Accounts List Items
            itemsIndexed(filteredAccounts, key = { _, acc -> acc.id }) { index, account ->
                ExpressiveAccountListItem(
                    account = account,
                    transactions = state.transactions,
                    cashbackRules = cashbackRules,
                    onEditClick = {
                        editingAccount = account
                        showAddEditDialog = true
                    }
                )
            }

            // 5. Generous Bottom Spacer to allow scrolling further down
            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
    }

    // 6. Interactive full CRUD Add / Edit Dialog
    if (showAddEditDialog) {
        val context = LocalContext.current
        remember {
            createTestBgIfNeeded(context)
        }
        var nameText by remember { mutableStateOf(editingAccount?.name ?: "") }
        var typeState by remember { mutableStateOf(editingAccount?.type ?: AccountType.CASH) }
        var balanceText by remember { mutableStateOf(editingAccount?.balance?.toString() ?: "0.0") }
        var quickAdjustAmountText by remember { mutableStateOf("") }
        
        var creditLimitText by remember { mutableStateOf(editingAccount?.creditLimit?.toString() ?: "5000") }
        var minSpendThresholdText by remember { mutableStateOf(editingAccount?.minSpendThreshold?.toString() ?: "") }
        var billingDateText by remember { mutableStateOf(editingAccount?.billingDate?.toString() ?: "10") }
        var paymentDateText by remember { mutableStateOf(editingAccount?.paymentDate?.toString() ?: "25") }
        var paymentDeadlineText by remember { mutableStateOf(editingAccount?.paymentDeadline?.toString() ?: "10") }
        
        var selectedCardTheme by remember { mutableStateOf(editingAccount?.cardTheme ?: "default") }
        var cardLast4Text by remember { mutableStateOf(editingAccount?.cardLast4 ?: "") }
        var selectedCardProtocol by remember { mutableStateOf(
            when (editingAccount?.cardProtocol?.lowercase(Locale.US)) {
                "visa" -> "Visa"
                "mastercard" -> "Mastercard"
                else -> "None"
            }
        ) }
        var selectedCardIssuer by remember { mutableStateOf(
            when (editingAccount?.cardIssuer?.lowercase(Locale.US)) {
                "boc" -> "BOC"
                "hsbc" -> "HSBC"
                "chase" -> "Chase"
                "citi" -> "Citi"
                else -> "None"
            }
        ) }
        var selectedCardPattern by remember { mutableStateOf(
            when (editingAccount?.cardPattern?.lowercase(Locale.US)) {
                "cyber grid" -> "Cyber Grid"
                "neon waves" -> "Neon Waves"
                "geometric mesh" -> "Geometric Mesh"
                else -> "None"
            }
        ) }
        var selectedCardImageUri by remember { mutableStateOf(editingAccount?.cardImageUri ?: "") }
        var selectedAspectRatio by remember { mutableStateOf(editingAccount?.customImageAspectRatio ?: "1.586:1") }
        val currentAccount = editingAccount
        var foodCashbackText by remember { mutableStateOf(if (currentAccount != null) com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(currentAccount.id, "Food").let { if (it > 0.0) it.toString() else "" } else "") }
        var transportCashbackText by remember { mutableStateOf(if (currentAccount != null) com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(currentAccount.id, "Transport").let { if (it > 0.0) it.toString() else "" } else "") }
        var shoppingCashbackText by remember { mutableStateOf(if (currentAccount != null) com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(currentAccount.id, "Shopping").let { if (it > 0.0) it.toString() else "" } else "") }
        var utilitiesCashbackText by remember { mutableStateOf(if (currentAccount != null) com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(currentAccount.id, "Utilities").let { if (it > 0.0) it.toString() else "" } else "") }
        var otherCashbackText by remember { mutableStateOf(if (currentAccount != null) com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(currentAccount.id, "Other").let { if (it > 0.0) it.toString() else "" } else "") }
        var tempBitmapToCrop by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        var showCropDialog by remember { mutableStateOf(false) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val imagesDir = File(context.filesDir, "account_images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val userPickedFile = File(imagesDir, "raw_user_${System.currentTimeMillis()}.png")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(userPickedFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (userPickedFile.exists() && userPickedFile.length() > 0) {
                        var bmap = BitmapFactory.decodeFile(userPickedFile.absolutePath)
                        if (bmap == null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            try {
                                val source = android.graphics.ImageDecoder.createSource(userPickedFile)
                                bmap = android.graphics.ImageDecoder.decodeBitmap(source)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        if (bmap != null) {
                            tempBitmapToCrop = bmap
                            showCropDialog = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (showCropDialog && tempBitmapToCrop != null) {
            InteractiveImageCropDialog(
                bitmap = tempBitmapToCrop!!,
                initialAspectRatio = selectedAspectRatio,
                onDismiss = { showCropDialog = false },
                onConfirm = { croppedBitmap, ratio ->
                    try {
                        val imagesDir = File(context.filesDir, "account_images")
                        if (!imagesDir.exists()) imagesDir.mkdirs()
                        val targetFile = File(imagesDir, "cropped_${System.currentTimeMillis()}.png")
                        FileOutputStream(targetFile).use { out ->
                            croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                        if (targetFile.exists() && targetFile.length() > 0) {
                            selectedCardImageUri = targetFile.absolutePath
                            selectedAspectRatio = ratio
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showCropDialog = false
                }
            )
        }
        val previewCardImage = rememberCardImagePainter(selectedCardImageUri)

        var selectedDialogTab by remember { mutableStateOf(0) }
        var typeDropdownExpanded by remember { mutableStateOf(false) }
        var billingDayDropdownExpanded by remember { mutableStateOf(false) }
        var paymentDayDropdownExpanded by remember { mutableStateOf(false) }
        var paymentDeadlineDayDropdownExpanded by remember { mutableStateOf(false) }
        var protocolDropdownExpanded by remember { mutableStateOf(false) }
        var issuerDropdownExpanded by remember { mutableStateOf(false) }
        var patternDropdownExpanded by remember { mutableStateOf(false) }
        val containerTransformScale by animateFloatAsState(
            targetValue = if (showAddEditDialog) 1.0f else 0.85f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "assetContainerTransformScale"
        )

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            modifier = Modifier.graphicsLayer {
                scaleX = containerTransformScale
                scaleY = containerTransformScale
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingAccount != null) {
                        TextButton(
                            onClick = {
                                onIntent(FinanceIntent.DeleteAccount(editingAccount!!))
                                showAddEditDialog = false
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showAddEditDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val name = nameText.trim().ifEmpty { "New Account" }
                            val balance = balanceText.toDoubleOrNull() ?: 0.0
                            val limit = creditLimitText.toDoubleOrNull() ?: 0.0
                            val minThreshold = minSpendThresholdText.toDoubleOrNull()
                            val billing = billingDateText.toIntOrNull() ?: 10
                            val payment = paymentDateText.toIntOrNull() ?: 25
                            val deadline = paymentDeadlineText.toIntOrNull() ?: 10

                            val acc = AccountEntity(
                                id = editingAccount?.id ?: 0L,
                                name = name,
                                type = typeState,
                                balance = balance,
                                icon = when (typeState) {
                                    AccountType.CASH -> "wallet"
                                    AccountType.BANK -> "bank"
                                    AccountType.CC -> "credit_card"
                                },
                                creditLimit = if (typeState == AccountType.CC) limit else null,
                                billingDate = if (typeState == AccountType.CC) billing else null,
                                paymentDate = if (typeState == AccountType.CC) payment else null,
                                paymentDeadline = if (typeState == AccountType.CC) deadline else null,
                                cardTheme = selectedCardTheme,
                                cardLast4 = if (cardLast4Text.length == 4) cardLast4Text else null,
                                cardProtocol = if (selectedCardProtocol == "None") null else selectedCardProtocol.lowercase(Locale.US),
                                cardIssuer = if (selectedCardIssuer == "None") null else selectedCardIssuer.lowercase(Locale.US),
                                cardPattern = if (selectedCardPattern == "None") null else selectedCardPattern.lowercase(Locale.US),
                                cardImageUri = if (selectedCardImageUri.isEmpty()) null else selectedCardImageUri,
                                customImageAspectRatio = selectedAspectRatio,
                                minSpendThreshold = if (typeState == AccountType.CC) minThreshold else null
                            )
                            val cashbackRates = mapOf(
                                "Food" to (foodCashbackText.toDoubleOrNull() ?: 0.0),
                                "Transport" to (transportCashbackText.toDoubleOrNull() ?: 0.0),
                                "Shopping" to (shoppingCashbackText.toDoubleOrNull() ?: 0.0),
                                "Utilities" to (utilitiesCashbackText.toDoubleOrNull() ?: 0.0),
                                "Other" to (otherCashbackText.toDoubleOrNull() ?: 0.0)
                            )
                            onIntent(FinanceIntent.SaveAccount(acc, cashbackRates))
                            showAddEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Changes")
                    }
                }
            },
            dismissButton = null,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = if (editingAccount == null) "Add Asset / Card" else "Modify ${editingAccount?.name}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // M3 Expressive Button Group (Essentials | Cashback | Design)
                    ExpressiveSegmentedButtonGroup(
                        items = listOf("💳 Essentials", "🎁 Cashback", "🎨 Design"),
                        selectedIndex = selectedDialogTab,
                        onItemSelected = { selectedDialogTab = it },
                        labelProvider = { it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )

                    // Scrollable Tab Content Container
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        0.04f to Color.Black,
                                        0.96f to Color.Black,
                                        1f to Color.Transparent
                                    ),
                                    blendMode = BlendMode.DstIn
                                )
                            },
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        if (selectedDialogTab == 0) {
                            // TAB 0: ESSENTIALS
                            item {
                                OutlinedTextField(
                                    value = nameText,
                                    onValueChange = { nameText = it },
                                    label = { Text("Account / Card Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item {
                                ExposedDropdownMenuBox(
                                    expanded = typeDropdownExpanded,
                                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = when (typeState) {
                                            AccountType.CASH -> "Cash Wallet"
                                            AccountType.BANK -> "Bank Account"
                                            AccountType.CC -> "Credit Card"
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Asset Type") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = typeDropdownExpanded,
                                        onDismissRequest = { typeDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(text = { Text("Cash Wallet") }, onClick = { typeState = AccountType.CASH; typeDropdownExpanded = false })
                                        DropdownMenuItem(text = { Text("Bank Account") }, onClick = { typeState = AccountType.BANK; typeDropdownExpanded = false })
                                        DropdownMenuItem(text = { Text("Credit Card") }, onClick = { typeState = AccountType.CC; typeDropdownExpanded = false })
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = balanceText,
                                        onValueChange = { balanceText = it },
                                        label = { Text(if (typeState == AccountType.CC) "Current Debt ($)" else "Current Balance ($)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    if (typeState == AccountType.CC) {
                                        OutlinedTextField(
                                            value = cardLast4Text,
                                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) cardLast4Text = it },
                                            label = { Text("Last 4") },
                                            placeholder = { Text("1234") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.weight(0.9f)
                                        )
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = quickAdjustAmountText,
                                        onValueChange = { quickAdjustAmountText = it },
                                        label = { Text("Quick Adjust ($)") },
                                        placeholder = { Text("e.g. 50") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                            val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                            balanceText = String.format(Locale.US, "%.2f", currentVal + delta)
                                        },
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Text("+")
                                    }
                                    Button(
                                        onClick = {
                                            val currentVal = balanceText.toDoubleOrNull() ?: 0.0
                                            val delta = quickAdjustAmountText.toDoubleOrNull() ?: 0.0
                                            balanceText = String.format(Locale.US, "%.2f", (currentVal - delta).coerceAtLeast(0.0))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Text("-")
                                    }
                                }
                            }

                            if (typeState == AccountType.CC) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = creditLimitText,
                                            onValueChange = { creditLimitText = it },
                                            label = { Text("Credit Limit ($)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = minSpendThresholdText,
                                            onValueChange = { minSpendThresholdText = it },
                                            label = { Text("Threshold / 門檻 ($)") },
                                            placeholder = { Text("e.g. 500") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        } else if (selectedDialogTab == 1) {
                            // TAB 1: CASHBACK PERKS & SCHEDULE
                            item {
                                Text(
                                    text = "Quick Perks Presets:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val presets = listOf(
                                        "Dining 5%" to mapOf("Food" to "5.0", "Transport" to "1.0", "Shopping" to "1.0", "Utilities" to "1.0", "Other" to "1.0"),
                                        "Grocery 6%" to mapOf("Food" to "6.0", "Transport" to "1.0", "Shopping" to "2.0", "Utilities" to "1.0", "Other" to "1.0"),
                                        "Fuel 5%" to mapOf("Food" to "1.0", "Transport" to "5.0", "Shopping" to "1.0", "Utilities" to "1.0", "Other" to "1.0"),
                                        "All 2% Flat" to mapOf("Food" to "2.0", "Transport" to "2.0", "Shopping" to "2.0", "Utilities" to "2.0", "Other" to "2.0")
                                    )
                                    items(presets) { (label, rates) ->
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                foodCashbackText = rates["Food"] ?: ""
                                                transportCashbackText = rates["Transport"] ?: ""
                                                shoppingCashbackText = rates["Shopping"] ?: ""
                                                utilitiesCashbackText = rates["Utilities"] ?: ""
                                                otherCashbackText = rates["Other"] ?: ""
                                            },
                                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }

                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = foodCashbackText,
                                        onValueChange = { foodCashbackText = it },
                                        label = { Text("Food (%)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = transportCashbackText,
                                        onValueChange = { transportCashbackText = it },
                                        label = { Text("Transport (%)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = shoppingCashbackText,
                                        onValueChange = { shoppingCashbackText = it },
                                        label = { Text("Shopping (%)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = utilitiesCashbackText,
                                        onValueChange = { utilitiesCashbackText = it },
                                        label = { Text("Utilities (%)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = otherCashbackText,
                                    onValueChange = { otherCashbackText = it },
                                    label = { Text("Other Category (%)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (typeState == AccountType.CC) {
                                item {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Text(text = "Billing & Payment Schedule", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ExposedDropdownMenuBox(
                                            expanded = billingDayDropdownExpanded,
                                            onExpandedChange = { billingDayDropdownExpanded = !billingDayDropdownExpanded },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = billingDateText,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Billing Day") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billingDayDropdownExpanded) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = billingDayDropdownExpanded,
                                                onDismissRequest = { billingDayDropdownExpanded = false }
                                            ) {
                                                (1..31).forEach { day ->
                                                    DropdownMenuItem(text = { Text(day.toString()) }, onClick = { billingDateText = day.toString(); billingDayDropdownExpanded = false })
                                                }
                                            }
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = paymentDayDropdownExpanded,
                                            onExpandedChange = { paymentDayDropdownExpanded = !paymentDayDropdownExpanded },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = paymentDateText,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Payment Due Day") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDayDropdownExpanded) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = paymentDayDropdownExpanded,
                                                onDismissRequest = { paymentDayDropdownExpanded = false }
                                            ) {
                                                (1..31).forEach { day ->
                                                    DropdownMenuItem(text = { Text(day.toString()) }, onClick = { paymentDateText = day.toString(); paymentDayDropdownExpanded = false })
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    ExposedDropdownMenuBox(
                                        expanded = paymentDeadlineDayDropdownExpanded,
                                        onExpandedChange = { paymentDeadlineDayDropdownExpanded = !paymentDeadlineDayDropdownExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = paymentDeadlineText,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Payment Deadline Day") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDeadlineDayDropdownExpanded) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedBorderColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = paymentDeadlineDayDropdownExpanded,
                                            onDismissRequest = { paymentDeadlineDayDropdownExpanded = false }
                                        ) {
                                            (1..31).forEach { day ->
                                                DropdownMenuItem(text = { Text(day.toString()) }, onClick = { paymentDeadlineText = day.toString(); paymentDeadlineDayDropdownExpanded = false })
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (selectedDialogTab == 2) {
                            // TAB 2: DESIGN & STYLING
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp)
                                            .rotate3DOnTouch(),
                                        shape = RoundedCornerShape(16.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .drawCardBackground(previewCardImage, selectedCardTheme, selectedCardPattern, isDark)
                                                .padding(14.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (selectedCardIssuer != "None") {
                                                            CardIssuerLogo(issuer = selectedCardIssuer, modifier = Modifier.padding(end = 8.dp))
                                                        }
                                                        Text(text = if (nameText.isEmpty()) "Card Preview" else nameText, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                    }
                                                    if (selectedCardProtocol != "None") {
                                                        CardProtocolLogo(protocol = selectedCardProtocol)
                                                    }
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                                    Text(text = if (cardLast4Text.length == 4) "•••• $cardLast4Text" else "•••• ••••", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                    Text(text = String.format(Locale.US, "$%,.2f", balanceText.toDoubleOrNull() ?: 0.0), color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(text = "Card Theme Preset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val themes = listOf(
                                            "default" to Pair(Color(0xFF161B22), Color(0xFF242C37)),
                                            "ocean" to Pair(Color(0xFF0D47A1), Color(0xFF1976D2)),
                                            "emerald" to Pair(Color(0xFF1B5E20), Color(0xFF388E3C)),
                                            "sunset" to Pair(Color(0xFFE65100), Color(0xFFF57C00)),
                                            "purple" to Pair(Color(0xFF4A148C), Color(0xFF7B1FA2)),
                                            "rose" to Pair(Color(0xFF880E4F), Color(0xFFC2185B)),
                                            "crimson" to Pair(Color(0xFFB71C1C), Color(0xFFD32F2F))
                                        )
                                        items(themes) { (themeKey, colors) ->
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(colors.first, colors.second)))
                                                    .border(width = if (selectedCardTheme == themeKey) 2.dp else 0.dp, color = if (selectedCardTheme == themeKey) MaterialTheme.colorScheme.primary else Color.Transparent, shape = CircleShape)
                                                    .clickable { selectedCardTheme = themeKey }
                                            )
                                        }
                                    }
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ExposedDropdownMenuBox(expanded = protocolDropdownExpanded, onExpandedChange = { protocolDropdownExpanded = !protocolDropdownExpanded }, modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = selectedCardProtocol,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Protocol") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = protocolDropdownExpanded) },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(expanded = protocolDropdownExpanded, onDismissRequest = { protocolDropdownExpanded = false }) {
                                                listOf("None", "Visa", "Mastercard").forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { selectedCardProtocol = p; protocolDropdownExpanded = false }) }
                                            }
                                        }

                                        ExposedDropdownMenuBox(expanded = issuerDropdownExpanded, onExpandedChange = { issuerDropdownExpanded = !issuerDropdownExpanded }, modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = selectedCardIssuer,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Issuer") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issuerDropdownExpanded) },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(expanded = issuerDropdownExpanded, onDismissRequest = { issuerDropdownExpanded = false }) {
                                                listOf("None", "BOC", "HSBC", "Chase", "Citi").forEach { i -> DropdownMenuItem(text = { Text(i) }, onClick = { selectedCardIssuer = i; issuerDropdownExpanded = false }) }
                                            }
                                        }
                                    }
                                }

                                item {
                                    ExposedDropdownMenuBox(expanded = patternDropdownExpanded, onExpandedChange = { patternDropdownExpanded = !patternDropdownExpanded }, modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedCardPattern,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Pattern Overlay") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patternDropdownExpanded) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(expanded = patternDropdownExpanded, onDismissRequest = { patternDropdownExpanded = false }) {
                                            listOf("None", "Cyber Grid", "Neon Waves", "Geometric Mesh").forEach { pat -> DropdownMenuItem(text = { Text(pat) }, onClick = { selectedCardPattern = pat; patternDropdownExpanded = false }) }
                                        }
                                    }
                                }

                                item {
                                    Text(text = "Custom Background Image", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        val customImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                                            if (uri != null) {
                                                try {
                                                    val inputStream = context.contentResolver.openInputStream(uri)
                                                    val bmap = BitmapFactory.decodeStream(inputStream)
                                                    if (bmap != null) {
                                                        tempBitmapToCrop = bmap
                                                        showCropDialog = true
                                                    }
                                                } catch (e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                        Button(
                                            onClick = { customImageLauncher.launch("image/*") },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (selectedCardImageUri.isEmpty()) "Upload Image" else "Change Image")
                                        }
                                        if (selectedCardImageUri.isNotEmpty()) {
                                            TextButton(
                                                onClick = { selectedCardImageUri = "" },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Remove")
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
 
@Composable
fun CardProtocolLogo(protocol: String?, modifier: Modifier = Modifier) {
    if (protocol == null || protocol.lowercase(Locale.US) == "none") return
    
    Box(modifier = modifier.height(24.dp).width(38.dp), contentAlignment = Alignment.Center) {
        when (protocol.lowercase(Locale.US)) {
            "visa" -> {
                Text(
                    text = "VISA",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            "mastercard" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.height * 0.45f
                    val centerY = size.height / 2f
                    drawCircle(
                        color = Color(0xFFEB001B),
                        radius = radius,
                        center = Offset(size.width * 0.35f, centerY)
                    )
                    drawCircle(
                        color = Color(0xFFF79E1B).copy(alpha = 0.8f),
                        radius = radius,
                        center = Offset(size.width * 0.65f, centerY)
                    )
                }
            }
        }
    }
}
 
@Composable
fun CardIssuerLogo(issuer: String?, modifier: Modifier = Modifier) {
    if (issuer == null || issuer.lowercase(Locale.US) == "none") return
    
    Box(modifier = modifier.height(28.dp).width(45.dp), contentAlignment = Alignment.Center) {
        when (issuer.lowercase(Locale.US)) {
            "boc" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.height * 0.4f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = Color(0xFFB31F24), radius = radius, center = center)
                    val squareSize = radius * 0.4f
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(center.x - squareSize/2, center.y - squareSize/2),
                        size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x, center.y - radius * 0.8f),
                        end = Offset(center.x, center.y + radius * 0.8f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            "hsbc" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val w = size.width * 0.7f
                    val h = size.height * 0.6f
                    
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - w/2, cy - h/2)
                        lineTo(cx - w/6, cy)
                        lineTo(cx - w/2, cy + h/2)
                        close()
                        moveTo(cx + w/2, cy - h/2)
                        lineTo(cx + w/6, cy)
                        lineTo(cx + w/2, cy + h/2)
                        close()
                    }
                    drawPath(path, color = Color(0xFFDB0011))
                    
                    val centerPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - w/6, cy)
                        lineTo(cx, cy - h/2)
                        lineTo(cx + w/6, cy)
                        lineTo(cx, cy + h/2)
                        close()
                    }
                    drawPath(centerPath, color = Color.White)
                }
            }
            "chase" -> {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.height * 0.45f
                    
                    for (i in 0..3) {
                        drawContext.canvas.save()
                        drawContext.transform.rotate(degrees = i * 90f, pivot = Offset(cx, cy))
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx - r * 0.15f, cy - r * 0.85f)
                            lineTo(cx + r * 0.85f, cy - r * 0.85f)
                            lineTo(cx + r * 0.85f, cy - r * 0.15f)
                            lineTo(cx + r * 0.15f, cy - r * 0.15f)
                            close()
                        }
                        drawPath(path, color = Color(0xFF117ACA))
                        drawContext.canvas.restore()
                    }
                }
            }
            "citi" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height * 0.45f
                        val rx = size.width * 0.35f
                        val ry = size.height * 0.2f
                        drawArc(
                            color = Color(0xFFEB001B),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(cx - rx, cy - ry),
                            size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                    Text(
                        text = "citi",
                        color = Color(0xFF002D62),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
 
fun Modifier.rotate3DOnTouch(): Modifier = this.composed {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var shineOffsetX by remember { mutableStateOf(0f) }
    
    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedRotationY by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedShineOffsetX by animateFloatAsState(
        targetValue = shineOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val changes = event.changes
                    val isAnyPressed = changes.any { it.pressed }
                    if (isAnyPressed) {
                        val change = changes.first()
                        val width = size.width
                        val height = size.height
                        if (width > 0 && height > 0) {
                            val touchX = change.position.x
                            val touchY = change.position.y
                            val maxRotation = 10f
                            val normX = ((touchX / width.toFloat()) - 0.5f).coerceIn(-0.5f, 0.5f) * 2f
                            val normY = ((touchY / height.toFloat()) - 0.5f).coerceIn(-0.5f, 0.5f) * 2f
                            rotationX = -normY * maxRotation
                            rotationY = normX * maxRotation
                            shineOffsetX = normX * width.toFloat()
                        }
                    } else {
                        rotationX = 0f
                        rotationY = 0f
                        shineOffsetX = 0f
                    }
                }
            }
        }
        .graphicsLayer {
            this.rotationX = animatedRotationX
            this.rotationY = animatedRotationY
            cameraDistance = 16f * density
        }
        .drawWithContent {
            drawContent()
            if (animatedRotationX != 0f || animatedRotationY != 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.0f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        start = Offset(animatedShineOffsetX - size.width * 0.4f, 0f),
                        end = Offset(animatedShineOffsetX + size.width * 0.4f, size.height)
                    ),
                    blendMode = BlendMode.SrcOver
                )
            }
        }
}

@Composable
fun rememberCardImagePainter(filePath: String?): ImageBitmap? {
    if (filePath.isNullOrEmpty()) return null
    return remember(filePath) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun saveAndResizeImage(inputStream: java.io.InputStream, targetFile: java.io.File): Boolean {
    return try {
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) return false
        
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        
        val maxW = 800
        val maxH = 500
        var inSampleSize = 1
        val srcWidth = options.outWidth
        val srcHeight = options.outHeight
        if (srcWidth > maxW || srcHeight > maxH) {
            val halfWidth = srcWidth / 2
            val halfHeight = srcHeight / 2
            while ((halfWidth / inSampleSize) >= maxW && (halfHeight / inSampleSize) >= maxH) {
                inSampleSize *= 2
            }
        }
        
        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = this@apply.inSampleSize
        }
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return false
        
        val finalBitmap = if (bitmap.width > maxW || bitmap.height > maxH) {
            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, maxW, maxH, true)
            if (scaled != bitmap) {
                bitmap.recycle()
            }
            scaled
        } else {
            bitmap
        }
        
        java.io.FileOutputStream(targetFile).use { out ->
            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        }
        finalBitmap.recycle()
        true
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
}

fun saveUriToInternalStorage(context: android.content.Context, uri: Uri, fileName: String): String? {
    android.util.Log.d("VIBE_DEBUG", "saveUriToInternalStorage started for URI: $uri")
    val targetDir = File(context.filesDir, "card_backgrounds").apply { mkdirs() }
    val targetFile = File(targetDir, fileName)
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            if (saveAndResizeImage(input, targetFile)) {
                android.util.Log.d("VIBE_DEBUG", "Successfully copied and resized via contentResolver stream to ${targetFile.absolutePath}")
                return targetFile.absolutePath
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("VIBE_DEBUG", "Failed to open resolver stream, trying fallbacks...", e)
        
        // Fallback 1: Query MediaStore _data column
        try {
            var targetUri = uri
            val pathStr = uri.toString()
            if (pathStr.contains("com.android.providers.media.documents/document/image")) {
                val id = pathStr.substringAfter("image%3A").substringAfter("image:")
                targetUri = Uri.parse("content://media/external/images/media/$id")
                android.util.Log.d("VIBE_DEBUG", "Fallback 1: Translated document URI to MediaStore URI: $targetUri")
            }
            val projection = arrayOf("_data")
            context.contentResolver.query(targetUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex("_data")
                    if (columnIndex != -1) {
                        val filePath = cursor.getString(columnIndex)
                        android.util.Log.d("VIBE_DEBUG", "Fallback 1: Resolved _data path: $filePath")
                        if (!filePath.isNullOrEmpty()) {
                            val possibleFile = File(filePath)
                            if (possibleFile.exists()) {
                                possibleFile.inputStream().use { input ->
                                    if (saveAndResizeImage(input, targetFile)) {
                                        android.util.Log.d("VIBE_DEBUG", "Fallback 1: Successfully copied and resized to ${targetFile.absolutePath}")
                                        return targetFile.absolutePath
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (cursorEx: Exception) {
            android.util.Log.e("VIBE_DEBUG", "Fallback 1 failed", cursorEx)
        }

        // Fallback 2: Handle primary: / primary%3A emulator document URIs
        try {
            val pathStr = uri.toString()
            android.util.Log.d("VIBE_DEBUG", "Fallback 2: Checking pathStr: $pathStr")
            if (pathStr.contains("primary%3A") || pathStr.contains("primary:")) {
                val relativePath = Uri.decode(pathStr.substringAfter("primary%3A").substringAfter("primary:"))
                val possibleFile = File("/storage/emulated/0/$relativePath")
                android.util.Log.d("VIBE_DEBUG", "Fallback 2: Checking file: ${possibleFile.absolutePath} exists = ${possibleFile.exists()}")
                if (possibleFile.exists()) {
                    possibleFile.inputStream().use { input ->
                        if (saveAndResizeImage(input, targetFile)) {
                            android.util.Log.d("VIBE_DEBUG", "Fallback 2: Successfully copied and resized to ${targetFile.absolutePath}")
                            return targetFile.absolutePath
                        }
                    }
                }
            }
        } catch (fallbackEx: Exception) {
            android.util.Log.e("VIBE_DEBUG", "Fallback 2 failed", fallbackEx)
        }

        // Fallback 3: Query display name and check standard emulator folders
        try {
            val projection = arrayOf("_display_name")
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex("_display_name")
                    if (nameIndex != -1) {
                        val displayName = cursor.getString(nameIndex)
                        android.util.Log.d("VIBE_DEBUG", "Fallback 3: Resolved _display_name: $displayName")
                        if (!displayName.isNullOrEmpty()) {
                            // Try Fallback 4: Query MediaStore by display name for a valid content URI
                            try {
                                val mediaProjection = arrayOf("_id")
                                val selection = "_display_name = ?"
                                val selectionArgs = arrayOf(displayName)
                                context.contentResolver.query(
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    mediaProjection,
                                    selection,
                                    selectionArgs,
                                    null
                                )?.use { mediaCursor ->
                                    if (mediaCursor.moveToFirst()) {
                                        val idIndex = mediaCursor.getColumnIndex("_id")
                                        if (idIndex != -1) {
                                            val id = mediaCursor.getLong(idIndex)
                                            val mediaUri = Uri.parse("content://media/external/images/media/$id")
                                            android.util.Log.d("VIBE_DEBUG", "Fallback 4: Found MediaStore URI for name: $mediaUri")
                                            context.contentResolver.openInputStream(mediaUri)?.use { input ->
                                                if (saveAndResizeImage(input, targetFile)) {
                                                    android.util.Log.d("VIBE_DEBUG", "Fallback 4: Successfully copied and resized to ${targetFile.absolutePath}")
                                                    return targetFile.absolutePath
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (mediaEx: Exception) {
                                android.util.Log.e("VIBE_DEBUG", "Fallback 4 failed", mediaEx)
                            }

                            // Fallback 3 filesystem check (fails under scoped storage but kept as raw legacy)
                            val possiblePaths = arrayOf(
                                "/storage/emulated/0/Download/$displayName",
                                "/storage/emulated/0/Pictures/$displayName",
                                "/storage/emulated/0/DCIM/$displayName",
                                "/storage/emulated/0/$displayName"
                            )
                            for (p in possiblePaths) {
                                val possibleFile = File(p)
                                android.util.Log.d("VIBE_DEBUG", "Fallback 3: Checking path: $p exists = ${possibleFile.exists()}")
                                if (possibleFile.exists()) {
                                    possibleFile.inputStream().use { input ->
                                        if (saveAndResizeImage(input, targetFile)) {
                                            android.util.Log.d("VIBE_DEBUG", "Fallback 3: Successfully copied and resized to ${targetFile.absolutePath}")
                                            return targetFile.absolutePath
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (nameEx: Exception) {
            android.util.Log.e("VIBE_DEBUG", "Fallback 3 failed", nameEx)
        }
    }
    return null
}

fun cropAndSaveCardBackground(
    sourceBitmap: android.graphics.Bitmap,
    rotation: Int,
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    targetFile: java.io.File
): Boolean {
    return try {
        val matrix = android.graphics.Matrix().apply {
            if (rotation != 0) {
                postRotate(rotation.toFloat())
            }
        }
        val rotated = android.graphics.Bitmap.createBitmap(
            sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true
        )
        
        val W = rotated.width.toFloat()
        val H = rotated.height.toFloat()
        val targetAspect = 1.6f
        
        val cropW: Float
        val cropH: Float
        if (W / H > targetAspect) {
            cropH = H / zoom
            cropW = cropH * targetAspect
        } else {
            cropW = W / zoom
            cropH = cropW / targetAspect
        }
        
        val maxShiftX = (W - cropW) / 2f
        val maxShiftY = (H - cropH) / 2f
        
        val centerX = W / 2f + offsetX * maxShiftX
        val centerY = H / 2f + offsetY * maxShiftY
        
        val startX = (centerX - cropW / 2f).coerceIn(0f, W - cropW)
        val startY = (centerY - cropH / 2f).coerceIn(0f, H - cropH)
        
        val cropped = android.graphics.Bitmap.createBitmap(
            rotated,
            startX.toInt(),
            startY.toInt(),
            cropW.toInt(),
            cropH.toInt()
        )
        
        val scaled = android.graphics.Bitmap.createScaledBitmap(cropped, 800, 500, true)
        
        java.io.FileOutputStream(targetFile).use { out ->
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        }
        
        if (rotated != sourceBitmap) rotated.recycle()
        cropped.recycle()
        scaled.recycle()
        true
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
}

fun Modifier.drawCardBackground(
    bitmap: ImageBitmap?,
    themeName: String?,
    patternName: String?,
    isDark: Boolean
): Modifier = this.drawBehind {
    // 1. Draw theme gradient
    val gradient = getCardGradient(themeName, isDark)
    drawRect(brush = gradient)
    
    // 2. Draw custom background image
    if (bitmap != null) {
        drawImage(
            image = bitmap,
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
    
    // 3. Draw pattern overlays
    if (patternName != null && patternName.lowercase(Locale.US) != "none") {
        when (patternName.lowercase(Locale.US)) {
            "cyber grid" -> {
                val step = 12.dp.toPx()
                val strokeWidth = 0.8f.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                    y += step
                }
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                    x += step
                }
            }
            "neon waves" -> {
                val wavePath1 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    cubicTo(
                        size.width * 0.25f, size.height * 0.5f,
                        size.width * 0.75f, size.height * 0.9f,
                        size.width, size.height * 0.6f
                    )
                }
                drawPath(
                    path = wavePath1,
                    color = Color.White.copy(alpha = 0.12f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                
                val wavePath2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.4f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.7f,
                        size.width * 0.7f, size.height * 0.3f,
                        size.width, size.height * 0.5f
                    )
                }
                drawPath(
                    path = wavePath2,
                    color = Color.White.copy(alpha = 0.1f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f.dp.toPx())
                )
            }
            "geometric mesh" -> {
                val w = size.width
                val h = size.height
                val poly1 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.6f, 0f)
                    lineTo(w * 0.3f, h * 0.8f)
                    close()
                }
                drawPath(path = poly1, color = Color.White.copy(alpha = 0.05f))
                
                val poly2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w, h)
                    lineTo(w * 0.4f, h)
                    lineTo(w * 0.7f, h * 0.2f)
                    close()
                }
                drawPath(path = poly2, color = Color.White.copy(alpha = 0.06f))
                
                val poly3 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w, 0f)
                    lineTo(w * 0.5f, h * 0.5f)
                    lineTo(w, h * 0.6f)
                    close()
                }
                drawPath(path = poly3, color = Color.White.copy(alpha = 0.04f))
            }
        }
    }
}

fun createTestBgIfNeeded(context: android.content.Context) {
    val testFile = File(context.filesDir, "card_backgrounds/test_bg.png")
    if (!testFile.exists()) {
        try {
            val targetDir = File(context.filesDir, "card_backgrounds").apply { mkdirs() }
            val bitmap = android.graphics.Bitmap.createBitmap(800, 500, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint()
            val gradient = android.graphics.LinearGradient(
                0f, 0f, 800f, 500f,
                android.graphics.Color.parseColor("#FF0F7B"),
                android.graphics.Color.parseColor("#F89B29"),
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, 800f, 500f, paint)
            java.io.FileOutputStream(testFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}


fun createSampleCardBitmap(context: android.content.Context): android.graphics.Bitmap {
    val sampleFile = File(context.filesDir, "card_backgrounds/sample_card_bg.png")
    if (sampleFile.exists()) {
        val bmp = BitmapFactory.decodeFile(sampleFile.absolutePath)
        if (bmp != null) return bmp
    }
    createTestBgIfNeeded(context)
    val fallbackFile = File(context.filesDir, "card_backgrounds/sample_card_bg.png")
    if (fallbackFile.exists()) {
        val bmp = BitmapFactory.decodeFile(fallbackFile.absolutePath)
        if (bmp != null) return bmp
    }
    val bitmap = android.graphics.Bitmap.createBitmap(800, 500, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 800f, 500f,
            android.graphics.Color.parseColor("#1A2980"),
            android.graphics.Color.parseColor("#26D0CE"),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, 800f, 500f, paint)
    return bitmap
}

@Composable
fun CreditCardWalletCarousel(
    creditCards: List<AccountEntity>,
    onEditCard: (AccountEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Physical Card Wallet 💳",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${creditCards.size} Cards Active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(creditCards, key = { it.id }) { card ->
                val previewCardImage = rememberCardImagePainter(card.cardImageUri ?: "")
                val last4 = card.cardLast4 ?: (card.id + 4000).toString()

                val paymentDeadlineDay = card.paymentDeadline ?: 10
                val today = remember { LocalDate.now() }
                val dueLocalDate = remember(today, paymentDeadlineDay) {
                    if (today.dayOfMonth <= paymentDeadlineDay) {
                        today.withDayOfMonth(paymentDeadlineDay)
                    } else {
                        today.plusMonths(1).withDayOfMonth(paymentDeadlineDay)
                    }
                }
                val daysRemaining = remember(today, dueLocalDate) {
                    ChronoUnit.DAYS.between(today, dueLocalDate).toInt()
                }
                val dueText = when {
                    daysRemaining == 0 -> "Due Today"
                    daysRemaining == 1 -> "Due Tomorrow"
                    else -> "Due in ${daysRemaining}d"
                }
                val dueBadgeColor = if (daysRemaining <= 3) Color(0xFFFFB3AD) else Color(0xFFA8C7FA)

                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .height(170.dp)
                        .rotate3DOnTouch()
                        .clickable { onEditCard(card) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawCardBackground(previewCardImage, card.cardTheme ?: "default", card.cardPattern ?: "none", isDark)
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (card.cardIssuer != null && card.cardIssuer != "none") {
                                        CardIssuerLogo(issuer = card.cardIssuer, modifier = Modifier.padding(end = 8.dp))
                                    }
                                    Text(
                                        text = card.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { onEditCard(card) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Card",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•••• •••• •••• $last4",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )
                                if (card.cardProtocol != null && card.cardProtocol != "none") {
                                    CardProtocolLogo(protocol = card.cardProtocol)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = dueBadgeColor.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, dueBadgeColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = dueText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Debt Balance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = String.format(Locale.US, "$%,.2f", card.balance),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveAccountListItem(
    account: AccountEntity,
    transactions: List<TransactionEntity>,
    cashbackRules: Map<String, Double>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCC = account.type == AccountType.CC
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Due countdown logic for Credit Cards
    val paymentDeadlineDay = account.paymentDeadline ?: 10
    val today = remember { LocalDate.now() }
    val dueLocalDate = remember(today, paymentDeadlineDay) {
        if (today.dayOfMonth <= paymentDeadlineDay) {
            today.withDayOfMonth(paymentDeadlineDay)
        } else {
            today.plusMonths(1).withDayOfMonth(paymentDeadlineDay)
        }
    }
    val daysRemaining = remember(today, dueLocalDate) {
        ChronoUnit.DAYS.between(today, dueLocalDate).toInt()
    }

    val dueText = if (isCC) {
        when {
            daysRemaining == 0 -> "Due Today"
            daysRemaining == 1 -> "Due Tomorrow"
            else -> "Due in ${daysRemaining}d"
        }
    } else null

    val dueBadgeColor = if (daysRemaining <= 3) {
        if (isDark) Color(0xFFFFB3AD) else Color(0xFFD93025)
    } else {
        if (isDark) Color(0xFFA8C7FA) else Color(0xFF174EA6)
    }

    val typeLabel = when (account.type) {
        AccountType.CC -> "Credit Card"
        AccountType.CASH -> "Cash"
        else -> "Bank Account"
    }

    val last4 = account.cardLast4 ?: (account.id + 4000).toString()

    var isPressed by remember { mutableStateOf(false) }
    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "accountItemTransform"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = itemScale
                scaleY = itemScale
            }
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            .clickable {
                isPressed = true
                onEditClick()
            },
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom Uploaded Image Avatar or Default Icon Container
            val context = LocalContext.current
            val customImageBitmap = remember(account.cardImageUri) {
                account.cardImageUri?.let { uriStr ->
                    try {
                        if (uriStr.equals("sample", ignoreCase = true)) {
                            createSampleCardBitmap(context)
                        } else {
                            val file = File(uriStr)
                            if (file.exists()) {
                                BitmapFactory.decodeFile(file.absolutePath)
                            } else null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (customImageBitmap != null) {
                val ratioKey = account.customImageAspectRatio ?: "1.586:1"
                val (imgWidth, imgHeight, imgShape) = remember(ratioKey) {
                    when (ratioKey) {
                        "1.586:1", "CARD" -> Triple(58.dp, 36.dp, RoundedCornerShape(8.dp))   // 1.586:1 Credit Card ratio
                        "16:9", "WIDE"    -> Triple(64.dp, 36.dp, RoundedCornerShape(8.dp))   // 16:9 Widescreen ratio
                        "4:3"             -> Triple(52.dp, 39.dp, RoundedCornerShape(10.dp))  // 4:3 Photo ratio
                        "2.35:1"          -> Triple(70.dp, 30.dp, RoundedCornerShape(6.dp))   // 2.35:1 Banner ratio
                        "1:1", "SQUARE"   -> Triple(48.dp, 48.dp, RoundedCornerShape(12.dp))  // 1:1 Square ratio
                        "CIRCLE"          -> Triple(48.dp, 48.dp, CircleShape)              // 1:1 Circle ratio
                        else              -> Triple(58.dp, 36.dp, RoundedCornerShape(8.dp))   // Default Card ratio
                    }
                }
                Surface(
                    modifier = Modifier.size(width = imgWidth, height = imgHeight),
                    shape = imgShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Image(
                        bitmap = customImageBitmap.asImageBitmap(),
                        contentDescription = account.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Fallback Prominent Avatar Container (52dp)
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (account.cardIssuer != null && account.cardIssuer != "none") {
                            CardIssuerLogo(
                                issuer = account.cardIssuer,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            val accountIcon = when (account.type) {
                                AccountType.CC -> Icons.Default.CreditCard
                                AccountType.CASH -> Icons.Default.Savings
                                else -> Icons.Default.AccountBalance
                            }
                            Icon(
                                imageVector = accountIcon,
                                contentDescription = account.name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // Material 3 Label & Supporting Text Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Headline Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (account.cardProtocol != null && account.cardProtocol != "none") {
                        CardProtocolLogo(
                            protocol = account.cardProtocol,
                            modifier = Modifier.height(14.dp)
                        )
                    }
                }

                // M3 Supporting Text Line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "•••• $last4",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (dueText != null) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = dueBadgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, dueBadgeColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = dueText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = dueBadgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Card Discount Perks Badge Summary
                if (isCC) {
                    val foodRule = remember(account.id) { com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(account.id, "Food") }
                    val transportRule = remember(account.id) { com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(account.id, "Transport") }
                    val shoppingRule = remember(account.id) { com.example.vibefinance.data.InMemoryDatabase.getCashbackRule(account.id, "Shopping") }

                    val perkTags = mutableListOf<String>()
                    if (foodRule > 0.0) perkTags.add("Food ${foodRule.toInt()}%")
                    if (transportRule > 0.0) perkTags.add("Transport ${transportRule.toInt()}%")
                    if (shoppingRule > 0.0) perkTags.add("Shop ${shoppingRule.toInt()}%")

                    if (perkTags.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "🎁 ${perkTags.joinToString(" • ")}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Monthly Spend Threshold (消費門檻) Progress Box
                    val allTransactions by com.example.vibefinance.data.InMemoryDatabase.transactions.collectAsStateWithLifecycle()
                    val threshold = account.minSpendThreshold ?: 0.0

                    if (threshold > 0.0) {
                        val currentMonthSpend = remember(allTransactions, account.id) {
                            val cal = java.util.Calendar.getInstance()
                            val currMonth = cal.get(java.util.Calendar.MONTH)
                            val currYear = cal.get(java.util.Calendar.YEAR)

                            allTransactions.filter { tx ->
                                tx.accountId == account.id && tx.toAccountId == null && tx.amount > 0.0
                            }.filter { tx ->
                                val txCal = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                                txCal.get(java.util.Calendar.MONTH) == currMonth && txCal.get(java.util.Calendar.YEAR) == currYear
                            }.sumOf { it.amount }
                        }

                        val progress = (currentMonthSpend / threshold).coerceIn(0.0, 1.0).toFloat()
                        val isThresholdMet = currentMonthSpend >= threshold
                        val remaining = (threshold - currentMonthSpend).coerceAtLeast(0.0)

                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "spendThresholdProgress"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isThresholdMet) Color(0xFF4CAF50).copy(alpha = 0.12f) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (isThresholdMet) Color(0xFF4CAF50).copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isThresholdMet) "🎉 門檻已達標 (Threshold Unlocked)" else "🎯 當月消費門檻 (Spend Threshold)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isThresholdMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = String.format(Locale.US, "$%,.0f / $%,.0f", currentMonthSpend, threshold),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isThresholdMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedProgress)
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isThresholdMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary)
                                    )
                                }

                                if (!isThresholdMet) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format(Locale.US, "還差 $%,.2f 即可解鎖高額現金回饋", remaining),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    // Linked Discount Shops & Aspect Box for this Card
                    val discountShops by com.example.vibefinance.data.InMemoryDatabase.discountShops.collectAsStateWithLifecycle()
                    val linkedShops = remember(discountShops, account.id) {
                        discountShops.filter { shop ->
                            shop.offers.any { offer -> offer.accountId == account.id }
                        }
                    }
                    var showAddShopModal by remember { mutableStateOf(false) }
                    var editingShopState by remember { mutableStateOf<com.example.vibefinance.data.entity.DiscountShop?>(null) }
                    var isShopsExpanded by remember { mutableStateOf(false) }

                    val displayedShops = remember(linkedShops, isShopsExpanded) {
                        if (isShopsExpanded || linkedShops.size <= 2) linkedShops else linkedShops.take(2)
                    }

                    val chevronRotation by animateFloatAsState(
                        targetValue = if (isShopsExpanded) 180f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "shopsChevronRotation"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏬 Linked Shops (${linkedShops.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(
                                    onClick = { showAddShopModal = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("+ Bind Shop", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (linkedShops.isEmpty()) {
                                Text(
                                    text = "No shops added yet. Tap '+ Bind Shop' to link shops for Radar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    displayedShops.forEach { shop ->
                                        val offer = shop.offers.find { it.accountId == account.id }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { editingShopState = shop }
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "📍 ${shop.name}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "(${shop.aspect})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "${offer?.discountRate ?: 5.0}% OFF",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Shop",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (linkedShops.size > 2) {
                                        TextButton(
                                            onClick = { isShopsExpanded = !isShopsExpanded },
                                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                            modifier = Modifier.height(20.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isShopsExpanded) "Show Less" else "+${linkedShops.size - 2} More Shops",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .graphicsLayer { rotationZ = chevronRotation + 90f }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showAddShopModal) {
                        AddShopForCardModal(
                            account = account,
                            onDismiss = { showAddShopModal = false }
                        )
                    }

                    if (editingShopState != null) {
                        EditDiscountShopModal(
                            shop = editingShopState!!,
                            account = account,
                            onDismiss = { editingShopState = null }
                        )
                    }
                }
            }

            // Material 3 Trailing Amount Label, Utilization Progress & Edit Button
            val creditLimit = account.creditLimit ?: 0.0
            val hasCreditLimit = isCC && creditLimit > 0.0
            val utilizationRatio = if (hasCreditLimit) (account.balance / creditLimit).coerceIn(0.0, 1.0).toFloat() else 0f
            val utilizationPercent = (utilizationRatio * 100).toInt()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasCreditLimit) {
                    val indicatorColor = when {
                        utilizationRatio > 0.70f -> MaterialTheme.colorScheme.error
                        utilizationRatio > 0.30f -> Color(0xFFE37400)
                        else -> Color(0xFF137333)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(38.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { utilizationRatio },
                            modifier = Modifier.fillMaxSize(),
                            color = indicatorColor,
                            trackColor = indicatorColor.copy(alpha = 0.18f),
                            strokeWidth = 3.5.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "$utilizationPercent%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = indicatorColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("$%,.2f", account.balance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCC && account.balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    if (hasCreditLimit) {
                        Text(
                            text = String.format(Locale.US, "Limit $%,.0f", creditLimit),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Account",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveImageCropDialog(
    bitmap: android.graphics.Bitmap,
    initialAspectRatio: String,
    onDismiss: () -> Unit,
    onConfirm: (android.graphics.Bitmap, String) -> Unit
) {
    var selectedRatio by remember { mutableStateOf(initialAspectRatio) }
    var scale by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val ratioOptions = listOf(
        "1.586:1" to "💳 1.586:1 (Card)",
        "16:9"    to "📺 16:9 (Wide)",
        "4:3"     to "📷 4:3 (Photo)",
        "1:1"     to "⬛ 1:1 (Square)",
        "2.35:1"  to "🎬 2.35:1 (Banner)"
    )

    val aspectRatioFloat = remember(selectedRatio) {
        when (selectedRatio) {
            "1.586:1" -> 1.586f
            "16:9"    -> 16f / 9f
            "4:3"     -> 4f / 3f
            "2.35:1"  -> 2.35f
            "1:1"     -> 1.0f
            else      -> 1.586f
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Position & Scale Card Image",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Drag to move image. Select aspect ratio below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Interactive Crop Viewport Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    // Frame Box with dynamic aspect ratio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(aspectRatioFloat)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                                    offset = Offset(offset.x + pan.x, offset.y + pan.y)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                        )
                    }
                }

                // Ratio Selector Chips
                Text(
                    text = "Aspect Ratio Preference",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ratioOptions) { (key, label) ->
                        val isSelected = selectedRatio == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRatio = key },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cropped = createCroppedBitmap(bitmap, scale, offset, aspectRatioFloat)
                    onConfirm(cropped, selectedRatio)
                }
            ) {
                Text("Confirm & Use")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun createCroppedBitmap(
    source: android.graphics.Bitmap,
    scale: Float,
    offset: Offset,
    aspectRatio: Float
): android.graphics.Bitmap {
    val targetWidth = 600
    val targetHeight = (targetWidth / aspectRatio).toInt().coerceAtLeast(1)
    val result = android.graphics.Bitmap.createBitmap(targetWidth, targetHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
    val matrix = android.graphics.Matrix()

    val scaleX = targetWidth.toFloat() / source.width.toFloat()
    val scaleY = targetHeight.toFloat() / source.height.toFloat()
    val baseScale = maxOf(scaleX, scaleY)

    val finalScale = baseScale * scale
    val dx = (targetWidth - source.width * finalScale) / 2f + offset.x
    val dy = (targetHeight - source.height * finalScale) / 2f + offset.y

    matrix.setScale(finalScale, finalScale)
    matrix.postTranslate(dx, dy)

    canvas.drawBitmap(source, matrix, paint)
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopForCardModal(
    account: AccountEntity,
    onDismiss: () -> Unit
) {
    var shopNameText by remember { mutableStateOf("") }
    var aspectText by remember { mutableStateOf("Coffee & Cafe") }
    var discountRateText by remember { mutableStateOf("10.0") }
    var aspectDropdownExpanded by remember { mutableStateOf(false) }

    val currentLocation by com.example.vibefinance.util.GnssLocationManager.currentLocation.collectAsStateWithLifecycle()
    val savedAspects by com.example.vibefinance.data.InMemoryDatabase.savedAspects.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Bind Shop to ${account.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Add a real-world shop & discount rate % to display on GNSS Radar when near this location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = shopNameText,
                    onValueChange = { shopNameText = it },
                    label = { Text("Shop / Business Name") },
                    placeholder = { Text("e.g. Starbucks Reserve, Shell Gas") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = aspectDropdownExpanded,
                    onExpandedChange = { aspectDropdownExpanded = !aspectDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = aspectText,
                        onValueChange = {
                            aspectText = it
                            aspectDropdownExpanded = true
                        },
                        readOnly = false,
                        label = { Text("Aspect / Category (Type custom or select)") },
                        placeholder = { Text("e.g. Bakery, Books, Clothing") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aspectDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = aspectDropdownExpanded,
                        onDismissRequest = { aspectDropdownExpanded = false }
                    ) {
                        savedAspects.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    aspectText = item
                                    aspectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = discountRateText,
                    onValueChange = { discountRateText = it },
                    label = { Text("Card Discount Rate (%)") },
                    placeholder = { Text("e.g. 10.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "GNSS: ${currentLocation.locationName} (${String.format(java.util.Locale.US, "%.4f, %.4f", currentLocation.latitude, currentLocation.longitude)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopNameText.isNotBlank() && aspectText.isNotBlank()) {
                        com.example.vibefinance.data.InMemoryDatabase.saveAspect(aspectText)
                        val rate = discountRateText.toDoubleOrNull() ?: 5.0
                        val newOffer = com.example.vibefinance.data.entity.ShopDiscountOffer(
                            accountId = account.id,
                            discountRate = rate
                        )
                        val newShop = com.example.vibefinance.data.entity.DiscountShop(
                            id = System.currentTimeMillis(),
                            name = shopNameText.trim(),
                            aspect = aspectText.trim(),
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude,
                            offers = listOf(newOffer),
                            isUserCreated = true
                        )
                        com.example.vibefinance.data.InMemoryDatabase.insertDiscountShop(newShop)
                        onDismiss()
                    }
                }
            ) {
                Text("Save & Bind Shop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiscountShopModal(
    shop: com.example.vibefinance.data.entity.DiscountShop,
    account: AccountEntity,
    onDismiss: () -> Unit
) {
    val offer = remember(shop, account.id) { shop.offers.find { it.accountId == account.id } }
    var shopNameText by remember { mutableStateOf(shop.name) }
    var aspectText by remember { mutableStateOf(shop.aspect) }
    var discountRateText by remember { mutableStateOf((offer?.discountRate ?: 5.0).toString()) }
    var aspectDropdownExpanded by remember { mutableStateOf(false) }

    val savedAspects by com.example.vibefinance.data.InMemoryDatabase.savedAspects.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Edit Linked Shop",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        com.example.vibefinance.data.InMemoryDatabase.deleteDiscountShop(shop)
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Shop", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Modify shop details or discount rate for ${account.name}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = shopNameText,
                    onValueChange = { shopNameText = it },
                    label = { Text("Shop / Business Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = aspectDropdownExpanded,
                    onExpandedChange = { aspectDropdownExpanded = !aspectDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = aspectText,
                        onValueChange = {
                            aspectText = it
                            aspectDropdownExpanded = true
                        },
                        readOnly = false,
                        label = { Text("Aspect / Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aspectDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = aspectDropdownExpanded,
                        onDismissRequest = { aspectDropdownExpanded = false }
                    ) {
                        savedAspects.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    aspectText = item
                                    aspectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = discountRateText,
                    onValueChange = { discountRateText = it },
                    label = { Text("Card Discount Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopNameText.isNotBlank() && aspectText.isNotBlank()) {
                        val rate = discountRateText.toDoubleOrNull() ?: 5.0
                        val updatedOffers = shop.offers.map { off ->
                            if (off.accountId == account.id) off.copy(discountRate = rate) else off
                        }
                        val updatedShop = shop.copy(
                            name = shopNameText.trim(),
                            aspect = aspectText.trim(),
                            offers = updatedOffers
                        )
                        com.example.vibefinance.data.InMemoryDatabase.saveAspect(aspectText)
                        com.example.vibefinance.data.InMemoryDatabase.updateDiscountShop(updatedShop)
                        onDismiss()
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



