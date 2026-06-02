package com.example.examquestionbank.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.examquestionbank.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Update
    suspend fun update(question: QuestionEntity)

    @Delete
    suspend fun delete(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteById(questionId: Long)

    @Query("DELETE FROM questions WHERE bankId = :bankId")
    suspend fun deleteByBankId(bankId: Long)

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getById(questionId: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE bankId = :bankId ORDER BY sortOrder ASC, id ASC")
    fun getByBankIdFlow(bankId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE bankId = :bankId ORDER BY sortOrder ASC, id ASC")
    suspend fun getByBankId(bankId: Long): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE bankId = :bankId AND type = :type ORDER BY sortOrder ASC, id ASC")
    suspend fun getByBankIdAndType(bankId: Long, type: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE bankId = :bankId AND category = :category ORDER BY sortOrder ASC, id ASC")
    suspend fun getByBankIdAndCategory(bankId: Long, category: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE bankId = :bankId AND type = :type AND category = :category ORDER BY sortOrder ASC, id ASC")
    suspend fun getByBankIdTypeAndCategory(bankId: Long, type: String, category: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE bankId = :bankId AND content LIKE '%' || :keyword || '%' ORDER BY sortOrder ASC, id ASC")
    suspend fun searchByKeyword(bankId: Long, keyword: String): List<QuestionEntity>

    @Query("SELECT DISTINCT category FROM questions WHERE bankId = :bankId AND category != ''")
    suspend fun getCategoriesByBankId(bankId: Long): List<String>

    @Query("SELECT COUNT(*) FROM questions WHERE bankId = :bankId")
    suspend fun getCountByBankId(bankId: Long): Int

    @Query("SELECT * FROM questions WHERE bankId = :bankId ORDER BY sortOrder ASC, id ASC LIMIT :limit OFFSET :offset")
    suspend fun getByBankIdPaged(bankId: Long, limit: Int, offset: Int): List<QuestionEntity>
}
