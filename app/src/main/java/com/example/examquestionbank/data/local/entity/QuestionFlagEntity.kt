package com.example.examquestionbank.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_flags",
    indices = [Index("questionId"), Index("flagType")],
    uniqueConstraints = [androidx.room.UniqueConstraint(columns = ["questionId", "flagType"])]
)
data class QuestionFlagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long,
    val flagType: String,  // favorite / wrong / note
    val masteryLevel: Int = 0,  // 0=未掌握, 1=基本掌握, 2=已掌握
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
