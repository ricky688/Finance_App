@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.components.AppButtonDefaults
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.CompletePressOutlinedButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.postPressIconBounce
import com.example.vibefinance.ui.motion.LocalIsSharedTargetPage
import com.example.vibefinance.ui.motion.predictiveBackPage
import com.example.vibefinance.ui.motion.rememberPredictiveBackPage
import com.example.vibefinance.ui.motion.sharedTransitionBounds
import com.example.vibefinance.ui.motion.sharedTransitionElement
import com.example.vibefinance.util.GnssLocationManager
import com.example.vibefinance.util.VibeHaptics
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Where an intercepted transaction happened, resolved once for the detail and edit dialogs. */
internal data class TransactionLocationInfo(
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val startStationName: String?,
    val endStationName: String?,
    val startLatitude: Double?,
    val startLongitude: Double?,
)

internal fun resolveTransactionLocation(context: Context, tx: TransactionEntity): TransactionLocationInfo? {
    val lat = tx.latitude
    val lng = tx.longitude
    if (lat == null || lng == null) return null
    val rawName = tx.locationName
    val isRoute = rawName?.contains(" ➔ ") == true
    val startStationName = tx.startLocationName?.takeIf { it.isNotBlank() }
        ?: if (isRoute) rawName?.substringBefore(" ➔ ")?.trim() else null
    val startStation = startStationName?.let {
        com.example.vibefinance.transit.MtrStationRegistry.getStationByName(context, it)
    }
    return TransactionLocationInfo(
        latitude = lat,
        longitude = lng,
        label = rawName?.takeIf { it.isNotBlank() } ?: String.format(Locale.US, "%.5f, %.5f", lat, lng),
        startStationName = startStationName,
        endStationName = if (isRoute) rawName?.substringAfter(" ➔ ")?.trim() else null,
        startLatitude = tx.startLatitude ?: startStation?.latitude,
        startLongitude = tx.startLongitude ?: startStation?.longitude,
    )
}

internal fun resolveInitialOrFallbackLocation(context: Context, tx: TransactionEntity): TransactionLocationInfo {
    resolveTransactionLocation(context, tx)?.let { return it }
    val instant = GnssLocationManager.getInstantLocation(context)
    if (instant != null) {
        return TransactionLocationInfo(
            latitude = instant.latitude,
            longitude = instant.longitude,
            label = instant.locationName,
            startStationName = null,
            endStationName = null,
            startLatitude = null,
            startLongitude = null,
        )
    }
    val preset = GnssLocationManager.PRESETS.first()
    return TransactionLocationInfo(
        latitude = preset.latitude,
        longitude = preset.longitude,
        label = preset.title,
        startStationName = null,
        endStationName = null,
        startLatitude = null,
        startLongitude = null,
    )
}

internal fun TransactionEntity.withRepositionedLocation(
    latitude: Double,
    longitude: Double,
    label: String,
): TransactionEntity {
    val cleanLabel = label.trim().ifBlank {
        String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
    }
    val isRoute = !startLocationName.isNullOrBlank() || locationName?.contains(" ➔ ") == true
    val finalLabel = if (isRoute) {
        val startName = startLocationName?.takeIf { it.isNotBlank() }
            ?: locationName?.substringBefore(" ➔ ")?.trim().orEmpty()
        if (cleanLabel.contains(" ➔ ") || startName.isBlank()) cleanLabel else "$startName ➔ $cleanLabel"
    } else {
        cleanLabel
    }
    return copy(
        latitude = latitude,
        longitude = longitude,
        locationName = finalLabel,
    )
}

@Composable
internal fun rememberTransactionLocation(tx: TransactionEntity): TransactionLocationInfo? {
    val context = LocalContext.current
    return remember(tx.latitude, tx.longitude, tx.locationName, tx.startLocationName, tx.startLatitude, tx.startLongitude) {
        resolveTransactionLocation(context, tx)
    }
}

/**
 * Compact location card shown first in the History edit dialog, so the place captured when the
 * payment notification was intercepted is visible before editing the record.
 */
@Composable
internal fun EditTransactionLocationCard(
    location: TransactionLocationInfo,
    modifier: Modifier = Modifier,
    mapHeight: Dp = 140.dp,
    onOpenFullScreen: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth().sharedTransitionBounds(SharedTxKeys.LocationCard, remeasure = true)
            .testTag("EditTransactionLocation"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.tx_detail_location),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (onOpenFullScreen != null) {
                    CompletePressTextButton(
                        onClick = onOpenFullScreen,
                        shapes = AppButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("EditLocationOpenFullScreenButton")
                    ) {
                        Icon(
                            Icons.Default.OpenInFull,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp).postPressIconBounce()
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.tx_detail_fullscreen_map),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            SharedTransactionMap(
                location = location,
                modifier = Modifier.fillMaxWidth().height(mapHeight),
                interactive = false,
                onPreviewClick = onOpenFullScreen
            )
            Text(
                location.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Keys of the elements shared between the phone detail and edit dialogs and full-screen map. */
internal object SharedTxKeys {
    const val LocationCard = "tx-location-card"
    const val Map = "tx-map"
    const val Amount = "tx-amount"
    const val Category = "tx-category"
    const val DateTime = "tx-datetime"
    const val Account = "tx-account"
    const val Description = "tx-description"
}

/** Hands one map view between pages, so a shared-element transition moves it instead of reloading. */
internal class SharedMapHost {
    var location by mutableStateOf<TransactionLocationInfo?>(null)
    var interactive by mutableStateOf(false)
    var cornerRadius by mutableStateOf(28.dp)
    var onMapClick by mutableStateOf<((Double, Double) -> Unit)?>(null)
    var onPreviewClick by mutableStateOf<(() -> Unit)?>(null)
    val content: @Composable (Modifier) -> Unit = movableContentOf { modifier: Modifier ->
        location?.let {
            TransactionLocationMap(
                latitude = it.latitude,
                longitude = it.longitude,
                startLatitude = it.startLatitude,
                startLongitude = it.startLongitude,
                description = it.label,
                modifier = modifier,
                interactive = interactive,
                cornerRadius = cornerRadius,
                onMapClick = onMapClick,
                onPreviewClick = onPreviewClick,
            )
        }
    }
}

internal val LocalSharedMapHost = staticCompositionLocalOf<SharedMapHost?> { null }

/** True inside the phone transaction dialog, where the edit form is tightened to avoid scrolling. */
internal val LocalCompactTransactionForm = staticCompositionLocalOf { false }

/**
 * The transaction map. Inside the phone dialog's or inspector's shared-element transition the single
 * map view lives on the target page and moves between pages; elsewhere it is an ordinary map.
 */
@Composable
internal fun SharedTransactionMap(
    location: TransactionLocationInfo,
    modifier: Modifier,
    interactive: Boolean = false,
    cornerRadius: Dp = 28.dp,
    onMapClick: ((Double, Double) -> Unit)? = null,
    onPreviewClick: (() -> Unit)? = null,
) {
    val host = LocalSharedMapHost.current
    if (host == null) {
        TransactionLocationMap(
            latitude = location.latitude,
            longitude = location.longitude,
            startLatitude = location.startLatitude,
            startLongitude = location.startLongitude,
            description = location.label,
            modifier = modifier,
            interactive = interactive,
            cornerRadius = cornerRadius,
            onMapClick = onMapClick,
            onPreviewClick = onPreviewClick,
        )
        return
    }
    val isTarget = LocalIsSharedTargetPage.current
    if (isTarget) {
        host.location = location
        host.interactive = interactive
        host.cornerRadius = cornerRadius
        host.onMapClick = onMapClick
        host.onPreviewClick = onPreviewClick
    }
    Box(modifier.sharedTransitionElement(SharedTxKeys.Map)) {
        if (isTarget) host.content(Modifier.matchParentSize())
    }
}

/**
 * In-app interactive OpenStreetMap overlay for inspecting and repositioning transaction location
 * metadata. Fills the right-side [MainContentContainer] on wide screens (never overlaying the rail)
 * or covers the full phone screen when [inContentContainer] is false.
 */
@Composable
internal fun TransactionFullScreenMapOverlay(
    transaction: TransactionEntity,
    inContentContainer: Boolean,
    onExit: () -> Unit,
    onSaveLocation: (latitude: Double, longitude: Double, label: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val backState = rememberPredictiveBackPage(enabled = true, onBack = onExit)

    val initialLocation = remember(
        transaction.id,
        transaction.latitude,
        transaction.longitude,
        transaction.locationName,
        transaction.startLocationName,
    ) {
        resolveInitialOrFallbackLocation(context, transaction)
    }

    var draftLat by remember(transaction.id) { mutableDoubleStateOf(initialLocation.latitude) }
    var draftLng by remember(transaction.id) { mutableDoubleStateOf(initialLocation.longitude) }
    var draftLabel by remember(transaction.id) {
        mutableStateOf(initialLocation.endStationName ?: initialLocation.label)
    }
    var resolvingAddress by remember { mutableStateOf(false) }
    var gpsLoading by remember { mutableStateOf(false) }
    var lookupGeneration by remember { mutableIntStateOf(0) }

    val cornerRadius = if (inContentContainer) 28.dp else 0.dp
    val draftLocation = remember(initialLocation, draftLat, draftLng, draftLabel) {
        initialLocation.copy(
            latitude = draftLat,
            longitude = draftLng,
            label = draftLabel.ifBlank { initialLocation.label },
        )
    }

    fun updateCoordinates(lat: Double, lng: Double, presetLabel: String? = null) {
        draftLat = lat
        draftLng = lng
        val generation = ++lookupGeneration
        if (presetLabel != null) {
            draftLabel = presetLabel
            resolvingAddress = false
            return
        }
        resolvingAddress = true
        scope.launch {
            val resolved = withContext(Dispatchers.IO) {
                GnssLocationManager.resolveAddress(context, lat, lng)
            }
            if (generation == lookupGeneration) {
                draftLabel = resolved?.takeIf { it.isNotBlank() }
                    ?: String.format(Locale.US, "%.5f, %.5f", lat, lng)
                resolvingAddress = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .predictiveBackPage(backState)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surface)
            .testTag("TransactionFullScreenMapOverlay")
    ) {
        SharedTransactionMap(
            location = draftLocation,
            modifier = Modifier.fillMaxSize(),
            interactive = true,
            cornerRadius = cornerRadius,
            onMapClick = { lat, lng ->
                VibeHaptics.lightTick(view)
                updateCoordinates(lat, lng)
            },
        )

        val safeOverlayInsets = if (inContentContainer) {
            Modifier
        } else {
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
        }

        // Top-left Exit button (always leaves without overwriting saved metadata).
        CompletePressFilledTonalButton(
            onClick = {
                VibeHaptics.selection(view)
                onExit()
            },
            shapes = AppButtonDefaults.shapes(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .then(safeOverlayInsets)
                .padding(start = 12.dp, top = 12.dp)
                .testTag("FullScreenMapExitButton")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.btn_close),
                modifier = Modifier.size(18.dp).postPressIconBounce()
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.btn_close), style = MaterialTheme.typography.labelLarge)
        }

        // Top-right Google Maps external redirection button (positioned below OSM attribution pill).
        CompletePressFilledTonalButton(
            onClick = {
                VibeHaptics.selection(view)
                val title = draftLabel.ifBlank { transaction.description.ifBlank { transaction.category } }
                val geoUri = Uri.parse("geo:$draftLat,$draftLng?q=$draftLat,$draftLng(${Uri.encode(title)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                runCatching { context.startActivity(mapIntent) }
            },
            shapes = AppButtonDefaults.shapes(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .then(safeOverlayInsets)
                .padding(end = 12.dp, top = 42.dp)
                .testTag("FullScreenMapGoogleRedirectButton")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp).postPressIconBounce()
            )
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.tx_detail_open_in_map),
                style = MaterialTheme.typography.labelMedium
            )
        }

        val bottomInsets = if (inContentContainer) {
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
        } else {
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
        }

        // Bottom floating M3E card for viewing/editing location metadata and persisting changes.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(bottomInsets)
                .padding(16.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .testTag("FullScreenMapControlCard"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.tx_detail_reposition_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (resolvingAddress) {
                                LoadingIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.5f, %.5f", draftLat, draftLng),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = draftLabel,
                    onValueChange = { draftLabel = it },
                    label = { Text(stringResource(R.string.tx_detail_location_name_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("FullScreenMapLocationNameInput")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletePressFilledTonalButton(
                        onClick = {
                            if (!gpsLoading) {
                                VibeHaptics.selection(view)
                                gpsLoading = true
                                scope.launch {
                                    val freshLoc = GnssLocationManager.resolveFreshLocation(context)
                                        ?: GnssLocationManager.getInstantLocation(context)
                                    gpsLoading = false
                                    if (freshLoc != null) {
                                        updateCoordinates(freshLoc.latitude, freshLoc.longitude, freshLoc.locationName)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.tx_detail_location_failed),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                        enabled = !gpsLoading,
                        shapes = AppButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("FullScreenMapUseGpsButton")
                    ) {
                        if (gpsLoading) {
                            LoadingIndicator(
                                modifier = Modifier.size(14.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp).postPressIconBounce()
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.tx_detail_tag_location),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    GnssLocationManager.PRESETS.forEach { preset ->
                        CompletePressOutlinedButton(
                            onClick = {
                                VibeHaptics.lightTick(view)
                                updateCoordinates(preset.latitude, preset.longitude, preset.title)
                            },
                            shapes = AppButtonDefaults.shapes(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("FullScreenMapPreset_${preset.id}")
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp).postPressIconBounce()
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                CompletePressButton(
                    onClick = {
                        VibeHaptics.successConfirm(view)
                        val finalLabel = draftLabel.trim().ifBlank {
                            String.format(Locale.US, "%.5f, %.5f", draftLat, draftLng)
                        }
                        onSaveLocation(draftLat, draftLng, finalLabel)
                        onExit()
                    },
                    shapes = AppButtonDefaults.largeShapes(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("FullScreenMapSaveButton")
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).postPressIconBounce()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.tx_detail_save_location),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
