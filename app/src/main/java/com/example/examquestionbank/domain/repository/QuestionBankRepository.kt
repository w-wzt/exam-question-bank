package com.example.examquestionbank.domain.repository

import com.example.examquestionbank.domain.model.QuestionBank
import kotlinx.coroutines.flow.Flow

interface QuestionBankRepository {

    suspend fun insert(bank: QuestionBank): Long

    suspend fun update(bank: QuestionBank)

    suspend fun delete(bankId: Long)

    suspend fun getById(bankId: Long): QuestionBank?

    fun getAllFlow(): Flow<List<QuestionBank>>

    suspend fun getAll(): List<QuestionBank>

    suspend fun updateQuestionCount(bankId: Long, count: Int, updatedAt: String)
}
