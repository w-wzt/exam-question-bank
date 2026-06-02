package com.example.examquestionbank.data.repository

import com.example.examquestionbank.data.local.dao.QuestionBankDao
import com.example.examquestionbank.data.local.entity.QuestionBankEntity
import com.example.examquestionbank.domain.model.QuestionBank
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionBankRepositoryImpl @Inject constructor(
    private val questionBankDao: QuestionBankDao
) : QuestionBankRepository {

    override suspend fun insert(bank: QuestionBank): Long {
        return questionBankDao.insert(bank.toEntity())
    }

    override suspend fun update(bank: QuestionBank) {
        questionBankDao.update(bank.toEntity())
    }

    override suspend fun delete(bankId: Long) {
        questionBankDao.deleteById(bankId)
    }

    override suspend fun getById(bankId: Long): QuestionBank? {
        return questionBankDao.getById(bankId)?.toDomain()
    }

    override fun getAllFlow(): Flow<List<QuestionBank>> {
        return questionBankDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAll(): List<QuestionBank> {
        return questionBankDao.getAll().map { it.toDomain() }
    }

    override suspend fun updateQuestionCount(bankId: Long, count: Int, updatedAt: String) {
        questionBankDao.updateQuestionCount(bankId, count, updatedAt)
    }

    private fun QuestionBankEntity.toDomain() = QuestionBank(
        id = id,
        name = name,
        description = description,
        questionCount = questionCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun QuestionBank.toEntity() = QuestionBankEntity(
        id = id,
        name = name,
        description = description,
        questionCount = questionCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
