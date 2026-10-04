package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Canvas as AndroidCanvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import com.example.vibefinance.data.entity.InterceptableApp
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val iconBitmap: Bitmap?,
    val isSuggestedPaymentApp: Boolean = false
)

object LocalAppManager {

    private const val CACHE_TTL_MILLIS = 60_000L
    private const val MAX_CACHED_ICONS = 64
    private val catalogLock = Any()
    private val iconLock = Any()
    private var cachedCatalog: CachedCatalog? = null
    private val cachedIcons = LinkedHashMap<String, CachedIcon>(MAX_CACHED_ICONS, 0.75f, true)

    private data class CachedCatalog(
        val configuration: String,
        val loadedAt: Long,
        val apps: List<InstalledAppInfo>
    )

    private data class CachedIcon(
        val configuration: String,
        val loadedAt: Long,
        val bitmap: Bitmap?
    )

    /** Metadata only: the picker can open before rasterizing icons for its visible rows. */
    suspend fun loadInstalledApps(context: Context): List<InstalledAppInfo> =
        withContext(Dispatchers.IO) {
            val coroutineContext = currentCoroutineContext()
            installedAppCatalog(context) { coroutineContext.ensureActive() }
        }

    /** Compatibility for the allowed-app dialog, which still expects eager icons. */
    fun getInstalledApps(context: Context): List<InstalledAppInfo> =
        installedAppCatalog(context).map { app ->
            app.copy(iconBitmap = getAppIcon(context, app.packageName))
        }

    private fun cacheConfiguration(context: Context): String =
        "${context.packageName}:${context.resources.configuration}:${Locale.getDefault().toLanguageTag()}"

    private fun isFresh(loadedAt: Long, now: Long): Boolean =
        now - loadedAt in 0 until CACHE_TTL_MILLIS

    private fun installedAppCatalog(
        context: Context,
        checkActive: () -> Unit = {}
    ): List<InstalledAppInfo> = synchronized(catalogLock) {
        checkActive()
        val configuration = cacheConfiguration(context)
        val now = SystemClock.elapsedRealtime()
        cachedCatalog?.takeIf {
            it.configuration == configuration && isFresh(it.loadedAt, now)
        }?.let { return@synchronized it.apps }

        // This lock coalesces simultaneous picker requests into a single package scan.
        // loadInstalledApps always acquires it on Dispatchers.IO.
        val pm = context.packageManager
        val apps = installedApplications(context, checkActive).map { appInfo ->
            checkActive()
            val packageName = appInfo.packageName
            val appName = runCatching {
                pm.getApplicationLabel(appInfo).toString().ifBlank { packageName }
            }.getOrDefault(packageName)
            InstalledAppInfo(
                packageName = packageName,
                appName = appName,
                iconBitmap = null,
                isSuggestedPaymentApp = isPaymentRelated(packageName, appName)
            )
        }

        // Sort: Suggested payment apps first, then alphabetically
        val sortedApps = apps.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isSuggestedPaymentApp }
                .thenBy { it.appName.lowercase(Locale.getDefault()) }
        )
        checkActive()
        cachedCatalog = CachedCatalog(configuration, SystemClock.elapsedRealtime(), sortedApps)
        sortedApps
    }

    fun getInstalledPackageNames(context: Context): Set<String> =
        installedApplications(context).mapTo(linkedSetOf()) { it.packageName }

    private fun installedApplications(
        context: Context,
        checkActive: () -> Unit = {}
    ): List<ApplicationInfo> {
        checkActive()
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packageNames = linkedSetOf<String>()
        val launcherApplications = mutableMapOf<String, ApplicationInfo>()
        pm.queryIntentActivities(launcherIntent, 0).forEach { resolveInfo ->
            resolveInfo.activityInfo?.let { activity ->
                packageNames.add(activity.packageName)
                activity.applicationInfo?.let { launcherApplications[activity.packageName] = it }
            }
        }
        // These packages can post payment notifications without exposing a launcher activity.
        // PackageManager still has to confirm each package is installed and visible.
        InterceptableApp.values().forEach { known ->
            packageNames.addAll(known.packageKeywords)
        }
        packageNames.remove(context.packageName)
        return packageNames.mapNotNull { packageName ->
            checkActive()
            launcherApplications[packageName]
                ?: runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull()
        }
    }

    /** Call from an IO dispatcher for an uncached icon. Retains at most 64 icons, each <=96x96. */
    fun getAppIcon(context: Context, packageName: String): Bitmap? {
        val configuration = cacheConfiguration(context)
        val now = SystemClock.elapsedRealtime()
        synchronized(iconLock) {
            cachedIcons[packageName]?.takeIf {
                it.configuration == configuration && isFresh(it.loadedAt, now)
            }?.let { return it.bitmap }
        }
        // A slow icon must not hold the cache lock and stall unrelated cached UI icons.
        val bitmap = try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            drawableToBitmap(drawable, 96, 96)
        } catch (e: Exception) {
            null
        }
        synchronized(iconLock) {
            cachedIcons[packageName] = CachedIcon(configuration, SystemClock.elapsedRealtime(), bitmap)
            if (cachedIcons.size > MAX_CACHED_ICONS) {
                cachedIcons.remove(cachedIcons.keys.first())
            }
        }
        return bitmap
    }

    fun getAppLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString().takeIf { it.isNotBlank() } ?: packageName
        } catch (e: Exception) {
            packageName
        }
    }

    private fun isPaymentRelated(pkg: String, name: String): Boolean {
        val lower = "$pkg $name".lowercase(Locale.US)
        return lower.contains("pay") || lower.contains("wallet") || lower.contains("bank") ||
               lower.contains("money") || lower.contains("finance") || lower.contains("card")
    }

    fun drawableToBitmap(drawable: Drawable, width: Int = 96, height: Int = 96): Bitmap? {
        return try {
            val sourceBitmap = (drawable as? BitmapDrawable)?.bitmap
            val intrinsicWidth = sourceBitmap?.width ?: drawable.intrinsicWidth
            val intrinsicHeight = sourceBitmap?.height ?: drawable.intrinsicHeight
            val maxWidth = width.coerceAtLeast(1)
            val maxHeight = height.coerceAtLeast(1)
            val sourceWidth = intrinsicWidth.takeIf { it > 0 } ?: maxWidth
            val sourceHeight = intrinsicHeight.takeIf { it > 0 } ?: maxHeight
            val scale = min(1f, min(maxWidth.toFloat() / sourceWidth, maxHeight.toFloat() / sourceHeight))
            val w = (sourceWidth * scale).roundToInt().coerceIn(1, maxWidth)
            val h = (sourceHeight * scale).roundToInt().coerceIn(1, maxHeight)
            if (sourceBitmap != null) {
                if (sourceBitmap.width == w && sourceBitmap.height == h) sourceBitmap
                else Bitmap.createScaledBitmap(sourceBitmap, w, h, true)
            } else {
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bitmap)
                val originalBounds = Rect(drawable.bounds)
                try {
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                } finally {
                    drawable.bounds = originalBounds
                }
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resolveTargetAppPackage(
        context: Context,
        linkedAppPackage: String?,
        @Suppress("UNUSED_PARAMETER") accountName: String? = null,
        @Suppress("UNUSED_PARAMETER") nickname: String? = null,
        @Suppress("UNUSED_PARAMETER") issuer: String? = null
    ): String? {
        if (linkedAppPackage.isNullOrBlank() || linkedAppPackage.equals("none", ignoreCase = true)) {
            return null
        }
        return if (isAppInstalled(context, linkedAppPackage)) linkedAppPackage else null
    }
}
