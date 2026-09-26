package com.example.vibefinance.util

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import org.junit.Assert.assertEquals
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
}
