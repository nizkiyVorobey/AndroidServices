package com.example.servicestest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.servicestest.MyWorker.Companion.WORK_NAME
import com.example.servicestest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /**
         * Запускаєтьс в головному потоці
         * Простий сервіс, після API 26 не буде відновлюватися, якщо його вбито системою.
         * Якщо викликати декілька раз, то одночасно буде працювати декілька сервісів
         */
        binding.simpleService.setOnClickListener {
//            stopService(MyForegroundService.newIntent(this)) // Зупинить сервіс MyForegroundService

            startService(MyService.newIntent(this, 25))
        }

        /**
         * Запускаєтьс в головному потоці
         * після API 26 буде відновлюватися, якщо його вбито системою, але завжди покажує notification, аж доти, доки
         * виконується. Якщо закрити апку, сервіс не припинить своє виконання ні на секунду.
         * Якщо викликати декілька раз, то одночасно буде працювати декілька сервісів
         */
        binding.foregroundService.setOnClickListener {
//            showNotification()
            ContextCompat.startForegroundService(
                this,
                MyForegroundService.newIntent(this)
            )
        }

        /**
         * Запускаєтьс не в головному потоці
         * завжди покажує notification, аж доти, доки виконується. Якщо закрити апку, сервіс не припинить своє виконання ні на секунду.
         * Якщо викликати декілька раз, то одночасно сервіси будуть ствавати у чергу і виконуватися по яерзі
         */
        binding.intentService.setOnClickListener {
            ContextCompat.startForegroundService(
                this,
                MyIntentService.newIntent(this)
            )
        }

        /**
         * Не показуємо notification, але при кожному виклику буде перестоврюватись.
         * Можна зробити щоб після вбивства системо він відновлювався, але відновлення буде з самого початку, і відновлення
         * займе певний час (кілька секунд).
         * Можна поставити певні умови коли убе виконуватися, наприклад якщо телефон підключено до зарядки, чи wifi.
         * Не працює на API менше 26
         */
        binding.jobScheduler.setOnClickListener {
            val componentName = ComponentName(this, MyJobService::class.java)

            // Містить усі вимоги до нашого сервіса
            val jobInfo = JobInfo.Builder(MyJobService.JOB_ID, componentName)
//                .setRequiresCharging(true) // Лише якщо телефон на зарядці
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // Лише wifi
                .setPersisted(true) // Перезапуститься, навіть, якщо телефон вимкнули, а потім умікнули
//                .setPeriodic() // Сервіс буде запускатися один раз на вказаний інтревал, правда це не точно (може і рідче)
                .build()

            val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(jobInfo)
        }


        /**
         *  Тапаємо кілька разів, і наш сервіс починає працювати (створюється черга), якщо ми викнемо апку, а сервіс не
         *  закінчився, то він обірветься, і сервіс перествориться за кілька хвилин(секунд) хоча апка вимкнена) з тієї черги,
         *  яка перервалася.
         *  Це поєднання jobScheduler і intentService. Ми маємо можливість зробити чергу і для API < 26
         */
        binding.jobIntentService.setOnClickListener {
            MyJobIntentService.enqueue(this, page++)
        }

        /**
         * WorkManager замінив старі методи роботи з Service
         */
        binding.workManager.setOnClickListener {

            // Передаємо applicationContext, а не this, щоб не було утечок памяті, коли актівіті нема, а WorkManager все ще працює
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.APPEND, // Що робити, якщо worker з таким імям вже існує
                MyWorker.makeRequest(page++) // Це сам запит, де ми можемо встановити обмеження для нашого сервіса
            )
        }
    }

    private fun showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        //Define sound URI
        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChanel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            notificationChanel.setSound(soundUri, audioAttributes)
            notificationManager.createNotificationChannel(notificationChanel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSound(soundUri)
            .setContentTitle("Title")
            .setContentText("Text")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()


        notificationManager.notify(1, notification)
    }

    companion object {
        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "channel_name"
    }
}