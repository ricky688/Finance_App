package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
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
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        val apps = mutableListOf<InstalledAppInfo>()
        val seen = mutableSetOf<String>()
        
        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (seen.contains(pkg) || pkg == context.packageName) continue
            seen.add(pkg)
            
            val appName = resolveInfo.loadLabel(pm).toString()
            val drawable = resolveInfo.loadIcon(pm)
            val bitmap = drawableToBitmap(drawable, 96, 96)
            val isSuggested = isPaymentRelated(pkg, appName)
            
            apps.add(InstalledAppInfo(pkg, appName, bitmap, isSuggested))
        }
        
        // Also ensure known payment apps are represented in the list if not installed
        for (known in InterceptableApp.values()) {
            val isAlreadyPresent = apps.any { it.packageName == known.packageKeywords.first() || it.appName.equals(known.displayName, ignoreCase = true) }
            if (!isAlreadyPresent) {
                apps.add(
                    InstalledAppInfo(
                        packageName = known.packageKeywords.first(),
                        appName = known.displayName,
                        iconBitmap = null,
                        isSuggestedPaymentApp = true
                    )
                )
            }
        }

        // Sort: Suggested payment apps first, then alphabetically
        return apps.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isSuggestedPaymentApp }
                .thenBy { it.appName.lowercase(Locale.getDefault()) }
        )
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
            val known = InterceptableApp.values().find { it.packageKeywords.contains(packageName) || it.id == packageName }
            known?.displayName ?: packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
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
