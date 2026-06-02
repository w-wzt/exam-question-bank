package com.example.examquestionbank.data.repository

import com.example.examquestionbank.data.local.converter.Converters
import com.example.examquestionbank.data.local.dao.PracticeProgressDao
import com.example.examquestionbank.data.local.entity.PracticeProgressEntity
import com.example.examquestionbank.domain.model.PracticeProgress
import com.example.examquestionbank.domain.repository.PracticeProgressRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeProgressRepositoryImpl @Inject constructor(
    private val practiceProgressDao: PracticeProgressDao,
    private val converters: Converters
) : PracticeProgressRepository {

    override suspend fun save(progress: PracticeProgress): Long {
        // 单行模式：先删除旧记录再插入
        practiceProgressDao.clearBySessionId(progress.sessionId)
        return practiceProgressDao.save(progress.toEntity())
    }

    override suspend fun getBySessionId(sessionId: String): PracticeProgress? {
        return practiceProgressDao.getBySessionId(sessionId)?.toDomain()
    }

    override suspend fun hasProgress(sessionId: String): Boolean {
        return practiceProgressDao.hasProgress(sessionId)
    }

    override suspend fun clearBySessionId(sessionId: String) {
        practiceProgressDao.clearBySessionId(sessionId)
    }

    override suspend fun clearAll() {
        practiceProgressDao.clearAll()
    }

    private fun PracticeProgressEntity.toDomain() = PracticeProgress(
        id = id,
        sessionId = sessionId,
        bankId = bankId,
        bankName = bankName,
        currentIndex = currentIndex,
        questionIds = converters.toLongList(questionIds),
        userAnswers = converters.toStringMap(userAnswers),
        submittedMap = converters.toStringMap(submittedMap),
        savedAt = savedAt
    )

    private fun PracticeProgress.toEntity() = PracticeProgressEntity(
        id = id,
        sessionId = sessionId,
        bankId = bankId,
        bankName = bankName,
        currentIndex = currentIndex,
        questionIds = converters.fromLongList(questionIds),
        userAnswers = converters.fromStringMap(userAnswers),
        submittedMap = converters.fromStringMap(submittedMap),
        savedAt = savedAt
    )
}
