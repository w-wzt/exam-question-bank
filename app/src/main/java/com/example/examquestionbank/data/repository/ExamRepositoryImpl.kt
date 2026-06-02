package com.example.examquestionbank.data.repository

import com.example.examquestionbank.data.local.dao.AnswerRecordDao
import com.example.examquestionbank.data.local.dao.ExamSessionDao
import com.example.examquestionbank.data.local.entity.AnswerRecordEntity
import com.example.examquestionbank.data.local.entity.ExamSessionEntity
import com.example.examquestionbank.domain.model.AnswerRecord
import com.example.examquestionbank.domain.model.ExamSession
import com.example.examquestionbank.domain.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val examSessionDao: ExamSessionDao,
    private val answerRecordDao: AnswerRecordDao
) : ExamRepository {

    override suspend fun createSession(session: ExamSession) {
        examSessionDao.insert(session.toEntity())
    }

    override suspend fun finishSession(sessionId: String, correctCount: Int, score: Float, finishedAt: String) {
        examSessionDao.finishSession(sessionId, correctCount, score, finishedAt)
    }

    override suspend fun getSessionById(sessionId: String): ExamSession? {
        return examSessionDao.getById(sessionId)?.toDomain()
    }

    override fun getSessionsByBankIdFlow(bankId: Long): Flow<List<ExamSession>> {
        return examSessionDao.getByBankIdFlow(bankId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllSessionsFlow(): Flow<List<ExamSession>> {
        return examSessionDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSessionsByMode(mode: String): List<ExamSession> {
        return examSessionDao.getByMode(mode).map { it.toDomain() }
    }

    override suspend fun insertAnswerRecords(records: List<AnswerRecord>) {
        answerRecordDao.insertAll(records.map { it.toEntity() })
    }

    override suspend fun getAnswerRecordsBySession(sessionId: String): List<AnswerRecord> {
        return answerRecordDao.getBySessionId(sessionId).map { it.toDomain() }
    }

    override suspend fun getCorrectCountBySession(sessionId: String): Int {
        return answerRecordDao.getCorrectCountBySession(sessionId)
    }

    override suspend fun deleteSession(sessionId: String) {
        examSessionDao.deleteById(sessionId)
    }

    private fun ExamSessionEntity.toDomain() = ExamSession(
        id = id,
        mode = mode,
        bankId = bankId,
        totalCount = totalCount,
        correctCount = correctCount,
        config = config,
        score = score,
        startedAt = startedAt,
        finishedAt = finishedAt
    )

    private fun ExamSession.toEntity() = ExamSessionEntity(
        id = id,
        mode = mode,
        bankId = bankId,
        totalCount = totalCount,
        correctCount = correctCount,
        config = config,
        score = score,
        startedAt = startedAt,
        finishedAt = finishedAt
    )

    private fun AnswerRecordEntity.toDomain() = AnswerRecord(
        id = id,
        questionId = questionId,
        sessionId = sessionId,
        mode = mode,
        userAnswer = userAnswer,
        isCorrect = isCorrect,
        timeSpent = timeSpent,
        answeredAt = answeredAt
    )

    private fun AnswerRecord.toEntity() = AnswerRecordEntity(
        id = id,
        questionId = questionId,
        sessionId = sessionId,
        mode = mode,
        userAnswer = userAnswer,
        isCorrect = isCorrect,
        timeSpent = timeSpent,
        answeredAt = answeredAt
    )
}
