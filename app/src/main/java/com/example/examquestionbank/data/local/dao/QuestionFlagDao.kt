package com.example.examquestionbank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.examquestionbank.data.local.entity.QuestionFlagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionFlagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flag: QuestionFlagEntity): Long

    @Query("SELECT * FROM question_flags WHERE questionId = :questionId AND flagType = :flagType")
    suspend fun getByQuestionAndFlagType(questionId: Long, flagType: String): QuestionFlagEntity?

    @Query("SELECT * FROM question_flags WHERE flagType = :flagType")
    fun getByFlagTypeFlow(flagType: String): Flow<List<QuestionFlagEntity>>

    @Query("SELECT * FROM question_flags WHERE flagType = 'wrong'")
    fun getWrongQuestionsFlow(): Flow<List<QuestionFlagEntity>>

    @Query("SELECT * FROM question_flags WHERE flagType = 'favorite'")
    fun getFavoriteQuestionsFlow(): Flow<List<QuestionFlagEntity>>

    @Query("SELECT * FROM question_flags WHERE questionId = :questionId")
    fun getByQuestionIdFlow(questionId: Long): Flow<List<QuestionFlagEntity>>

    @Query("UPDATE question_flags SET masteryLevel = :masteryLevel, note = :note, updatedAt = :updatedAt WHERE questionId = :questionId AND flagType = :flagType")
    suspend fun updateMastery(questionId: Long, flagType: String, masteryLevel: Int, note: String, updatedAt: String)

    @Query("UPDATE question_flags SET note = :note, updatedAt = :updatedAt WHERE questionId = :questionId AND flagType = :flagType")
    suspend fun updateNote(questionId: Long, flagType: String, note: String, updatedAt: String)

    @Query("DELETE FROM question_flags WHERE questionId = :questionId AND flagType = :flagType")
    suspend fun deleteByQuestionAndFlagType(questionId: Long, flagType: String)

    @Query("DELETE FROM question_flags WHERE flagType = :flagType")
    suspend fun deleteByFlagType(flagType: String)
}
