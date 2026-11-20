package com.example.vibefinance.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.example.vibefinance.ui.motion.LocalIsSharedTargetPage
import com.example.vibefinance.ui.motion.sharedTransitionBounds
import com.example.vibefinance.ui.motion.sharedTransitionElement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import java.util.Locale

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

@Composable
internal fun rememberTransactionLocation(tx: TransactionEntity): TransactionLocationInfo? {
    val context = LocalContext.current
    return remember(tx.latitude, tx.longitude, tx.locationName, tx.startLocationName, tx.startLatitude, tx.startLongitude) {
        val lat = tx.latitude
        val lng = tx.longitude
        if (lat == null || lng == null) return@remember null
        val rawName = tx.locationName
        val isRoute = rawName?.contains(" ➔ ") == true
        val startStationName = tx.startLocationName?.takeIf { it.isNotBlank() }
            ?: if (isRoute) rawName.substringBefore(" ➔ ").trim() else null
        val startStation = startStationName?.let {
            com.example.vibefinance.transit.MtrStationRegistry.getStationByName(context, it)
        }
        TransactionLocationInfo(
            latitude = lat,
            longitude = lng,
            label = rawName?.takeIf { it.isNotBlank() } ?: String.format(Locale.US, "%.5f, %.5f", lat, lng),
            startStationName = startStationName,
            endStationName = if (isRoute) rawName.substringAfter(" ➔ ").trim() else null,
            startLatitude = tx.startLatitude ?: startStation?.latitude,
            startLongitude = tx.startLongitude ?: startStation?.longitude,
        )
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
) {
    Surface(
        modifier = modifier.fillMaxWidth().sharedTransitionBounds(SharedTxKeys.LocationCard, remeasure = true)
            .testTag("EditTransactionLocation"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.tx_detail_location),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            SharedTransactionMap(location, Modifier.fillMaxWidth().height(mapHeight))
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

/** Keys of the elements shared between the phone detail and edit dialogs. */
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
    val content: @Composable (Modifier) -> Unit = movableContentOf { modifier: Modifier ->
        location?.let {
            TransactionLocationMap(
                latitude = it.latitude, longitude = it.longitude,
                startLatitude = it.startLatitude, startLongitude = it.startLongitude,
                description = it.label, modifier = modifier
            )
        }
    }
}

internal val LocalSharedMapHost = staticCompositionLocalOf<SharedMapHost?> { null }

/** True inside the phone transaction dialog, where the edit form is tightened to avoid scrolling. */
internal val LocalCompactTransactionForm = staticCompositionLocalOf { false }

/**
 * The transaction map. Inside the phone dialog's shared-element transition the single map view
 * lives on the target page and moves between pages; elsewhere it is an ordinary map.
 */
@Composable
internal fun SharedTransactionMap(location: TransactionLocationInfo, modifier: Modifier) {
    val host = LocalSharedMapHost.current
    if (host == null) {
        TransactionLocationMap(
            latitude = location.latitude, longitude = location.longitude,
            startLatitude = location.startLatitude, startLongitude = location.startLongitude,
            description = location.label, modifier = modifier
        )
        return
    }
    host.location = location
    val isTarget = LocalIsSharedTargetPage.current
    Box(modifier.sharedTransitionElement(SharedTxKeys.Map)) {
        if (isTarget) host.content(Modifier.matchParentSize())
    }
}
