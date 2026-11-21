@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)

package com.example.vibefinance.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.example.vibefinance.R
import com.example.vibefinance.theme.VibeMotion
import com.example.vibefinance.ui.components.VibeFinanceIcon
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.util.VibeHaptics

val LocalMainWindowWidth = compositionLocalOf<androidx.compose.ui.unit.Dp?> { null }

/** Official Material navigation suite type for the current window (bar, medium bar or wide rail). */
val LocalNavigationSuiteType = compositionLocalOf { NavigationSuiteType.ShortNavigationBarCompact }

/**
 * Google's adaptive rule from `NavigationSuiteScaffoldDefaults.navigationSuiteType`:
 * compact width -> short bar; compact height or tabletop -> medium short bar (icon beside label);
 * otherwise -> collapsed wide navigation rail. Sized from the frame constraints so windowed and
 * flex-display hosts follow the space the app actually has.
 */
@Composable
internal fun rememberNavigationSuiteType(widthDp: Float, heightDp: Float): NavigationSuiteType {
    val posture = currentWindowAdaptiveInfo().windowPosture
    return remember(widthDp, heightDp, posture) {
        NavigationSuiteScaffoldDefaults.navigationSuiteType(
            WindowAdaptiveInfo(WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp, heightDp), posture)
        )
    }
}

/**
 * Material 3 Expressive collapsed wide navigation rail for medium and expanded windows.
 * The 96dp column follows the M3E collapsed rail spec and anchors the global app controls
 * (Logo, FAB, `WideNavigationRailItem` tabs, utility dock).
 * Preserves 100% stable horizontal canvas space for the dual-column Bento dashboard.
 */
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
        val suiteType = rememberNavigationSuiteType(maxWidth.value, maxHeight.value)
        val useRail = suiteType == NavigationSuiteType.WideNavigationRailCollapsed
        val view = LocalView.current
        val triggers = remember { mutableStateMapOf<TabItem, Int>() }

        // Root for M3 Expressive container transforms between pages.
        com.example.vibefinance.ui.motion.ExpressiveSharedTransitionRoot {
        CompositionLocalProvider(LocalMainWindowWidth provides maxWidth, LocalNavigationSuiteType provides suiteType) {
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

                    // The rail surface stays edge-to-edge behind the system bars; only its
                    // controls are inset, matching WideNavigationRailDefaults.windowInsets.
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(96.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                      BoxWithConstraints(
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start))
                      ) {
                        // Three zones: header (logo + FAB) on top, destinations centred in the
                        // remaining height, utility dock at the bottom. heightIn(min) lets the
                        // weighted spacers fill tall windows while short ones fall back to scrolling.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .heightIn(min = maxHeight)
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

                            Spacer(Modifier.weight(1f).heightIn(min = 8.dp))

                            // --- NAVIGATION TABS (vertically centred group, 12dp rhythm) ---
                            val tabs = listOf(
                                TabItem.HOME to Triple(R.string.nav_daily, Icons.Filled.Home, Icons.Outlined.Home),
                                TabItem.ACCOUNTS to Triple(R.string.nav_assets, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
                                TabItem.RECURRING to Triple(R.string.nav_recurring, Icons.Filled.AutoMode, Icons.Outlined.AutoMode),
                                TabItem.HISTORY to Triple(R.string.nav_history, Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
                            )
                            val minimalMotion = LocalExperience.current.motion == MotionLevel.MINIMAL

                            SharedTransitionLayout(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    tabs.forEach { (tab, info) ->
                                        val title = stringResource(info.first)
                                        WideNavigationRailItem(
                                            selected = selectedTab == tab,
                                            onClick = {
                                                triggers[tab] = (triggers[tab] ?: 0) + 1
                                                onSelect(tab)
                                            },
                                            colors = WideNavigationRailItemDefaults.colors(
                                                selectedIndicatorColor = Color.Transparent
                                            ),
                                            indicatorPadding = PaddingValues(0.dp),
                                            icon = {
                                                Box(
                                                    modifier = Modifier.size(56.dp, 32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AnimatedContent(
                                                        targetState = selectedTab == tab,
                                                        transitionSpec = {
                                                            fadeIn(
                                                                if (minimalMotion) snap()
                                                                else VibeMotion.fastEffects()
                                                            ) togetherWith fadeOut(
                                                                if (minimalMotion) snap()
                                                                else VibeMotion.fastEffects()
                                                            )
                                                        },
                                                        label = "RailIndicator_${tab.name}"
                                                    ) { isSelected ->
                                                        if (isSelected) {
                                                            val sharedPillModifier = if (minimalMotion) {
                                                                Modifier
                                                            } else {
                                                                Modifier.sharedBounds(
                                                                    sharedContentState = rememberSharedContentState("rail-active-indicator-pill"),
                                                                    animatedVisibilityScope = this,
                                                                    boundsTransform = { _, _ -> VibeMotion.defaultSpatial() },
                                                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                                                )
                                                            }
                                                            Box(
                                                                Modifier
                                                                    .then(sharedPillModifier)
                                                                    .size(56.dp, 32.dp)
                                                                    .background(
                                                                        MaterialTheme.colorScheme.secondaryContainer,
                                                                        CircleShape
                                                                    )
                                                                    .semantics(mergeDescendants = true) {}
                                                                    .testTag("RailActiveIndicatorPill")
                                                            )
                                                        } else {
                                                            Spacer(Modifier.size(56.dp, 32.dp))
                                                        }
                                                    }
                                                    BottomNavigationIcon(
                                                        tab = tab,
                                                        selected = selectedTab == tab,
                                                        title = title,
                                                        filledIcon = info.second,
                                                        outlinedIcon = info.third,
                                                        trigger = triggers[tab] ?: 0
                                                    )
                                                }
                                            },
                                            label = { Text(title, maxLines = 1) },
                                            railExpanded = false,
                                            modifier = Modifier.testTag("RailNav_${tab.name}")
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.weight(1f).heightIn(min = 16.dp))

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
                                            LoadingIndicator(
                                                modifier = Modifier.size(20.dp),
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
                                WideNavigationRailItem(
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
                                    railExpanded = false,
                                    modifier = Modifier.testTag("RailSettings")
                                )
                            }
                        }
                      }
                    }
                }

                // --- MAIN CONTENT CONTAINER (100% Stable Horizontal Space) ---
                if (useRail) Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // Keep the card below the status bar / cutout. The bottom edge still runs
                        // behind the navigation bar because pages already pad for it themselves.
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End))
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
}
