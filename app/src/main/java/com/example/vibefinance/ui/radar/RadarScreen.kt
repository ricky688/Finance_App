package com.example.vibefinance.ui.radar

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.DiscountShop
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.components.GlassmorphicCard
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import com.example.vibefinance.util.GnssLocationManager
import com.example.vibefinance.util.GnssPoint
import com.example.vibefinance.util.GnssPreset
import kotlin.math.cos
import kotlin.math.sin

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
internal fun radarAspectLabel(aspect: String): String = when (aspect) {
    "All" -> stringResource(R.string.filter_all)
    "Coffee & Cafe" -> stringResource(R.string.ui_radar_aspect_coffee)
    "Supermarket" -> stringResource(R.string.ui_radar_aspect_supermarket)
    "Gas & Fuel" -> stringResource(R.string.ui_radar_aspect_fuel)
    "Electronics" -> stringResource(R.string.ui_radar_aspect_electronics)
    "Dining" -> stringResource(R.string.ui_radar_aspect_dining)
    "Retail & Shopping" -> stringResource(R.string.ui_radar_aspect_retail)
    "Entertainment" -> stringResource(R.string.cat_entertainment)
    "Services" -> stringResource(R.string.ui_radar_aspect_services)
    else -> aspect
}

@Composable
private fun radarLocationLabel(name: String): String = when (name) {
    "Downtown City Center" -> stringResource(R.string.ui_radar_location_downtown)
    "Tech & Financial District" -> stringResource(R.string.ui_radar_location_financial)
    "Grand Shopping Plaza" -> stringResource(R.string.ui_radar_location_shopping)
    "University Quarter" -> stringResource(R.string.ui_radar_location_university)
    "Suburban Mall & Park" -> stringResource(R.string.ui_radar_location_suburbs)
    "Live GNSS Location" -> stringResource(R.string.ui_radar_location_live)
    "Current Location" -> stringResource(R.string.ui_radar_location_current)
    "Custom Pin Location" -> stringResource(R.string.ui_radar_location_custom)
    else -> name
}

@Composable
internal fun radarDistanceLabel(meters: Double): String = if (meters < 1000) {
    stringResource(R.string.ui_radar_distance_m, meters.roundToInt())
} else {
    stringResource(R.string.ui_radar_distance_km, meters / 1000.0)
}

private val generatedPerkPattern = Regex("^([0-9]+(?:\\.[0-9]+)?)% Discount / Cashback Perk$")

@Composable
private fun radarPromoLabel(description: String, discountRate: Double): String {
    val generatedRate = generatedPerkPattern.matchEntire(description)?.groupValues?.get(1)
    return when {
        description.isBlank() -> stringResource(R.string.ui_radar_cashback, discountRate.toString())
        generatedRate != null -> stringResource(R.string.ui_radar_generated_perk, generatedRate)
        else -> description
    }
}

enum class RadarViewMode { GOOGLE_MAPS, RADAR_CANVAS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    onLogSpendClick: (shopName: String, aspect: String, cardId: Long) -> Unit
) {
    val context = LocalContext.current
    val currentLocation by GnssLocationManager.currentLocation.collectAsState()

    var viewMode by remember { mutableStateOf(RadarViewMode.GOOGLE_MAPS) } // Default Google Maps
    var scanRadiusMeters by remember { mutableFloatStateOf(1000f) } // Default 1km scan radius
    var selectedAspect by remember { mutableStateOf("All") }
    var selectedShopId by remember { mutableStateOf<Long?>(null) }
    var showAddShopSheet by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }

    // Aspects filter list
    val aspectOptions = listOf("All", "Coffee & Cafe", "Supermarket", "Gas & Fuel", "Electronics", "Dining", "Retail & Shopping")

    // Filter shops by radius & aspect
    val filteredShopsWithDistance = remember(state.discountShops, currentLocation, scanRadiusMeters, selectedAspect) {
        state.discountShops.map { shop ->
            val dist = GnssLocationManager.calculateHaversineDistance(
                currentLocation.latitude, currentLocation.longitude,
                shop.latitude, shop.longitude
            )
            shop to dist
        }.filter { (shop, dist) ->
            dist <= scanRadiusMeters && (selectedAspect == "All" || shop.aspect.contains(selectedAspect, ignoreCase = true))
        }.sortedBy { it.second }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. GNSS Location Bar & Presets Selector
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = stringResource(R.string.ui_radar_location),
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = radarLocationLabel(currentLocation.locationName),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (currentLocation.isMock) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = stringResource(R.string.ui_radar_preset),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = stringResource(
                                        R.string.ui_radar_gnss_active,
                                        String.format(java.util.Locale.US, "%.4f°, %.4f°", currentLocation.latitude, currentLocation.longitude)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Presets Menu Button
                        Box {
                            IconButton(
                                onClick = { showPresetMenu = true },
                                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = stringResource(R.string.ui_radar_location_presets),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showPresetMenu,
                                onDismissRequest = { showPresetMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.ui_radar_start_gps)) },
                                    leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                                    onClick = {
                                        GnssLocationManager.startGnssUpdates(context)
                                        showPresetMenu = false
                                    }
                                )

                                HorizontalDivider()

                                Text(
                                    text = stringResource(R.string.ui_radar_preset_locations),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )

                                GnssLocationManager.PRESETS.forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(radarLocationLabel(preset.title)) },
                                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                                        onClick = {
                                            GnssLocationManager.selectPreset(preset)
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. High-Tech Animated Radar Canvas
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.CompassCalibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.ui_radar_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // View Mode Connected Button Group Selector (Google Maps vs Radar Canvas)
                            val radarModes = listOf("Maps", "Radar")
                            val selectedIndex = if (viewMode == RadarViewMode.GOOGLE_MAPS) 0 else 1
                            ExpressiveSegmentedButtonGroup(
                                items = radarModes,
                                selectedIndex = selectedIndex,
                                onItemSelected = { index ->
                                    viewMode = if (index == 0) RadarViewMode.GOOGLE_MAPS else RadarViewMode.RADAR_CANVAS
                                },
                                labelProvider = { if (it == "Maps") stringResource(R.string.ui_radar_maps) else stringResource(R.string.ui_radar_mode) },
                                iconProvider = { item, tint ->
                                    if (item == "Maps") {
                                        Icon(Icons.Default.Map, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                                    } else {
                                        Icon(Icons.Default.Radar, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                                    }
                                },
                                modifier = Modifier.width(180.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render selected View Mode
                        AnimatedContent(
                            targetState = viewMode,
                            label = "radarViewModeCrossfade"
                        ) { currentMode ->
                            when (currentMode) {
                                RadarViewMode.GOOGLE_MAPS -> {
                                    GoogleMapView(
                                        scanRadiusMeters = scanRadiusMeters,
                                        currentLocation = currentLocation,
                                        shops = filteredShopsWithDistance,
                                        accounts = state.accounts,
                                        selectedShopId = selectedShopId,
                                        onSelectShop = { selectedShopId = it }
                                    )
                                }
                                RadarViewMode.RADAR_CANVAS -> {
                                    RadarCanvasView(
                                        scanRadiusMeters = scanRadiusMeters,
                                        currentLocation = currentLocation,
                                        shops = filteredShopsWithDistance,
                                        selectedShopId = selectedShopId,
                                        onSelectShop = { selectedShopId = it }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Radius Slider & Quick Radius Presets
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.ui_radar_scan_radius),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = radarDistanceLabel(scanRadiusMeters.toDouble()),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = scanRadiusMeters,
                                onValueChange = { scanRadiusMeters = it },
                                valueRange = 200f..5000f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Radius Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(300f, 500f, 1000f, 2500f, 5000f).forEach { radiusVal ->
                                    val isSelected = (scanRadiusMeters - radiusVal).let { kotlin.math.abs(it) < 50f }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { scanRadiusMeters = radiusVal },
                                        label = { Text(radarDistanceLabel(radiusVal.toDouble()), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.scale(0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Aspect / Category Filter Chips & Add Custom Button
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ui_radar_aspect_filter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(onClick = { showAddShopSheet = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.ui_radar_add_shop), fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(aspectOptions) { option ->
                            val isSelected = selectedAspect == option
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAspect = option },
                                label = { Text(radarAspectLabel(option), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 4. Nearby Discount Shop Items List
            if (filteredShopsWithDistance.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = stringResource(R.string.ui_radar_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.ui_radar_empty_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )

                            Button(
                                onClick = { showAddShopSheet = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.ui_radar_add_discount_shop))
                            }
                        }
                    }
                }
            } else {
                items(filteredShopsWithDistance, key = { it.first.id }) { (shop, distanceMeters) ->
                    val isSelected = selectedShopId == shop.id
                    DiscountShopCardItem(
                        shop = shop,
                        distanceMeters = distanceMeters,
                        accounts = state.accounts,
                        isSelected = isSelected,
                        onClick = { selectedShopId = if (isSelected) null else shop.id },
                        onDelete = { onIntent(FinanceIntent.DeleteDiscountShop(shop)) },
                        onLogSpendClick = onLogSpendClick
                    )
                }
            }
        }

        // Add Discount Shop Modal Bottom Sheet
        if (showAddShopSheet) {
            AddDiscountShopSheet(
                accounts = state.accounts,
                currentGnssPoint = currentLocation,
                onDismiss = { showAddShopSheet = false },
                onSaveShop = { shop -> onIntent(FinanceIntent.SaveDiscountShop(shop)) }
            )
        }
    }
}

@Composable
private fun RadarCanvasView(
    scanRadiusMeters: Float,
    currentLocation: GnssPoint,
    shops: List<Pair<DiscountShop, Double>>,
    selectedShopId: Long?,
    onSelectShop: (Long) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    // Continuous Sweep Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A)), // Deep midnight radar screen background
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = minOf(size.width, size.height) * 0.42f

            // Concentric Distance Rings
            val ringRatios = listOf(0.33f, 0.66f, 1.0f)
            ringRatios.forEach { ratio ->
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = maxR * ratio,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // Radar Axis Lines (Crosshairs)
            drawLine(
                color = primaryColor.copy(alpha = 0.2f),
                start = Offset(center.x - maxR * 1.05f, center.y),
                end = Offset(center.x + maxR * 1.05f, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.2f),
                start = Offset(center.x, center.y - maxR * 1.05f),
                end = Offset(center.x, center.y + maxR * 1.05f),
                strokeWidth = 1.dp.toPx()
            )

            // Radar Sweeping Cone
            rotate(degrees = sweepAngle, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor.copy(alpha = 0.4f),
                        0.25f to primaryColor.copy(alpha = 0.05f),
                        1.0f to Color.Transparent,
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxR, center.y - maxR),
                    size = androidx.compose.ui.geometry.Size(maxR * 2, maxR * 2)
                )
            }

            // Draw User Center GNSS Dot
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 16.dp.toPx() * pulseScale,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = center
            )

            // Plot nearby POI dots on Radar
            val latRangeDegree = (scanRadiusMeters / 111000f).coerceAtLeast(0.001f)
            val lngRangeDegree = (scanRadiusMeters / (111000f * cos(Math.toRadians(currentLocation.latitude)))).toFloat().coerceAtLeast(0.001f)

            shops.forEach { (shop, dist) ->
                val deltaLat = (shop.latitude - currentLocation.latitude).toFloat()
                val deltaLng = (shop.longitude - currentLocation.longitude).toFloat()

                // Relative position normalized to radar radius
                val xNorm = (deltaLng / lngRangeDegree).coerceIn(-1.0f, 1.0f)
                val yNorm = -(deltaLat / latRangeDegree).coerceIn(-1.0f, 1.0f) // Inverted Y for screen coordinates

                val poiPos = Offset(
                    center.x + xNorm * maxR,
                    center.y + yNorm * maxR
                )

                val isSelected = selectedShopId == shop.id
                val maxOfferRate = shop.offers.maxOfOrNull { it.discountRate } ?: 0.0

                val poiColor = if (maxOfferRate >= 8.0) Color(0xFFFFD54F) else tertiaryColor

                // Glowing outer halo
                drawCircle(
                    color = poiColor.copy(alpha = if (isSelected) 0.6f else 0.3f),
                    radius = (if (isSelected) 14.dp else 10.dp).toPx() * pulseScale,
                    center = poiPos
                )

                // Inner core dot
                drawCircle(
                    color = if (isSelected) Color.White else poiColor,
                    radius = (if (isSelected) 6.dp else 4.dp).toPx(),
                    center = poiPos
                )
            }
        }

        // Overlay Radar Scan Status Text
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = stringResource(R.string.ui_radar_scan_range, radarDistanceLabel(scanRadiusMeters.toDouble())),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DiscountShopCardItem(
    shop: DiscountShop,
    distanceMeters: Double,
    accounts: List<AccountEntity>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLogSpendClick: (shopName: String, aspect: String, cardId: Long) -> Unit
) {
    // Find best card discount for this shop
    val bestOffer = shop.offers.maxByOrNull { it.discountRate }
    val bestCard = accounts.find { it.id == bestOffer?.accountId } ?: accounts.firstOrNull { it.type == com.example.vibefinance.data.entity.AccountType.CC }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .bouncyClickable(shape = RoundedCornerShape(20.dp)) { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Aspect Category Badge
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val categoryIcon = when {
                                shop.aspect.contains("coffee", true) -> Icons.Default.Coffee
                                shop.aspect.contains("supermarket", true) || shop.aspect.contains("grocer", true) -> Icons.Default.ShoppingCart
                                shop.aspect.contains("gas", true) || shop.aspect.contains("fuel", true) -> Icons.Default.LocalGasStation
                                shop.aspect.contains("tech", true) || shop.aspect.contains("electronic", true) -> Icons.Default.Devices
                                shop.aspect.contains("dining", true) || shop.aspect.contains("food", true) -> Icons.Default.Restaurant
                                else -> Icons.Default.Storefront
                            }
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = shop.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = radarAspectLabel(shop.aspect),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "• ${if (shop.address == "Registered Store Location") stringResource(R.string.ui_radar_registered_address) else shop.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Distance Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = radarDistanceLabel(distanceMeters),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Best Credit Card Perk Banner
            if (bestOffer != null && bestCard != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.ui_radar_best_card, bestCard.name),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = radarPromoLabel(bestOffer.promoDescription, bestOffer.discountRate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD54F)
                        ) {
                            Text(
                                text = stringResource(R.string.ui_radar_percent_off, bestOffer.discountRate.toString()),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Quick Action Bar
            AnimatedVisibility(visible = isSelected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shop.isUserCreated) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            val cardId = bestCard?.id ?: accounts.firstOrNull()?.id ?: 1L
                            onLogSpendClick(shop.name, shop.aspect, cardId)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_radar_log_spend), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleMapView(
    scanRadiusMeters: Float,
    currentLocation: GnssPoint,
    shops: List<Pair<DiscountShop, Double>>,
    accounts: List<AccountEntity>,
    selectedShopId: Long?,
    onSelectShop: (Long) -> Unit
) {
    val userLatLng = remember(currentLocation.latitude, currentLocation.longitude) {
        LatLng(currentLocation.latitude, currentLocation.longitude)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 14.5f)
    }

    LaunchedEffect(userLatLng, scanRadiusMeters) {
        val zoomLevel = when {
            scanRadiusMeters <= 500f -> 15.5f
            scanRadiusMeters <= 1200f -> 14.5f
            scanRadiusMeters <= 3000f -> 13.0f
            else -> 11.5f
        }
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, zoomLevel)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val circleFillColor = primaryColor.copy(alpha = 0.12f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = false
            )
        ) {
            // User Location Center Marker
            Marker(
                state = MarkerState(position = userLatLng),
                title = radarLocationLabel(currentLocation.locationName),
                snippet = stringResource(R.string.ui_radar_marker_position, String.format(java.util.Locale.US, "%.4f, %.4f", currentLocation.latitude, currentLocation.longitude)),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

            // Scan Radius Circle Overlay
            Circle(
                center = userLatLng,
                radius = scanRadiusMeters.toDouble(),
                fillColor = circleFillColor,
                strokeColor = primaryColor,
                strokeWidth = 3f
            )

            // Plot Discount Shop Markers
            shops.forEach { (shop, dist) ->
                val shopLatLng = LatLng(shop.latitude, shop.longitude)
                val bestOffer = shop.offers.maxByOrNull { it.discountRate }
                val bestCard = accounts.find { it.id == bestOffer?.accountId }

                val markerHue = if ((bestOffer?.discountRate ?: 0.0) >= 8.0) {
                    BitmapDescriptorFactory.HUE_YELLOW
                } else {
                    BitmapDescriptorFactory.HUE_VIOLET
                }

                Marker(
                    state = MarkerState(position = shopLatLng),
                    title = shop.name,
                    snippet = stringResource(R.string.ui_radar_marker_offer, bestCard?.name ?: stringResource(R.string.ui_radar_card_fallback), (bestOffer?.discountRate ?: 5.0).toString(), radarDistanceLabel(dist)),
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        onSelectShop(shop.id)
                        false
                    }
                )
            }
        }
    }
}
