package com.example.examquestionbank.domain.model

data class PracticeProgress(
    val id: Long = 0,
    val sessionId: String,
    val bankId: Long,
    val bankName: String = "",
    val currentIndex: Int = 0,
    val questionIds: List<Long> = emptyList(),
    val userAnswers: Map<String, String> = emptyMap(),
    val submittedMap: Map<String, String> = emptyMap(),
    val savedAt: String = ""
)
