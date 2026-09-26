package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.example.vibefinance.data.entity.InterceptableApp
import java.util.Locale

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val iconBitmap: Bitmap?,
    val isSuggestedPaymentApp: Boolean = false
)

object LocalAppManager {

    fun getInstalledApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val apps = installedApplications(context).map { appInfo ->
            val packageName = appInfo.packageName
            val appName = pm.getApplicationLabel(appInfo).toString().ifBlank { packageName }
            val bitmap = runCatching {
                drawableToBitmap(pm.getApplicationIcon(appInfo), 96, 96)
            }.getOrNull()
            InstalledAppInfo(
                packageName = packageName,
                appName = appName,
                iconBitmap = bitmap,
                isSuggestedPaymentApp = isPaymentRelated(packageName, appName)
            )
        }

        // Sort: Suggested payment apps first, then alphabetically
        return apps.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isSuggestedPaymentApp }
                .thenBy { it.appName.lowercase(Locale.getDefault()) }
        )
    }

    fun getInstalledPackageNames(context: Context): Set<String> =
        installedApplications(context).mapTo(linkedSetOf()) { it.packageName }

    private fun installedApplications(context: Context): List<ApplicationInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packageNames = linkedSetOf<String>()
        pm.queryIntentActivities(launcherIntent, 0).forEach { resolveInfo ->
            resolveInfo.activityInfo?.packageName?.let(packageNames::add)
        }
        // These packages can post payment notifications without exposing a launcher activity.
        // PackageManager still has to confirm each package is installed and visible.
        InterceptableApp.values().forEach { known ->
            packageNames.addAll(known.packageKeywords)
        }
        packageNames.remove(context.packageName)
        return packageNames.mapNotNull { packageName ->
            runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull()
        }
    }

    fun getAppIcon(context: Context, packageName: String): Bitmap? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            drawableToBitmap(drawable, 96, 96)
        } catch (e: Exception) {
            null
        }
    }

    fun getAppLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun isPaymentRelated(pkg: String, name: String): Boolean {
        val lower = "$pkg $name".lowercase(Locale.US)
        return lower.contains("pay") || lower.contains("wallet") || lower.contains("bank") ||
               lower.contains("money") || lower.contains("octopus") || lower.contains("alipay") ||
               lower.contains("wechat") || lower.contains("card") || lower.contains("finance") ||
               lower.contains("hsbc") || lower.contains("chase") || lower.contains("citi") ||
               lower.contains("boc") || lower.contains("hangseng") || lower.contains("dbs")
    }

    fun drawableToBitmap(drawable: Drawable, width: Int = 96, height: Int = 96): Bitmap? {
        return try {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else width
                val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else height
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }
}
