package com.example.examquestionbank.data.repository

import com.example.examquestionbank.data.local.converter.Converters
import com.example.examquestionbank.data.local.dao.QuestionDao
import com.example.examquestionbank.data.local.entity.QuestionEntity
import com.example.examquestionbank.domain.model.Question
import com.example.examquestionbank.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val converters: Converters
) : QuestionRepository {

    override suspend fun insert(question: Question): Long {
        return questionDao.insert(question.toEntity())
    }

    override suspend fun insertAll(questions: List<Question>) {
        questionDao.insertAll(questions.map { it.toEntity() })
    }

    override suspend fun deleteById(questionId: Long) {
        questionDao.deleteById(questionId)
    }

    override suspend fun deleteByBankId(bankId: Long) {
        questionDao.deleteByBankId(bankId)
    }

    override suspend fun getById(questionId: Long): Question? {
        return questionDao.getById(questionId)?.toDomain()
    }

    override fun getByBankIdFlow(bankId: Long): Flow<List<Question>> {
        return questionDao.getByBankIdFlow(bankId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getByBankId(bankId: Long): List<Question> {
        return questionDao.getByBankId(bankId).map { it.toDomain() }
    }

    override suspend fun getByBankIdAndType(bankId: Long, type: String): List<Question> {
        return questionDao.getByBankIdAndType(bankId, type).map { it.toDomain() }
    }

    override suspend fun getByBankIdAndCategory(bankId: Long, category: String): List<Question> {
        return questionDao.getByBankIdAndCategory(bankId, category).map { it.toDomain() }
    }

    override suspend fun getByBankIdTypeAndCategory(bankId: Long, type: String, category: String): List<Question> {
        return questionDao.getByBankIdTypeAndCategory(bankId, type, category).map { it.toDomain() }
    }

    override suspend fun searchByKeyword(bankId: Long, keyword: String): List<Question> {
        return questionDao.searchByKeyword(bankId, keyword).map { it.toDomain() }
    }

    override suspend fun getCategoriesByBankId(bankId: Long): List<String> {
        return questionDao.getCategoriesByBankId(bankId)
    }

    override suspend fun getCountByBankId(bankId: Long): Int {
        return questionDao.getCountByBankId(bankId)
    }

    private fun QuestionEntity.toDomain() = Question(
        id = id,
        bankId = bankId,
        type = type,
        content = content,
        options = converters.toOptionItemList(options),
        answer = answer,
        explanation = explanation,
        difficulty = difficulty,
        category = category,
        tags = converters.toStringList(tags),
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Question.toEntity() = QuestionEntity(
        id = id,
        bankId = bankId,
        type = type,
        content = content,
        options = converters.fromOptionItemList(options),
        answer = answer,
        explanation = explanation,
        difficulty = difficulty,
        category = category,
        tags = converters.fromStringList(tags),
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
