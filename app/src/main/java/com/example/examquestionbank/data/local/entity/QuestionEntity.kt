package com.example.examquestionbank.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuestionBankEntity::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bankId"), Index("type"), Index("category")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankId: Long,
    val type: String,  // single / multiple / judge
    val content: String,
    val options: String = "[]",  // JSON: ["选项A","选项B"]
    val answer: String,
    val explanation: String = "",
    val difficulty: Int = 2,  // 1-5
    val category: String = "",
    val tags: String = "[]",  // JSON: ["tag1","tag2"]
    val sortOrder: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
