package com.example.servicestest

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class MyForegroundService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Сервіс створюється
    override fun onCreate() {
        super.onCreate()
        log("onCreate")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChanel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChanel)
        }
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Counting...")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
    }

    /**
     * Тут відбувається уся робота сервісу. Цей метод вконується у головноту потоці.
     * На якому потоці працює сервіс - він працює на головному потоці
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand")

        coroutineScope.launch {
            for (i in 0 until 15) {
                delay(1000)
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

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
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