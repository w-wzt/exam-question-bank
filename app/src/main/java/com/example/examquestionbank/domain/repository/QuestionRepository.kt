package com.example.examquestionbank.domain.repository

import com.example.examquestionbank.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {

    suspend fun insert(question: Question): Long

    suspend fun insertAll(questions: List<Question>)

    suspend fun deleteById(questionId: Long)

    suspend fun deleteByBankId(bankId: Long)

    suspend fun getById(questionId: Long): Question?

    fun getByBankIdFlow(bankId: Long): Flow<List<Question>>

    suspend fun getByBankId(bankId: Long): List<Question>

    suspend fun getByBankIdAndType(bankId: Long, type: String): List<Question>

    suspend fun getByBankIdAndCategory(bankId: Long, category: String): List<Question>

    suspend fun getByBankIdTypeAndCategory(bankId: Long, type: String, category: String): List<Question>

    suspend fun searchByKeyword(bankId: Long, keyword: String): List<Question>

    suspend fun getCategoriesByBankId(bankId: Long): List<String>

    suspend fun getCountByBankId(bankId: Long): Int
}
