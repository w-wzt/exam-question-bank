package com.example.examquestionbank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_banks")
data class QuestionBankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val questionCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
