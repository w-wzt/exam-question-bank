package com.example.examquestionbank.domain.repository

interface SettingsRepository {

    suspend fun get(key: String): String?

    suspend fun set(key: String, value: String)

    suspend fun delete(key: String)
}
