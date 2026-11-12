package com.example.vibefinance.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.example.vibefinance.R
import com.example.vibefinance.ui.components.VibeFinanceIcon
import com.example.vibefinance.util.VibeHaptics

val LocalMainWindowWidth = compositionLocalOf<androidx.compose.ui.unit.Dp?> { null }

/**
 * Material 3 & ImageToolbox/Morphe inspired Navigation Rail Hub for landscape and large screens.
 * Fixed 88dp width anchors the global app controls (Logo, FAB, 12dp-spaced tabs, utility dock).
 * Preserves 100% stable horizontal canvas space for the dual-column Bento dashboard.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun AdaptiveMainFrame(
    selectedTab: TabItem,
    onSelect: (TabItem) -> Unit,
    onAddTransaction: () -> Unit = {},
    onAddSubscription: (() -> Unit)? = null,
    onAddAccount: (() -> Unit)? = null,
    onSettings: () -> Unit = {},
    onTogglePrivacy: (() -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null,
    onTriggerSync: (() -> Unit)? = null,
    isPrivacyHidden: Boolean = false,
    isDarkTheme: Boolean = false,
    isSyncing: Boolean = false,
    content: @Composable (Boolean) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sizes = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        val useRail = sizes.widthSizeClass != WindowWidthSizeClass.Compact
        val view = LocalView.current
        val triggers = remember { mutableStateMapOf<TabItem, Int>() }

        CompositionLocalProvider(LocalMainWindowWidth provides maxWidth) {
            Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
                if (useRail) {
                    val railView = LocalView.current
                    val dialogWindow = generateSequence(railView) { it.parent as? android.view.View }
                        .filterIsInstance<DialogWindowProvider>().firstOrNull()?.window
                    val darkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f

                    // Match system bar theming
                    SideEffect {
                        dialogWindow?.let { window ->
                            window.navigationBarColor = android.graphics.Color.TRANSPARENT
                            if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
                            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                                isAppearanceLightStatusBars = !darkSurface
                                isAppearanceLightNavigationBars = !darkSurface
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(88.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .testTag("MainNavigationRail"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // --- TOP HEADER (App Logo & FAB per M3 Guidelines) ---
                            Surface(
                                modifier = Modifier
                                    .padding(top = 12.dp, bottom = 10.dp)
                                    .size(42.dp)
                                    .testTag("RailAppLogo"),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    VibeFinanceIcon(
                                        size = 28.dp,
                                        showBackground = false
                                    )
                                }
                            }

                            val (fabDescRes, fabAction) = when (selectedTab) {
                                TabItem.RECURRING -> Pair(
                                    R.string.btn_add_subscription,
                                    onAddSubscription ?: onAddTransaction
                                )
                                TabItem.ACCOUNTS -> Pair(
                                    R.string.btn_add_account,
                                    onAddAccount ?: onAddTransaction
                                )
                                else -> Pair(
                                    R.string.analytics_add_transaction,
                                    onAddTransaction
                                )
                            }

                            FloatingActionButton(
                                onClick = {
                                    VibeHaptics.selection(view)
                                    fabAction()
                                },
                                shape = RoundedCornerShape(18.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .testTag("RailAddTransaction")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(fabDescRes)
                                )
                            }

                            // --- NAVIGATION TABS (12dp gap per ImageToolbox & M3 rhythm) ---
                            val tabs = listOf(
                                TabItem.HOME to Triple(R.string.nav_daily, Icons.Filled.Home, Icons.Outlined.Home),
                                TabItem.ACCOUNTS to Triple(R.string.nav_assets, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
                                TabItem.RECURRING to Triple(R.string.nav_recurring, Icons.Filled.AutoMode, Icons.Outlined.AutoMode),
                                TabItem.HISTORY to Triple(R.string.nav_history, Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                tabs.forEach { (tab, info) ->
                                    val title = stringResource(info.first)
                                    NavigationRailItem(
                                        selected = selectedTab == tab,
                                        onClick = {
                                            triggers[tab] = (triggers[tab] ?: 0) + 1
                                            onSelect(tab)
                                        },
                                        icon = {
                                            BottomNavigationIcon(
                                                tab = tab,
                                                selected = selectedTab == tab,
                                                title = title,
                                                filledIcon = info.second,
                                                outlinedIcon = info.third,
                                                trigger = triggers[tab] ?: 0
                                            )
                                        },
                                        label = { Text(title, maxLines = 1) },
                                        alwaysShowLabel = true,
                                        modifier = Modifier.testTag("RailNav_${tab.name}")
                                    )
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            // --- BOTTOM UTILITY HUB (Centralized Global Controls) ---
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Privacy Mode Toggle
                                if (onTogglePrivacy != null) {
                                    IconButton(
                                        onClick = {
                                            VibeHaptics.selection(view)
                                            onTogglePrivacy()
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("RailTogglePrivacy")
                                    ) {
                                        Icon(
                                            imageVector = if (isPrivacyHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = stringResource(if (isPrivacyHidden) R.string.privacy_show_amounts else R.string.privacy_hide_amounts),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Dark / Light Mode Toggle
                                if (onToggleTheme != null) {
                                    IconButton(
                                        onClick = {
                                            VibeHaptics.selection(view)
                                            onToggleTheme()
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("RailToggleTheme")
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = stringResource(if (isDarkTheme) R.string.theme_light else R.string.theme_dark),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Google Drive Sync (Optional)
                                if (onTriggerSync != null) {
                                    IconButton(
                                        onClick = {
                                            if (!isSyncing) {
                                                VibeHaptics.lightTick(view)
                                                onTriggerSync()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("RailGoogleDriveSync")
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = stringResource(R.string.google_drive_sync_title),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                // Settings Item
                                NavigationRailItem(
                                    selected = false,
                                    onClick = {
                                        VibeHaptics.selection(view)
                                        onSettings()
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = stringResource(R.string.settings_title)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.settings_title), maxLines = 1) },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("RailSettings")
                                )
                            }
                        }
                    }
                }

                // --- MAIN CONTENT CONTAINER (100% Stable Horizontal Space) ---
                if (useRail) Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp)
                        .testTag("MainContentContainer"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) { content(true) }
                else Box(Modifier.weight(1f).fillMaxHeight()) { content(false) }
            }
        }
    }
}
