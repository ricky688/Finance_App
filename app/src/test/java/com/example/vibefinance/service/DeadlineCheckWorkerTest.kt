package com.example.vibefinance.service

import android.app.NotificationManager
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeadlineCheckWorkerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var worker: DeadlineCheckWorker

    @Before
    fun setUp() {
        // Use Robolectric's real application context
        context = RuntimeEnvironment.getApplication()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val executor = java.util.concurrent.Executor { command -> command.run() }
        
        val serialExecutor = org.mockito.Mockito.mock(
            Class.forName("androidx.work.impl.utils.taskexecutor.SerialExecutor"),
            org.mockito.Mockito.withSettings()
                .extraInterfaces(java.util.concurrent.Executor::class.java)
                .defaultAnswer { invocation ->
                    if (invocation.method.name == "execute") {
                        val runnable = invocation.arguments[0] as Runnable
                        runnable.run()
                    }
                    null
                }
        )

        val taskExecutorProxy = org.mockito.Mockito.mock(
            Class.forName("androidx.work.impl.utils.taskexecutor.TaskExecutor"),
            org.mockito.Mockito.withSettings()
                .defaultAnswer { invocation ->
                    val methodName = invocation.method.name
                    if (methodName == "getBackgroundExecutor" || methodName == "backgroundExecutor") {
                        executor
                    } else if (methodName == "getMainThreadExecutor" || methodName == "mainThreadExecutor") {
                        executor
                    } else if (methodName == "getSerialTaskExecutor" || methodName == "serialTaskExecutor") {
                        serialExecutor
                    } else {
                        null
                    }
                }
        )

        val workerFactory = mock<androidx.work.WorkerFactory>()
        val progressUpdater = mock<androidx.work.ProgressUpdater>()
        val foregroundUpdater = mock<androidx.work.ForegroundUpdater>()

        val workerParams = createWorkerParameters(
            id = java.util.UUID.randomUUID(),
            inputData = androidx.work.Data.EMPTY,
            tags = emptySet(),
            runAttemptCount = 0,
            generation = 0,
            backgroundExecutor = executor,
            taskExecutor = taskExecutorProxy as androidx.work.impl.utils.taskexecutor.TaskExecutor,
            workerFactory = workerFactory,
            progressUpdater = progressUpdater,
            foregroundUpdater = foregroundUpdater
        )

        worker = DeadlineCheckWorker(context, workerParams)
    }

    @Test
    fun testDeadlineCheckWorker_triggersNotification_whenDaysUntilDeadlineIsExactlyThree() = runTest {
        // Clear database before starting
        InMemoryDatabase.clearAllTables()

        // Calculate a payment deadline that is exactly 3 days in the future.
        // If today is 2026-05-27, plus 3 days is 2026-05-30.
        // We set the paymentDeadline day of month to that target day.
        val today = LocalDate.now()
        val targetDate = today.plusDays(3)
        val paymentDeadlineDay = targetDate.dayOfMonth

        val creditCardAccount = AccountEntity(
            id = 0, // Will be auto-assigned 1L
            name = "Amex Gold Card",
            type = AccountType.CC,
            balance = 1200.0,
            icon = "credit_card",
            creditLimit = 10000.0,
            billingDate = 10,
            paymentDate = 25,
            paymentDeadline = paymentDeadlineDay
        )

        InMemoryDatabase.insertAccount(creditCardAccount)

        // Run the worker
        val result = worker.doWork()

        // Assert work succeeds
        assertEquals(ListenableWorker.Result.success(), result)

        // Verify with Robolectric Shadows that the notification was posted
        val shadowNotificationManager = shadowOf(notificationManager)
        val notifications = shadowNotificationManager.allNotifications
        
        assertEquals(1, notifications.size)
        val notification = notifications[0]
        
        val extras = notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        
        assertNotNull(notification)
        assertTrue("Notification title matches card name", title?.contains("Amex Gold Card") == true)
        assertTrue("Notification text contains deadline info", text?.contains("3 days") == true)
    }

    @Test
    fun testDeadlineCheckWorker_doesNotTriggerNotification_whenDaysUntilDeadlineIsFour() = runTest {
        InMemoryDatabase.clearAllTables()

        // Calculate a payment deadline that is exactly 4 days in the future.
        val today = LocalDate.now()
        val targetDate = today.plusDays(4)
        val paymentDeadlineDay = targetDate.dayOfMonth

        val creditCardAccount = AccountEntity(
            id = 0,
            name = "Amex Gold Card",
            type = AccountType.CC,
            balance = 1200.0,
            icon = "credit_card",
            creditLimit = 10000.0,
            billingDate = 10,
            paymentDate = 25,
            paymentDeadline = paymentDeadlineDay
        )

        InMemoryDatabase.insertAccount(creditCardAccount)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
 
        // Verify with Robolectric Shadows that no notification was posted
        val shadowNotificationManager = shadowOf(notificationManager)
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(0, notifications.size)
    }

    private fun createWorkerParameters(
        id: java.util.UUID,
        inputData: androidx.work.Data,
        tags: Set<String>,
        runAttemptCount: Int,
        generation: Int,
        backgroundExecutor: java.util.concurrent.Executor,
        taskExecutor: androidx.work.impl.utils.taskexecutor.TaskExecutor,
        workerFactory: androidx.work.WorkerFactory,
        progressUpdater: androidx.work.ProgressUpdater,
        foregroundUpdater: androidx.work.ForegroundUpdater
    ): androidx.work.WorkerParameters {
        val constructor = androidx.work.WorkerParameters::class.java.getDeclaredConstructor(
            java.util.UUID::class.java,
            androidx.work.Data::class.java,
            java.util.Collection::class.java,
            Class.forName("androidx.work.WorkerParameters\$RuntimeExtras"),
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            java.util.concurrent.Executor::class.java,
            Class.forName("androidx.work.impl.utils.taskexecutor.TaskExecutor"),
            androidx.work.WorkerFactory::class.java,
            androidx.work.ProgressUpdater::class.java,
            androidx.work.ForegroundUpdater::class.java
        ).apply { isAccessible = true }

        val runtimeExtrasConstructor = Class.forName("androidx.work.WorkerParameters\$RuntimeExtras")
            .getDeclaredConstructor().apply { isAccessible = true }
        val runtimeExtras = runtimeExtrasConstructor.newInstance()

        return constructor.newInstance(
            id,
            inputData,
            tags,
            runtimeExtras,
            runAttemptCount,
            generation,
            backgroundExecutor,
            taskExecutor,
            workerFactory,
            progressUpdater,
            foregroundUpdater
        )
    }
}
