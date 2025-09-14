package com.walhalla.data.repository;

import android.content.Context;
import android.os.Handler;

import com.walhalla.data.model.Category;

import java.util.List;

public class CategoryPresenter extends BasePresenter {

    public CategoryPresenter(Handler handler, Context context) {
        super(handler, context);
    }

    public void getAllCategories(RepoCallback<List<Category>> callback) {
        executeInBackground(() -> {
            try {
                // Получаем категории в фоновом потоке
                List<Category> categories = db_repo.getAllCategories();
                // Возвращаем результат в главный поток
                postToMainThread(() -> callback.successResult(categories));
            } catch (Exception e) {
                postToMainThread(() -> callback.errorResult(e.getMessage()));
            }
        });
    }
}
