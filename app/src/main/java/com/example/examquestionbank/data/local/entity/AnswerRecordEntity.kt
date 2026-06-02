package com.example.examquestionbank.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "answer_records",
    foreignKeys = [
        ForeignKey(
            entity = ExamSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("questionId")]
)
data class AnswerRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long,
    val sessionId: String,
    val mode: String,  // practice / quiz / exam
    val userAnswer: String = "",
    val isCorrect: Boolean? = null,  // null=未判, true=正确, false=错误
    val timeSpent: Int = 0,  // 秒
    val answeredAt: String = ""
)
