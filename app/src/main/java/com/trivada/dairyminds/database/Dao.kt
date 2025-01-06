package com.trivada.dairyminds.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DairyDao {
    @Query("SELECT * FROM dairy")
    fun getAll(): Flow<List<Dairy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dairy: Dairy)

    @Update
    suspend fun update(dairy: Dairy)

    @Delete
    suspend fun delete(dairy: Dairy)

    @Query("SELECT * FROM dairy WHERE title = :key")
    suspend fun getByTitle(key: String): Dairy?

    @Query("DELETE FROM dairy WHERE title = :key")
    suspend fun deleteByTitle(key: String)

    @Query("INSERT OR REPLACE INTO dairy (title, content, date, time) VALUES (:title, :content, :date, :time)")
    suspend fun upsertText(title: String, content: String, date : String, time : String)
}