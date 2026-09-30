package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LocalAppManagerTest {

    @Test
    fun candidatePaymentPackagesAreListedOnlyWhenPackageManagerConfirmsInstallation() {
        val context = mock<Context>()
        val packageManager = mock<PackageManager>()
        val launcherApp = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply { packageName = "com.example.mail" }
        }
        val installed = setOf("com.example.mail", "com.tencent.mm")

        whenever(context.packageManager).thenReturn(packageManager)
        whenever(context.packageName).thenReturn("com.example.vibefinance")
        whenever(packageManager.queryIntentActivities(any<Intent>(), eq(0)))
            .thenReturn(listOf(launcherApp))
        whenever(packageManager.getApplicationInfo(any(), eq(0))).thenAnswer { invocation ->
            val packageName = invocation.getArgument<String>(0)
            if (packageName !in installed) throw PackageManager.NameNotFoundException(packageName)
            ApplicationInfo().apply { this.packageName = packageName }
        }

        assertEquals(installed, LocalAppManager.getInstalledPackageNames(context))
    }

    @Test
    fun resolveTargetAppPackageOnlyReturnsInstalledExplicitAppAndNeverGuessesByName() {
        val context = mock<Context>()
        val packageManager = mock<PackageManager>()
        val installed = setOf("com.real.installed.app", "hk.alipay.payment")

        whenever(context.packageManager).thenReturn(packageManager)
        whenever(context.packageName).thenReturn("com.example.vibefinance")
        whenever(packageManager.getPackageInfo(any<String>(), eq(0))).thenAnswer { invocation ->
            val packageName = invocation.getArgument<String>(0)
            if (packageName !in installed) throw PackageManager.NameNotFoundException(packageName)
            android.content.pm.PackageInfo().apply { this.packageName = packageName }
        }

        // Explicit "none" disables redirection
        assertNull(LocalAppManager.resolveTargetAppPackage(context, "none", "Alipay HK"))
        assertNull(LocalAppManager.resolveTargetAppPackage(context, "NONE", "Alipay HK"))

        // Null or blank returns null even if account name has recognizable bank keywords
        assertNull(LocalAppManager.resolveTargetAppPackage(context, null, "Alipay HK"))
        assertNull(LocalAppManager.resolveTargetAppPackage(context, "", "HSBC Premier"))
        assertNull(LocalAppManager.resolveTargetAppPackage(context, "   ", "Octopus"))

        // Uninstalled package returns null
        assertNull(LocalAppManager.resolveTargetAppPackage(context, "com.not.installed", "Random Account"))

        // Installed package returns the exact package name
        assertEquals("com.real.installed.app", LocalAppManager.resolveTargetAppPackage(context, "com.real.installed.app", "My Cash"))
        assertEquals("hk.alipay.payment", LocalAppManager.resolveTargetAppPackage(context, "hk.alipay.payment", null))
    }
}
