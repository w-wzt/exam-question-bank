package com.example.examquestionbank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_sessions")
data class ExamSessionEntity(
    @PrimaryKey
    val id: String,  // UUID
    val mode: String,  // practice / quiz / exam
    val bankId: Long,
    val totalCount: Int = 0,
    val correctCount: Int = 0,
    val config: String = "{}",  // JSON
    val score: Float = 0f,  // 正确率百分比
    val startedAt: String = "",
    val finishedAt: String? = null
)
