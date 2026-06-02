package com.example.examquestionbank.data.repository

import com.example.examquestionbank.data.local.dao.QuestionFlagDao
import com.example.examquestionbank.data.local.entity.QuestionFlagEntity
import com.example.examquestionbank.domain.model.QuestionFlag
import com.example.examquestionbank.domain.repository.WrongQuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WrongQuestionRepositoryImpl @Inject constructor(
    private val questionFlagDao: QuestionFlagDao
) : WrongQuestionRepository {

    override suspend fun insertOrUpdateFlag(flag: QuestionFlag) {
        val existing = questionFlagDao.getByQuestionAndFlagType(flag.questionId, flag.flagType)
        if (existing != null) {
            questionFlagDao.updateMastery(
                questionId = flag.questionId,
                flagType = flag.flagType,
                masteryLevel = flag.masteryLevel,
                note = flag.note,
                updatedAt = flag.updatedAt
            )
        } else {
            questionFlagDao.insert(flag.toEntity())
        }
    }

    override suspend fun getFlagByQuestionAndFlagType(questionId: Long, flagType: String): QuestionFlag? {
        return questionFlagDao.getByQuestionAndFlagType(questionId, flagType)?.toDomain()
    }

    override fun getByFlagTypeFlow(flagType: String): Flow<List<QuestionFlag>> {
        return questionFlagDao.getByFlagTypeFlow(flagType).map { list -> list.map { it.toDomain() } }
    }

    override fun getWrongQuestionsFlow(): Flow<List<QuestionFlag>> {
        return questionFlagDao.getWrongQuestionsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getFavoriteQuestionsFlow(): Flow<List<QuestionFlag>> {
        return questionFlagDao.getFavoriteQuestionsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getByQuestionIdFlow(questionId: Long): Flow<List<QuestionFlag>> {
        return questionFlagDao.getByQuestionIdFlow(questionId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateMastery(questionId: Long, flagType: String, masteryLevel: Int, note: String, updatedAt: String) {
        questionFlagDao.updateMastery(questionId, flagType, masteryLevel, note, updatedAt)
    }

    override suspend fun updateNote(questionId: Long, flagType: String, note: String, updatedAt: String) {
        questionFlagDao.updateNote(questionId, flagType, note, updatedAt)
    }

    override suspend fun deleteByQuestionAndFlagType(questionId: Long, flagType: String) {
        questionFlagDao.deleteByQuestionAndFlagType(questionId, flagType)
    }

    override suspend fun deleteByFlagType(flagType: String) {
        questionFlagDao.deleteByFlagType(flagType)
    }

    private fun QuestionFlagEntity.toDomain() = QuestionFlag(
        id = id,
        questionId = questionId,
        flagType = flagType,
        masteryLevel = masteryLevel,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun QuestionFlag.toEntity() = QuestionFlagEntity(
        id = id,
        questionId = questionId,
        flagType = flagType,
        masteryLevel = masteryLevel,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
