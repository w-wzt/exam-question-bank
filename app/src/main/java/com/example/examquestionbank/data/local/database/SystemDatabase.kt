package com.example.examquestionbank.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.examquestionbank.data.local.converter.Converters
import com.example.examquestionbank.data.local.dao.QuestionBankDao
import com.example.examquestionbank.data.local.dao.QuestionDao
import com.example.examquestionbank.data.local.entity.QuestionBankEntity
import com.example.examquestionbank.data.local.entity.QuestionEntity

@Database(
    entities = [
        QuestionBankEntity::class,
        QuestionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SystemDatabase : RoomDatabase() {
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun questionDao(): QuestionDao
}
