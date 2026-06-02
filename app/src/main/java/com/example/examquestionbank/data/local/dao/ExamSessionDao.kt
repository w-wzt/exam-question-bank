package com.example.examquestionbank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examquestionbank.data.local.entity.ExamSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ExamSessionEntity)

    @Query("SELECT * FROM exam_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: String): ExamSessionEntity?

    @Query("UPDATE exam_sessions SET correctCount = :correctCount, score = :score, finishedAt = :finishedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: String, correctCount: Int, score: Float, finishedAt: String)

    @Query("SELECT * FROM exam_sessions WHERE bankId = :bankId ORDER BY startedAt DESC")
    fun getByBankIdFlow(bankId: Long): Flow<List<ExamSessionEntity>>

    @Query("SELECT * FROM exam_sessions WHERE bankId = :bankId ORDER BY startedAt DESC")
    suspend fun getByBankId(bankId: Long): List<ExamSessionEntity>

    @Query("SELECT * FROM exam_sessions WHERE mode = :mode ORDER BY startedAt DESC")
    suspend fun getByMode(mode: String): List<ExamSessionEntity>

    @Query("SELECT * FROM exam_sessions ORDER BY startedAt DESC")
    fun getAllFlow(): Flow<List<ExamSessionEntity>>

    @Query("SELECT * FROM exam_sessions WHERE finishedAt IS NULL")
    suspend fun getUnfinishedSessions(): List<ExamSessionEntity>

    @Query("DELETE FROM exam_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)
}
