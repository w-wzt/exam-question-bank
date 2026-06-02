package com.example.examquestionbank.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CountdownTimer(
    private val totalDurationMs: Long,
    private val tickIntervalMs: Long = 1000L
) {
    private val _remainingMs = MutableStateFlow(totalDurationMs)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var startTimeMs: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var thread: Thread? = null

    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        _isPaused.value = false
        startTimeMs = System.currentTimeMillis()
        pausedRemainingMs = 0L

        thread = Thread {
            while (_isRunning.value && _remainingMs.value > 0) {
                if (_isPaused.value) {
                    Thread.sleep(tickIntervalMs)
                    continue
                }
                Thread.sleep(tickIntervalMs)
                val elapsed = System.currentTimeMillis() - startTimeMs
                _remainingMs.value = maxOf(0L, totalDurationMs - elapsed)
                if (_remainingMs.value <= 0L) {
                    _isRunning.value = false
                    break
                }
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun pause() {
        if (!_isRunning.value || _isPaused.value) return
        _isPaused.value = true
        pausedRemainingMs = _remainingMs.value
    }

    fun resume() {
        if (!_isRunning.value || !_isPaused.value) return
        _isPaused.value = false
        startTimeMs = System.currentTimeMillis() - (totalDurationMs - pausedRemainingMs)
    }

    fun stop() {
        _isRunning.value = false
        _isPaused.value = false
        thread?.interrupt()
        thread = null
    }

    fun reset() {
        stop()
        _remainingMs.value = totalDurationMs
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
