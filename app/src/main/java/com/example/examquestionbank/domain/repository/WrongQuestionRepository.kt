package com.example.examquestionbank.domain.repository

import com.example.examquestionbank.domain.model.QuestionFlag
import kotlinx.coroutines.flow.Flow

interface WrongQuestionRepository {

    suspend fun insertOrUpdateFlag(flag: QuestionFlag)

    suspend fun getFlagByQuestionAndFlagType(questionId: Long, flagType: String): QuestionFlag?

    fun getByFlagTypeFlow(flagType: String): Flow<List<QuestionFlag>>

    fun getWrongQuestionsFlow(): Flow<List<QuestionFlag>>

    fun getFavoriteQuestionsFlow(): Flow<List<QuestionFlag>>

    fun getByQuestionIdFlow(questionId: Long): Flow<List<QuestionFlag>>

    suspend fun updateMastery(questionId: Long, flagType: String, masteryLevel: Int, note: String, updatedAt: String)

    suspend fun updateNote(questionId: Long, flagType: String, note: String, updatedAt: String)

    suspend fun deleteByQuestionAndFlagType(questionId: Long, flagType: String)

    suspend fun deleteByFlagType(flagType: String)
}
