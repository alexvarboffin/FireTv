package com.walhalla.data.repository;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BasePresenter {

    protected final Handler handler;
    protected final ExecutorService executor;
    protected final LocalDatabaseRepo db_repo;
    protected final Context context;


    public BasePresenter(Handler handler, Context context) {
        this.handler = handler;
        this.db_repo = LocalDatabaseRepo.getStoreInfoDatabase(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    // Метод для выполнения любой задачи в фоновом потоке
    protected void executeInBackground(Runnable task) {
        executor.execute(task);
    }

    // Метод для выполнения задачи в основном потоке
    protected void postToMainThread(Runnable task) {
        handler.post(task);
    }

    // Закрываем Executor, когда он больше не нужен
    public void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
