package com.example.servicestest

import android.content.Context
import android.util.Log
import androidx.work.*

class MyWorker(context: Context, private val workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    // Метод виконується не в головному потоці
    override fun doWork(): Result {

        log("doWork")
        val page = workerParameters.inputData.getInt(PAGE, 0)

        for (i in 0 until 5) {
            Thread.sleep(1000)
            log("Timer: $page $i")
        }

        return Result.success()
    }

    private fun log(message: String) {
        Log.d("MyWorker", "MyMessage: $message")
    }

    companion object {
        private const val PAGE = "page"
        const val WORK_NAME = "work name"

        fun makeRequest(page: Int): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<MyWorker>()
                .apply {
                    setInputData(
                        workDataOf(Pair(PAGE, page)) // or workDataOf(PAGE to page). Це так само сторить Pair
                    )
                    setConstraints(makeConstraints())
                }
                .build()
        }

        private fun makeConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                .build()
        }
    }

}