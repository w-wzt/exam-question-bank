package com.example.examquestionbank.data.repository

import com.example.examquestionbank.data.local.dao.SettingsDao
import com.example.examquestionbank.data.local.entity.SettingsEntity
import com.example.examquestionbank.domain.repository.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun get(key: String): String? {
        return settingsDao.getValue(key)
    }

    override suspend fun set(key: String, value: String) {
        settingsDao.set(SettingsEntity(key = key, value = value, updatedAt = dateFormat.format(Date())))
    }

    override suspend fun delete(key: String) {
        settingsDao.delete(key)
    }
}
