package com.example.servicestest

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class MyForegroundService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val notificationBuilder by lazy { createNotificationBuilder() }
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    var onProgressChanged: ((Int) -> Unit)? = null

    // Сервіс створюється
    override fun onCreate() {
        super.onCreate()
        log("onCreate")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChanel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChanel)
        }
    }

    private fun createNotificationBuilder() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Foreground Service")
        .setContentText("Counting...")
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setProgress(100, 0, false)
        .setOnlyAlertOnce(true)

    /**
     * Тут відбувається уся робота сервісу. Цей метод вконується у головноту потоці.
     * На якому потоці працює сервіс - він працює на головному потоці
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand")

        coroutineScope.launch {
            for (i in 0..100 step 5) {
                delay(1000)
                val notification = notificationBuilder.setProgress(100, i, false).build()
                notificationManager.notify(NOTIFICATION_ID, notification)
                onProgressChanged?.invoke(i)
                log("Timer $i")
            }
            stopSelf() // Зупитяє сервіс з середини
        }

        return START_STICKY
    }

    // Сервіс вмирає
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        log("onDestroy")
    }

    /**
     * Тут ми повертаємр екземплаяр IBinder-ного типу, onBind інсує щоб ми могли підписатися на якісь зміни ззовні
     * Ми повернемо метод getService який повертає посилання на сервіс, а отже на всі його методи і поля
     */
    override fun onBind(p0: Intent?): IBinder {
        return LocalBinder()
    }

    inner class LocalBinder : Binder() {
        fun getService(): MyForegroundService = this@MyForegroundService
    }

    private fun log(message: String) {
        Log.d("MyForegroundService", "MyMessage: $message")
    }

    companion object {
        private const val CHANNEL_ID = "42"
        private const val CHANNEL_NAME = "my_channel"
        private const val NOTIFICATION_ID = 10

        fun newIntent(context: Context): Intent {
            return Intent(context, MyForegroundService::class.java)
        }
    }
}