package com.example.examquestionbank.di

import android.content.Context
import androidx.room.Room
import com.example.examquestionbank.data.local.converter.Converters
import com.example.examquestionbank.data.local.dao.AnswerRecordDao
import com.example.examquestionbank.data.local.dao.ExamSessionDao
import com.example.examquestionbank.data.local.dao.PracticeProgressDao
import com.example.examquestionbank.data.local.dao.QuestionBankDao
import com.example.examquestionbank.data.local.dao.QuestionDao
import com.example.examquestionbank.data.local.dao.QuestionFlagDao
import com.example.examquestionbank.data.local.dao.SettingsDao
import com.example.examquestionbank.data.local.database.SystemDatabase
import com.example.examquestionbank.data.local.database.UserDatabase
import com.example.examquestionbank.data.repository.ExamRepositoryImpl
import com.example.examquestionbank.data.repository.PracticeProgressRepositoryImpl
import com.example.examquestionbank.data.repository.QuestionBankRepositoryImpl
import com.example.examquestionbank.data.repository.QuestionRepositoryImpl
import com.example.examquestionbank.data.repository.SettingsRepositoryImpl
import com.example.examquestionbank.data.repository.WrongQuestionRepositoryImpl
import com.example.examquestionbank.domain.repository.ExamRepository
import com.example.examquestionbank.domain.repository.PracticeProgressRepository
import com.example.examquestionbank.domain.repository.QuestionBankRepository
import com.example.examquestionbank.domain.repository.QuestionRepository
import com.example.examquestionbank.domain.repository.SettingsRepository
import com.example.examquestionbank.domain.repository.WrongQuestionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideConverters(): Converters = Converters()

    @Provides
    @Singleton
    fun provideSystemDatabase(@ApplicationContext context: Context): SystemDatabase {
        return Room.databaseBuilder(
            context,
            SystemDatabase::class.java,
            "system_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase {
        return Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_database"
        ).build()
    }

    // SystemDatabase DAOs
    @Provides
    fun provideQuestionBankDao(db: SystemDatabase): QuestionBankDao = db.questionBankDao()

    @Provides
    fun provideQuestionDao(db: SystemDatabase): QuestionDao = db.questionDao()

    // UserDatabase DAOs
    @Provides
    fun provideExamSessionDao(db: UserDatabase): ExamSessionDao = db.examSessionDao()

    @Provides
    fun provideAnswerRecordDao(db: UserDatabase): AnswerRecordDao = db.answerRecordDao()

    @Provides
    fun provideQuestionFlagDao(db: UserDatabase): QuestionFlagDao = db.questionFlagDao()

    @Provides
    fun provideSettingsDao(db: UserDatabase): SettingsDao = db.settingsDao()

    @Provides
    fun providePracticeProgressDao(db: UserDatabase): PracticeProgressDao = db.practiceProgressDao()

    // Repository bindings
    @Provides
    @Singleton
    fun provideQuestionBankRepository(impl: QuestionBankRepositoryImpl): QuestionBankRepository = impl

    @Provides
    @Singleton
    fun provideQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository = impl

    @Provides
    @Singleton
    fun provideExamRepository(impl: ExamRepositoryImpl): ExamRepository = impl

    @Provides
    @Singleton
    fun provideWrongQuestionRepository(impl: WrongQuestionRepositoryImpl): WrongQuestionRepository = impl

    @Provides
    @Singleton
    fun providePracticeProgressRepository(impl: PracticeProgressRepositoryImpl): PracticeProgressRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}
