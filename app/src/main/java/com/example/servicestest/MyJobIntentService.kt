package com.example.servicestest

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/**
 * У старих API ми можемо просто використовувати Service, оскільки немає ніяких обмежень,
 * але від Android 8, є обмеження і тому, щоб  запутити задачі у чергу, ми повинні аикористати метод
 * enqueue у MyJobService. Отже у нас є два класи, які роблять одне в теж, отже, щоб не (один для старих api,
 * інший для нових), отже щоб було дубляжу кода можна використати JobIntentService. Він під капотом обєднає
 * два класи і обере правильний в залежності від рівня API
 */
class MyJobIntentService : JobIntentService() {

    // Сервіс створюється
    override fun onCreate() {
        super.onCreate()
        log("onCreate")
    }


    override fun onHandleWork(intent: Intent) {
        val page = intent.getIntExtra(PAGE, 0)

        for (i in 0 until 5) {
            Thread.sleep(1000)
            log("Timer: $page $i")
        }
    }


    // Сервіс вмирає
    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy")
    }


    private fun log(message: String) {
        Log.d("MyJobIntentService", "MyMessage: $message")
    }


    companion object {
        private const val PAGE = "page"
        private const val JOB_ID = 12

        fun enqueue(context: Context, page: Int) {

            enqueueWork(
                context,
                MyJobIntentService::class.java,
                JOB_ID,
                newIntent(context, page)
            )
        }

        private fun newIntent(context: Context, page: Int): Intent {
            return Intent(context, MyJobIntentService::class.java).apply {
                putExtra(PAGE, page)
            }
        }
    }
}