package com.example.vibefinance.ui.history

import android.content.res.Configuration
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.sources.VectorSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.FillLayer
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicBoolean

/** ARTEMIS explored the located History detail first. Public HK coordinates; no ledger writes. */
class TransactionVectorMapTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val map = AtomicReference<MapLibreMap>()
    private val rendered = AtomicBoolean(false)
    private var visible by mutableStateOf(true)
    private var destinationLat by mutableDoubleStateOf(22.2840)

    private fun mapView(view: View): MapView? {
        if (view is MapView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) mapView(view.getChildAt(i))?.let { return it }
        return null
    }
    private fun render(route: Boolean, dark: Boolean) {
        rule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag("zh-Hant-HK"))
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    if (visible) TransactionLocationMap(destinationLat, 114.1580,
                        if (route) 22.3235 else null, if (route) 114.2140 else null,
                        "Public Hong Kong map fixture", Modifier.fillMaxWidth().height(180.dp))
                    else Text("Map closed")
                }
            }
        }
        waitForMap()
    }
    private fun waitForMap() {
        rule.waitUntil(90_000) {
            var loaded = false
            rule.runOnIdle {
                val view = mapView(rule.activity.window.decorView)
                if (map.get() == null && view != null) {
                    view.addOnDidFinishRenderingMapListener { if (it) rendered.set(true) }
                    view.getMapAsync { map.set(it); it.triggerRepaint() }
                }
                val active = map.get()
                if (active?.style?.isFullyLoaded == true && view != null) {
                    val features = active.queryRenderedFeatures(RectF(0f, 0f, view.width.toFloat(), view.height.toFloat()))
                    // Rendered geometry is tile-quantized; distinguish the 600m update within 10m.
                    loaded = rendered.get() && features.any {
                        val point = it.geometry() as? org.maplibre.geojson.Point
                        it.hasProperty("color") && point != null && kotlin.math.abs(point.latitude() - destinationLat) < 0.0001
                    } && features.any {
                        it.properties()?.toString()?.contains(Regex("[\\u3400-\\u9fff]")) == true
                    }
                }
            }
            loaded
        }
        rule.onNodeWithText("© OpenStreetMap").assertIsDisplayed()
    }
    private fun screenshot(name: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.getExternalFilesDir(null), name).outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    @Test fun lightVectorRouteHasChineseLabelsAndUpdatesWithoutWritingTransactions() {
        render(route = true, dark = false)
        rule.runOnIdle {
            val style = map.get().style!!
            assertTrue(style.getSource("openmaptiles") is VectorSource)
            assertEquals(android.graphics.Color.parseColor("#A8C7FA"), style.getLayerAs<FillLayer>("water")!!.fillColorAsInt)
            assertEquals(android.graphics.Color.parseColor("#CDE8C5"), style.getLayerAs<FillLayer>("park")!!.fillColorAsInt)
            assertNotNull(style.getLayer("transaction-line"))
            assertTrue(style.layers.filterIsInstance<SymbolLayer>().any {
                it.textField.expression?.toString()?.contains("name:zh-Hant") == true
            })
            val view = mapView(rule.activity.window.decorView)!!
            for (point in listOf(org.maplibre.android.geometry.LatLng(22.3235, 114.2140),
                org.maplibre.android.geometry.LatLng(22.2840, 114.1580))) {
                val screen = map.get().projection.toScreenLocation(point)
                assertTrue(screen.x in 0f..view.width.toFloat() && screen.y in 0f..view.height.toFloat())
            }
        }
        screenshot("maplibre-light-route.png")
        rule.runOnIdle { destinationLat = 22.29 }
        waitForMap()
        rule.runOnIdle { visible = false }
        rule.onNodeWithText("Map closed").assertIsDisplayed()
        rule.runOnIdle { assertNull(mapView(rule.activity.window.decorView)) }
    }
    @Test fun darkSingleLocationRendersVectorTilesAndDisposesCleanly() {
        render(route = false, dark = true)
        rule.runOnIdle {
            val style = map.get().style!!
            assertEquals(TRANSACTION_MAP_DARK_STYLE, style.uri)
            assertEquals(android.graphics.Color.parseColor("#17263C"), style.getLayerAs<FillLayer>("water")!!.fillColorAsInt)
            assertEquals(android.graphics.Color.parseColor("#263C32"), style.getLayerAs<FillLayer>("landuse_park")!!.fillColorAsInt)
            assertEquals(16.0, map.get().cameraPosition.zoom, 0.01)
        }
        screenshot("maplibre-dark-point.png")
        rule.runOnIdle { visible = false }
        rule.onNodeWithText("Map closed").assertIsDisplayed()
    }
}
