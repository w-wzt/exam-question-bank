package com.example.examquestionbank.domain.model

data class AnswerRecord(
    val id: Long = 0,
    val questionId: Long,
    val sessionId: String,
    val mode: String,
    val userAnswer: String = "",
    val isCorrect: Boolean? = null,
    val timeSpent: Int = 0,
    val answeredAt: String = ""
)
