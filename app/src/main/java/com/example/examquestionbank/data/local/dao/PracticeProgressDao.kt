package com.example.examquestionbank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examquestionbank.data.local.entity.PracticeProgressEntity

@Dao
interface PracticeProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(progress: PracticeProgressEntity): Long

    @Query("SELECT * FROM practice_progress WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): PracticeProgressEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM practice_progress WHERE sessionId = :sessionId)")
    suspend fun hasProgress(sessionId: String): Boolean

    @Query("DELETE FROM practice_progress WHERE sessionId = :sessionId")
    suspend fun clearBySessionId(sessionId: String)

    @Query("DELETE FROM practice_progress")
    suspend fun clearAll()
}
