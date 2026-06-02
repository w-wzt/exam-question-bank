package com.example.examquestionbank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_progress")
data class PracticeProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val bankId: Long,
    val bankName: String = "",
    val currentIndex: Int = 0,
    val questionIds: String = "[]",  // JSON: [1,2,3]
    val userAnswers: String = "{}",  // JSON: {"0":"A","1":"BD"}
    val submittedMap: String = "{}",  // JSON: {"0":"A","1":"BD"}
    val savedAt: String = ""
)
