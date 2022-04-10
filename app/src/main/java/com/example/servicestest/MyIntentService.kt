package com.example.servicestest

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class MyIntentService : IntentService(NAME) {

    // Сервіс створюється
    override fun onCreate() {
        super.onCreate()
        log("onCreate")

        /**
         * true = START_REDELIVER_INTENT. Якщо система вб'є наш сервіс, то він буде перезапушений, і
         * Intent який прилітає в якості параметра буде збережений
         *
         * false = START_NOT_STICKY.  Якщо система вб'є наш сервіс, він не буде перестворений
         */
        setIntentRedelivery(true)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }


    /**
     * Навідміну від onStartCommand виконується не в головному потоці. Якщо викликати одночасно
     * три виклики цього сервіса то вони додадуться у чергі у будуть виконуватися по черзці, як тільки
     * код у onHandleIntent виконається то сервіс буде зупинено (якщо в черзі є інші то вони почнуть виконання)
     */
    override fun onHandleIntent(p0: Intent?) {
        log("onHandleIntent")

        for (i in 0 until 15) {
            Thread.sleep(1000)
            log("Timer $i")
        }
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


    // Сервіс вмирає
    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy")
    }


    private fun log(message: String) {
        Log.d("MyIntentService", "MyMessage: $message")
    }

    companion object {
        private const val CHANNEL_ID = "42"
        private const val CHANNEL_NAME = "my_channel"
        private const val NOTIFICATION_ID = 10
        private const val NAME = "MyIntentService"

        fun newIntent(context: Context): Intent {
            return Intent(context, MyIntentService::class.java)
        }
    }
}