package com.example.servicestest

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import kotlinx.coroutines.*

class MyJobService : JobService() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Сервіс створюється
    override fun onCreate() {
        super.onCreate()
        log("onCreate")
    }

    /**
     *  Тут виконується уся робота. Виконується на головному потоці.
     *  Повертаємий Boolean показує чи все ще виконується наша робота. У нас робота виконується в coroutine, отже при закінченні
     *  метода onStartJob робота все ще виконується, отже матємо повернути true. Якщо робота синхронна, то повертаємо false
     */
    override fun onStartJob(p0: JobParameters?): Boolean {
        log("onStartJob")
        coroutineScope.launch {
            for (i in 0 until 100) {
                delay(1000)
                log("Timer $i")
            }
            jobFinished(p0, true) // true - означає, що після закінчення роботи наше сервіс через певний час перезапуститься
        }

        return true
    }

    /**
     * Викликається, якщо сервіс був зупинений. Наприклад сервіс працює, лише коли телефон на зарядці. Якщо теелфон відєднали від
     * зарядки то сервіс буде зупинено і викличеться метод onStopJob.
     *
     * ПРОТЕ, якщо ми самі зупитили сервіс за допомогою jobFinished, то onStopJob викликано не буде
      */
    override fun onStopJob(p0: JobParameters?): Boolean {
        log("onStopJob")

        return true // Якщо true то сервіс буде наново запланований на виконання, якщо ні, то false
    }


    // Сервіс вмирає
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        log("onDestroy")
    }

    private fun log(message: String) {
        Log.d("MyJobService", "MyMessage: $message")
    }


    companion object {
        const val JOB_ID = 111
    }
}