package com.example.vibefinance.ui.history

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.view.MotionEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.vibefinance.R
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val PINS = "transaction-pins"
private const val ROUTE = "transaction-route"

/** OSM-derived OpenFreeMap vector styles: no Google endpoints or API keys. */
internal const val TRANSACTION_MAP_LIGHT_STYLE = "https://tiles.openfreemap.org/styles/positron"
internal const val TRANSACTION_MAP_DARK_STYLE = "https://tiles.openfreemap.org/styles/dark"

@Composable
internal fun TransactionLocationMap(
    latitude: Double, longitude: Double,
    startLatitude: Double?, startLongitude: Double?, description: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val chinese = LocalConfiguration.current.locales[0].language == "zh"
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val haloColor = MaterialTheme.colorScheme.surface.toArgb()
    val styleUrl = if (dark) TRANSACTION_MAP_DARK_STYLE else TRANSACTION_MAP_LIGHT_STYLE
    val coordinates = remember(latitude, longitude, startLatitude, startLongitude) {
        fun valid(lat: Double?, lng: Double?) = lat != null && lng != null && lat.isFinite() && lng.isFinite() && lat in -90.0..90.0 && lng in -180.0..180.0
        if (!valid(latitude, longitude)) emptyList() else buildList {
            if (valid(startLatitude, startLongitude)) add(LatLng(startLatitude!!, startLongitude!!))
            add(LatLng(latitude, longitude))
        }.distinct()
    }
    val currentCoordinates by rememberUpdatedState(coordinates)
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var readyStyle by remember { mutableStateOf<Style?>(null) }
    val mapView = remember(context) {
        MapLibre.getInstance(context)
        HttpRequestUtil.setOkHttpClient(OkHttpClient.Builder().addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .header("User-Agent", "${context.packageName} (Android; MapLibre)").build())
        }.build())
        MapView(context, MapLibreMapOptions.createFromAttributes(context)
            .textureMode(true).localIdeographFontFamily("sans-serif")).apply {
            onCreate(null)
            addOnDidFinishRenderingMapListener { fullyRendered ->
                if (fullyRendered && readyStyle != null && currentCoordinates.isNotEmpty()) { loading = false; failed = false }
            }
            addOnDidFailLoadingMapListener { failed = true; loading = false }
            getMapAsync { map = it }
            setOnTouchListener { view, event ->
                view.parent?.requestDisallowInterceptTouchEvent(event.actionMasked != MotionEvent.ACTION_UP && event.actionMasked != MotionEvent.ACTION_CANCEL)
                false
            }
        }
    }
    DisposableEffect(mapView, owner) {
        var started = false
        var resumed = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> { mapView.onStart(); started = true }
                Lifecycle.Event.ON_RESUME -> { mapView.onResume(); resumed = true }
                Lifecycle.Event.ON_PAUSE -> { mapView.onPause(); resumed = false }
                Lifecycle.Event.ON_STOP -> { mapView.onStop(); started = false }
                else -> Unit
            }
        }
        val memory = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() = mapView.onLowMemory()
        }
        context.applicationContext.registerComponentCallbacks(memory)
        owner.lifecycle.addObserver(observer)
        onDispose {
            readyStyle = null
            map = null
            owner.lifecycle.removeObserver(observer)
            context.applicationContext.unregisterComponentCallbacks(memory)
            if (resumed) mapView.onPause()
            if (started) mapView.onStop()
            mapView.onDestroy()
        }
    }
    LaunchedEffect(map, styleUrl, chinese, labelColor, haloColor, retry) {
        val activeMap = map ?: return@LaunchedEffect
        loading = true; failed = false; readyStyle = null
        activeMap.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
            applyStreetPalette(style, dark)
            style.layers.filterIsInstance<SymbolLayer>().forEach { layer ->
                // Preserve route shields/house numbers; only adapt name labels.
                val field = layer.textField.expression?.toString() ?: layer.textField.value?.toString().orEmpty()
                if (field.contains("name")) {
                    if (chinese) layer.setProperties(textField(coalesce(get("name:zh-Hant"), get("name:zh"),
                        get("name:nonlatin"), get("name"), get("name:latin"))))
                    layer.setProperties(textColor(labelColor), textHaloColor(haloColor), textHaloWidth(1f))
                }
            }
            style.addSource(GeoJsonSource(PINS, FeatureCollection.fromFeatures(emptyArray<Feature>())))
            style.addSource(GeoJsonSource(ROUTE, FeatureCollection.fromFeatures(emptyArray<Feature>())))
            style.addLayer(LineLayer("transaction-line", ROUTE).withProperties(
                lineColor("#2563EB"), lineWidth(3f), lineCap(Property.LINE_CAP_ROUND)))
            style.addLayer(CircleLayer("transaction-pin-halo", PINS).withProperties(
                circleColor(get("color")), circleRadius(11f), circleOpacity(0.25f), circleBlur(0.3f)))
            style.addLayer(CircleLayer("transaction-pin", PINS).withProperties(
                circleColor(get("color")), circleRadius(7f), circleStrokeColor("#FFFFFF"), circleStrokeWidth(2f)))
            readyStyle = style
        }
        delay(30_000)
        if (loading) { failed = true; loading = false }
    }
    LaunchedEffect(readyStyle, coordinates) {
        val style = readyStyle ?: return@LaunchedEffect
        if (coordinates.isEmpty()) { loading = false; failed = true; return@LaunchedEffect }
        val points = coordinates.map { Point.fromLngLat(it.longitude, it.latitude) }
        val pins = points.mapIndexed { index, point -> Feature.fromGeometry(point).apply {
            addStringProperty("color", if (points.size == 1) "#007AFE" else if (index == 0) "#10B981" else "#EF4444")
        } }
        style.getSourceAs<GeoJsonSource>(PINS)?.setGeoJson(FeatureCollection.fromFeatures(pins))
        style.getSourceAs<GeoJsonSource>(ROUTE)?.setGeoJson(FeatureCollection.fromFeatures(
            if (points.size > 1) listOf(Feature.fromGeometry(LineString.fromLngLats(points))) else emptyList()))
        mapView.post {
            if (readyStyle === style) {
                if (coordinates.size > 1) map?.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder().includes(coordinates).build(),
                    (24 * context.resources.displayMetrics.density).toInt(),
                    (56 * context.resources.displayMetrics.density).toInt(),
                    (24 * context.resources.displayMetrics.density).toInt(),
                    (32 * context.resources.displayMetrics.density).toInt()))
                else map?.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates.last(), 16.0))
            }
        }
    }
    Box(modifier.clip(RoundedCornerShape(28.dp))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(28.dp))) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize().semantics { contentDescription = description })
        if (loading && !failed) CircularProgressIndicator(Modifier.align(Alignment.Center).size(28.dp))
        if (failed) Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.transaction_map_unavailable))
                TextButton(onClick = { retry++ }) { Text(stringResource(R.string.transaction_map_retry)) }
            }
        }
        // Visible data attribution; native attribution control retains provider/license links.
        Surface(Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onSurface) {
            Text("© OpenStreetMap", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
        }
    }
}

/** Cartographic colors stay consistent across wallpaper palettes, like familiar street maps. */
private fun applyStreetPalette(style: Style, dark: Boolean) {
    fun color(light: String, night: String) = if (dark) night else light
    val land = color("#F8F9FA", "#202124")
    val water = color("#A8C7FA", "#17263C")
    val park = color("#CDE8C5", "#263C32")
    val road = color("#FFFFFF", "#3C4043")
    val roadEdge = color("#DADCE0", "#292D31")
    val highway = color("#FCE8B2", "#8A7E57")
    style.layers.forEach { layer ->
        when (layer) {
            is BackgroundLayer -> layer.setProperties(backgroundColor(land))
            is FillLayer -> when (layer.sourceLayer) {
                "water" -> layer.setProperties(fillColor(water))
                "park" -> layer.setProperties(fillColor(park))
                "landcover" -> if (layer.id.contains("wood")) layer.setProperties(fillColor(park))
                "landuse" -> layer.setProperties(fillColor(if (layer.id.contains("park")) park else color("#F1F3F4", "#24272B")))
                "building" -> layer.setProperties(fillColor(color("#E3E6E8", "#303438")), fillOutlineColor(roadEdge))
                "transportation" -> layer.setProperties(fillColor(land))
                "aeroway" -> layer.setProperties(fillColor(color("#E8EAED", "#303438")))
            }
            is LineLayer -> when (layer.sourceLayer) {
                "waterway" -> layer.setProperties(lineColor(water))
                "aeroway" -> layer.setProperties(lineColor(color("#DADCE0", "#4A4E52")))
                "transportation" -> {
                    val id = layer.id
                    val value = when {
                        id.contains("pier") -> land
                        id.contains("railway") -> if (id.contains("dashline")) road else color("#B8BDC4", "#666C75")
                        id.contains("casing") -> roadEdge
                        id.contains("motorway") -> highway
                        id.contains("path") -> color("#E5E7EB", "#454B52")
                        else -> road
                    }
                    layer.setProperties(lineColor(value))
                }
            }
        }
    }
}
