package com.example.examquestionbank.domain.model

data class ExamSession(
    val id: String,  // UUID
    val mode: String,  // practice / quiz / exam
    val bankId: Long,
    val totalCount: Int = 0,
    val correctCount: Int = 0,
    val config: String = "{}",
    val score: Float = 0f,
    val startedAt: String = "",
    val finishedAt: String? = null
)
