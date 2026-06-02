package com.example.examquestionbank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examquestionbank.data.local.entity.AnswerRecordEntity

@Dao
interface AnswerRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AnswerRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AnswerRecordEntity>)

    @Query("SELECT * FROM answer_records WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): List<AnswerRecordEntity>

    @Query("SELECT * FROM answer_records WHERE sessionId = :sessionId AND questionId = :questionId")
    suspend fun getBySessionAndQuestion(sessionId: String, questionId: Long): AnswerRecordEntity?

    @Query("SELECT COUNT(*) FROM answer_records WHERE sessionId = :sessionId AND isCorrect = 1")
    suspend fun getCorrectCountBySession(sessionId: String): Int

    @Query("DELETE FROM answer_records WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
}
