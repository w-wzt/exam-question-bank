package com.example.examquestionbank.domain.model

data class QuestionBank(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val questionCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
