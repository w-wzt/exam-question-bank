package com.example.examquestionbank.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.examquestionbank.data.local.entity.QuestionBankEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionBankDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bank: QuestionBankEntity): Long

    @Update
    suspend fun update(bank: QuestionBankEntity)

    @Delete
    suspend fun delete(bank: QuestionBankEntity)

    @Query("DELETE FROM question_banks WHERE id = :bankId")
    suspend fun deleteById(bankId: Long)

    @Query("SELECT * FROM question_banks WHERE id = :bankId")
    suspend fun getById(bankId: Long): QuestionBankEntity?

    @Query("SELECT * FROM question_banks ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<QuestionBankEntity>>

    @Query("SELECT * FROM question_banks ORDER BY createdAt DESC")
    suspend fun getAll(): List<QuestionBankEntity>

    @Query("SELECT COUNT(*) FROM question_banks")
    suspend fun getCount(): Int

    @Query("UPDATE question_banks SET questionCount = :count, updatedAt = :updatedAt WHERE id = :bankId")
    suspend fun updateQuestionCount(bankId: Long, count: Int, updatedAt: String)
}
