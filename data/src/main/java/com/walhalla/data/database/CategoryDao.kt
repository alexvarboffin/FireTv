package com.walhalla.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.walhalla.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun addData(channels: List<Category>): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun addData(channels: Category)

    @Query("SELECT * from category order by name ASC")
    fun selectAllCategories(): List<Category> //    //убыванию, от больших значений к меньшим

    @Query("SELECT * from category order by name ASC")
    fun observeAllCategories(): Flow<List<Category>>

    @Query("DELETE FROM category WHERE LOWER(name) = LOWER(:name)")
    fun deleteCategoryByName(name: String): Int

    @Query(
        "DELETE FROM category WHERE name IS NOT NULL AND TRIM(name) != '' AND NOT EXISTS (" +
            "SELECT 1 FROM channel WHERE channel.cat IS NOT NULL AND " +
            "LOWER(channel.cat) = LOWER(category.name)" +
            ")",
    )
    fun deleteCategoriesWithNoChannels(): Int
}
