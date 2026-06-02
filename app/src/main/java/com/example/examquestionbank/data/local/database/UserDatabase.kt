package com.example.examquestionbank.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.examquestionbank.data.local.converter.Converters
import com.example.examquestionbank.data.local.dao.AnswerRecordDao
import com.example.examquestionbank.data.local.dao.ExamSessionDao
import com.example.examquestionbank.data.local.dao.PracticeProgressDao
import com.example.examquestionbank.data.local.dao.QuestionFlagDao
import com.example.examquestionbank.data.local.dao.SettingsDao
import com.example.examquestionbank.data.local.entity.AnswerRecordEntity
import com.example.examquestionbank.data.local.entity.ExamSessionEntity
import com.example.examquestionbank.data.local.entity.PracticeProgressEntity
import com.example.examquestionbank.data.local.entity.QuestionFlagEntity
import com.example.examquestionbank.data.local.entity.SettingsEntity

@Database(
    entities = [
        ExamSessionEntity::class,
        AnswerRecordEntity::class,
        QuestionFlagEntity::class,
        SettingsEntity::class,
        PracticeProgressEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun examSessionDao(): ExamSessionDao
    abstract fun answerRecordDao(): AnswerRecordDao
    abstract fun questionFlagDao(): QuestionFlagDao
    abstract fun settingsDao(): SettingsDao
    abstract fun practiceProgressDao(): PracticeProgressDao
}
