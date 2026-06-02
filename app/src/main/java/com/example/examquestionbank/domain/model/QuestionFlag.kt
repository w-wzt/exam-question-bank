package com.example.examquestionbank.domain.model

data class QuestionFlag(
    val id: Long = 0,
    val questionId: Long,
    val flagType: String,  // favorite / wrong / note
    val masteryLevel: Int = 0,  // 0-2
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
