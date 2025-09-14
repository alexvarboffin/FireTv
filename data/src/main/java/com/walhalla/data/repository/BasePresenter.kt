package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class BasePresenter(protected val handler: Handler, @JvmField protected val context: Context) {
    protected val executor: ExecutorService
    @JvmField
    protected val db_repo: LocalDatabaseRepo


    init {
        this.db_repo = LocalDatabaseRepo.getStoreInfoDatabase(context)
        this.executor = Executors.newSingleThreadExecutor()
    }

    // Метод для выполнения любой задачи в фоновом потоке
    protected fun executeInBackground(task: Runnable?) {
        executor.execute(task)
    }

    // Метод для выполнения задачи в основном потоке
    protected fun postToMainThread(task: Runnable) {
        handler.post(task)
    }

    // Закрываем Executor, когда он больше не нужен
    fun shutdown() {
        if (!executor.isShutdown) {
            executor.shutdown()
        }
    }
}
