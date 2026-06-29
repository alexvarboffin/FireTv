package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import com.walhalla.data.model.Category
import kotlinx.coroutines.runBlocking
import tv.hdonlinetv.compose.core.databridge.category.CategoryMapper
import tv.hdonlinetv.compose.core.databridge.category.LocalCategoryRepository

/**
 * Legacy MVP presenter — delegates to shared [LocalCategoryRepository].
 * Used by legacy :app fragments; Compose uses [CategoryViewModel] directly.
 */
class CategoryPresenter(handler: Handler, context: Context) : BasePresenter(handler, context) {

    private val repository = LocalCategoryRepository(context.applicationContext)

    fun getAllCategories(callback: RepoCallback<List<Category>>) {
        executeInBackground {
            try {
                val categories = runBlocking {
                    repository.getAllCategories()
                }.map { ui ->
                    CategoryMapper.toData(ui)
                }
                postToMainThread { callback.successResult(categories) }
            } catch (e: Exception) {
                postToMainThread { callback.errorResult(e.message ?: "error") }
            }
        }
    }
}
