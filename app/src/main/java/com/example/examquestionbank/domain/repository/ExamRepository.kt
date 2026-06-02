package com.example.examquestionbank.domain.repository

import com.example.examquestionbank.domain.model.AnswerRecord
import com.example.examquestionbank.domain.model.ExamSession
import kotlinx.coroutines.flow.Flow

interface ExamRepository {

    suspend fun createSession(session: ExamSession)

    suspend fun finishSession(sessionId: String, correctCount: Int, score: Float, finishedAt: String)

    suspend fun getSessionById(sessionId: String): ExamSession?

    fun getSessionsByBankIdFlow(bankId: Long): Flow<List<ExamSession>>

    fun getAllSessionsFlow(): Flow<List<ExamSession>>

    suspend fun getSessionsByMode(mode: String): List<ExamSession>

    suspend fun insertAnswerRecords(records: List<AnswerRecord>)

    suspend fun getAnswerRecordsBySession(sessionId: String): List<AnswerRecord>

    suspend fun getCorrectCountBySession(sessionId: String): Int

    suspend fun deleteSession(sessionId: String)
}
