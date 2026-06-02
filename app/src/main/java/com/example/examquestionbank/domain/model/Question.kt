package com.example.examquestionbank.domain.model

data class Question(
    val id: Long = 0,
    val bankId: Long,
    val type: String,  // single / multiple / judge
    val content: String,
    val options: List<OptionItem> = emptyList(),
    val answer: String,
    val explanation: String = "",
    val difficulty: Int = 2,
    val category: String = "",
    val tags: List<String> = emptyList(),
    val sortOrder: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
