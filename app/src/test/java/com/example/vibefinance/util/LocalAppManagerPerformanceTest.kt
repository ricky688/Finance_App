package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Looper
import java.time.Duration
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock

/**
 * Covers the catalog used by the verified Modify Wallet -> App Quick Launch picker.
 * These tests protect the expensive PackageManager work rather than UI layout details.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalAppManagerPerformanceTest {

    @Test
    fun metadataLoadsOffMainWithoutLoadingIconsOrRepeatingLauncherLookups(): Unit = runBlocking {
        val fixture = catalogFixture()
        val mainThread = Looper.getMainLooper().thread
        val scanThread = AtomicReference<Thread>()
        whenever(fixture.pm.queryIntentActivities(any<Intent>(), eq(0))).thenAnswer {
            scanThread.set(Thread.currentThread())
            fixture.launchers
        }

        val apps = LocalAppManager.loadInstalledApps(fixture.context)

        assertNotNull(scanThread.get())
        assertNotSame("Package scanning must leave the Android main thread", mainThread, scanThread.get())
        assertEquals(listOf("Zeta Bank", "Alpha Mail", "WeChat"), apps.map { it.appName })
        assertTrue(apps.first().isSuggestedPaymentApp)
        assertFalse(apps.any { it.packageName == fixture.context.packageName })
        apps.forEach { assertNull(it.iconBitmap) }
        verify(fixture.pm, never()).getApplicationIcon(any<ApplicationInfo>())
        verify(fixture.pm, never()).getApplicationIcon(any<String>())
        verify(fixture.pm, never()).getApplicationInfo(eq("com.example.bank"), eq(0))
        verify(fixture.pm, never()).getApplicationInfo(eq("com.example.mail"), eq(0))
        verify(fixture.pm).getApplicationInfo(eq("com.tencent.mm"), eq(0))
    }

    @Test
    fun simultaneousLoadsAndReopeningShareOneCatalogScan(): Unit = runBlocking {
        val fixture = catalogFixture()
        val enteredQuery = CountDownLatch(1)
        val releaseQuery = CountDownLatch(1)
        whenever(fixture.pm.queryIntentActivities(any<Intent>(), eq(0))).thenAnswer {
            enteredQuery.countDown()
            check(releaseQuery.await(5, TimeUnit.SECONDS)) { "Package scan was not released" }
            fixture.launchers
        }

        val requests = List(4) { async { LocalAppManager.loadInstalledApps(fixture.context) } }
        // Let the async children start before the synchronous latch wait on this thread.
        kotlinx.coroutines.yield()
        try {
            assertTrue("An IO package scan must start", enteredQuery.await(5, TimeUnit.SECONDS))
        } finally {
            releaseQuery.countDown()
        }
        val catalogs = requests.awaitAll()
        catalogs.forEach { assertSame(catalogs.first(), it) }
        assertSame(catalogs.first(), LocalAppManager.loadInstalledApps(fixture.context))
        verify(fixture.pm, times(1)).queryIntentActivities(any<Intent>(), eq(0))
    }

    @Test
    fun localeConfigurationChangeRefreshesCatalogImmediately(): Unit = runBlocking {
        val fixture = catalogFixture()
        LocalAppManager.loadInstalledApps(fixture.context)

        fixture.configuration.setLocale(Locale.TRADITIONAL_CHINESE)
        LocalAppManager.loadInstalledApps(fixture.context)

        verify(fixture.pm, times(2)).queryIntentActivities(any<Intent>(), eq(0))
    }

    @Test
    fun expiredCatalogRefreshesAndDropsRemovedLauncherApps(): Unit = runBlocking {
        val fixture = catalogFixture()
        assertTrue(LocalAppManager.loadInstalledApps(fixture.context).any { it.packageName == "com.example.bank" })
        whenever(fixture.pm.queryIntentActivities(any<Intent>(), eq(0))).thenReturn(
            fixture.launchers.filterNot { it.activityInfo.packageName == "com.example.bank" }
        )

        ShadowSystemClock.advanceBy(Duration.ofMillis(60_001))
        val refreshed = LocalAppManager.loadInstalledApps(fixture.context)

        assertFalse(refreshed.any { it.packageName == "com.example.bank" })
        assertTrue(refreshed.any { it.packageName == "com.tencent.mm" })
        verify(fixture.pm, times(2)).queryIntentActivities(any<Intent>(), eq(0))
    }

    @Test
    fun cancellingColdLoadStopsWorkAndDoesNotPublishPartialCatalog(): Unit = runBlocking {
        val fixture = catalogFixture()
        val enteredQuery = CountDownLatch(1)
        val releaseQuery = CountDownLatch(1)
        whenever(fixture.pm.queryIntentActivities(any<Intent>(), eq(0))).thenAnswer {
            enteredQuery.countDown()
            check(releaseQuery.await(5, TimeUnit.SECONDS)) { "Package scan was not released" }
            fixture.launchers
        }
        val request = async { LocalAppManager.loadInstalledApps(fixture.context) }
        kotlinx.coroutines.yield()
        try {
            assertTrue(enteredQuery.await(5, TimeUnit.SECONDS))
            request.cancel()
        } finally {
            releaseQuery.countDown()
        }
        request.join()
        assertTrue(request.isCancelled)
        verify(fixture.pm, never()).getApplicationLabel(any<ApplicationInfo>())

        val reopened = LocalAppManager.loadInstalledApps(fixture.context)
        assertEquals(3, reopened.size)
        verify(fixture.pm, times(2)).queryIntentActivities(any<Intent>(), eq(0))
    }

    @Test
    fun bitmapIconsAreDownsampledWithoutRecyclingTheirSource() {
        val source = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888)
        val drawable = BitmapDrawable(Resources.getSystem(), source)

        val icon = LocalAppManager.drawableToBitmap(drawable)

        assertNotNull(icon)
        assertEquals(96, icon!!.width)
        assertEquals(64, icon.height)
        assertFalse(source.isRecycled)
        assertEquals(600, source.width)
    }

    @Test
    fun drawableIconsAreRasterizedWithinBoundsAndRestoreOriginalDrawableBounds() {
        val drawable = SizedDrawable()
        val originalBounds = Rect(10, 20, 300, 200)
        drawable.bounds = originalBounds

        val icon = LocalAppManager.drawableToBitmap(drawable)

        assertNotNull(icon)
        assertEquals(96, icon!!.width)
        assertEquals(48, icon.height)
        assertEquals(Rect(0, 0, 96, 48), drawable.drawnBounds)
        assertEquals(originalBounds, drawable.bounds)
    }

    @Test
    fun iconReopeningUsesCacheAndExpiredIconsAreReloaded() {
        val fixture = catalogFixture()
        val packageName = "com.example.bank"
        whenever(fixture.pm.getApplicationIcon(packageName)).thenAnswer { SizedDrawable() }

        val first = LocalAppManager.getAppIcon(fixture.context, packageName)
        assertNotNull(first)
        assertSame(first, LocalAppManager.getAppIcon(fixture.context, packageName))
        verify(fixture.pm, times(1)).getApplicationIcon(packageName)

        ShadowSystemClock.advanceBy(Duration.ofMillis(60_001))
        assertNotSame(first, LocalAppManager.getAppIcon(fixture.context, packageName))
        verify(fixture.pm, times(2)).getApplicationIcon(packageName)
    }

    @Test
    fun cachedIconDoesNotWaitForAnUnrelatedSlowIconDecode() {
        val fixture = catalogFixture()
        val cachedPackage = "com.example.bank"
        val slowPackage = "com.example.slow"
        val enteredDecode = CountDownLatch(1)
        val releaseDecode = CountDownLatch(1)
        whenever(fixture.pm.getApplicationIcon(cachedPackage)).thenAnswer { SizedDrawable() }
        whenever(fixture.pm.getApplicationIcon(slowPackage)).thenAnswer {
            enteredDecode.countDown()
            check(releaseDecode.await(5, TimeUnit.SECONDS)) { "Slow icon was not released" }
            SizedDrawable()
        }
        val cachedIcon = LocalAppManager.getAppIcon(fixture.context, cachedPackage)
        assertNotNull(cachedIcon)
        val workers = Executors.newFixedThreadPool(2)
        try {
            val slowLoad = workers.submit<Bitmap?> {
                LocalAppManager.getAppIcon(fixture.context, slowPackage)
            }
            try {
                assertTrue(enteredDecode.await(5, TimeUnit.SECONDS))
                val cachedLoad = workers.submit<Bitmap?> {
                    LocalAppManager.getAppIcon(fixture.context, cachedPackage)
                }
                // Completion must be possible while the other PackageManager call is still blocked.
                assertSame(cachedIcon, cachedLoad.get(2, TimeUnit.SECONDS))
            } finally {
                releaseDecode.countDown()
            }
            assertNotNull(slowLoad.get(5, TimeUnit.SECONDS))
            verify(fixture.pm, times(1)).getApplicationIcon(cachedPackage)
        } finally {
            releaseDecode.countDown()
            workers.shutdownNow()
        }
    }

    private data class CatalogFixture(
        val context: Context,
        val pm: PackageManager,
        val configuration: Configuration,
        val launchers: List<ResolveInfo>
    )

    private fun catalogFixture(): CatalogFixture {
        val context = mock<Context>()
        val pm = mock<PackageManager>()
        val resources = mock<Resources>()
        val configuration = Configuration().apply { setLocale(Locale.ENGLISH) }
        // A distinct package/configuration key isolates process caches between tests.
        val ownPackage = "com.example.vibefinance.test.${UUID.randomUUID()}"
        whenever(context.packageName).thenReturn(ownPackage)
        whenever(context.packageManager).thenReturn(pm)
        whenever(context.resources).thenReturn(resources)
        whenever(resources.configuration).thenReturn(configuration)

        fun launcher(packageName: String, label: String) = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                this.packageName = packageName
                applicationInfo = ApplicationInfo().apply {
                    this.packageName = packageName
                    name = label
                }
            }
        }
        val bank = launcher("com.example.bank", "Zeta Bank")
        val launchers = listOf(
            bank,
            launcher("com.example.mail", "Alpha Mail"),
            bank, // Multiple activities must not cause extra package/label/icon work.
            launcher(ownPackage, "VibeFinance")
        )
        whenever(pm.queryIntentActivities(any<Intent>(), eq(0))).thenReturn(launchers)
        whenever(pm.getApplicationInfo(any<String>(), eq(0))).thenAnswer { invocation ->
            val packageName = invocation.getArgument<String>(0)
            if (packageName != "com.tencent.mm") throw PackageManager.NameNotFoundException(packageName)
            ApplicationInfo().apply {
                this.packageName = packageName
                name = "WeChat"
            }
        }
        whenever(pm.getApplicationLabel(any<ApplicationInfo>())).thenAnswer { invocation ->
            invocation.getArgument<ApplicationInfo>(0).name
        }
        return CatalogFixture(context, pm, configuration, launchers)
    }

    private class SizedDrawable : Drawable() {
        var drawnBounds: Rect? = null
        override fun getIntrinsicWidth(): Int = 512
        override fun getIntrinsicHeight(): Int = 256
        override fun draw(canvas: Canvas) {
            drawnBounds = Rect(bounds)
            canvas.drawColor(Color.BLUE)
        }
        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: ColorFilter?) = Unit
        @Suppress("DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }
}
