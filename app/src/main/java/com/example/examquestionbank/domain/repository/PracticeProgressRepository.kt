package com.example.examquestionbank.domain.repository

import com.example.examquestionbank.domain.model.PracticeProgress

interface PracticeProgressRepository {

    suspend fun save(progress: PracticeProgress): Long

    suspend fun getBySessionId(sessionId: String): PracticeProgress?

    suspend fun hasProgress(sessionId: String): Boolean

    suspend fun clearBySessionId(sessionId: String)

    suspend fun clearAll()
}
