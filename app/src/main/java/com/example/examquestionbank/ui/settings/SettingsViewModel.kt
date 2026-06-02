package com.example.examquestionbank.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examquestionbank.domain.repository.ExamRepository
import com.example.examquestionbank.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val fontSize: String = "medium",
    val theme: String = "system",
    val autoBackup: Boolean = false,
    val maxBackups: Int = 5,
    val version: String = "1.0.0",
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    companion object {
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_THEME = "theme"
        const val KEY_AUTO_BACKUP = "auto_backup"
        const val KEY_MAX_BACKUPS = "max_backups"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val fontSize = settingsRepository.get(KEY_FONT_SIZE) ?: "medium"
            val theme = settingsRepository.get(KEY_THEME) ?: "system"
            val autoBackup = settingsRepository.get(KEY_AUTO_BACKUP)?.toBooleanStrictOrNull() ?: false
            val maxBackups = settingsRepository.get(KEY_MAX_BACKUPS)?.toIntOrNull() ?: 5

            _uiState.value = _uiState.value.copy(
                fontSize = fontSize,
                theme = theme,
                autoBackup = autoBackup,
                maxBackups = maxBackups,
                isLoading = false
            )
        }
    }

    fun setFontSize(size: String) {
        viewModelScope.launch {
            settingsRepository.set(KEY_FONT_SIZE, size)
            _uiState.value = _uiState.value.copy(fontSize = size)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.set(KEY_THEME, theme)
            _uiState.value = _uiState.value.copy(theme = theme)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                // 删除所有考试记录（级联删除答题记录）
                examRepository.getAllSessionsFlow().collect { sessions ->
                    sessions.forEach { session ->
                        examRepository.deleteSession(session.id)
                    }
                    _uiState.value = _uiState.value.copy(message = "数据已清除")
                    return@collect
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "清除失败：${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
