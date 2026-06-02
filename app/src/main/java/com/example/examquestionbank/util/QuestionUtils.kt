package com.example.examquestionbank.util

import com.example.examquestionbank.domain.model.OptionItem

object QuestionUtils {

    private val OPTION_LABELS = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    /** 解析选项JSON为带标签的列表，输入如 ["选项A","选项B"] */
    fun parseOptions(optionsJson: String): List<OptionItem> {
        if (optionsJson.isBlank() || optionsJson == "[]") return emptyList()
        val list: List<String> = try {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            com.google.gson.Gson().fromJson(optionsJson, type)
        } catch (_: Exception) { return emptyList() }
        return list.mapIndexed { idx, item ->
            OptionItem(label = OPTION_LABELS.getOrElse(idx) { "${idx + 1}" }, content = item)
        }
    }

    /** 按题型解析答案 */
    fun parseAnswer(raw: String, type: String): Any {
        if (raw.isBlank()) return if (type == "multiple") emptyList<String>() else if (type == "judge") false else ""
        return when (type) {
            "multiple" -> {
                if (raw.startsWith("[")) {
                    try { com.google.gson.Gson().fromJson(raw, List::class.java) as List<String> } catch { emptyList() }
                } else {
                    raw.chunked(1).filter { it in OPTION_LABELS }
                }
            }
            "judge" -> raw == "T" || raw.equals("true", true) || raw == "1"
            else -> raw  // single: 直接返回字符串如 "A"
        }
    }

    /** 校验答案 */
    fun checkAnswer(userAnswer: Any, correctAnswer: Any, type: String): Boolean {
        if (userAnswer == null) return false
        return when (type) {
            "multiple" -> {
                val user = (userAnswer as? List<*>)?.filterIsInstance<String>()?.sorted() ?: emptyList()
                val correct = (correctAnswer as? List<*>)?.filterIsInstance<String>()?.sorted() ?: emptyList()
                user == correct
            }
            "judge" -> {
                val user = when (userAnswer) {
                    is Boolean -> userAnswer
                    is String -> userAnswer == "T" || userAnswer.equals("true", true)
                    else -> false
                }
                val correct = when (correctAnswer) {
                    is Boolean -> correctAnswer
                    is String -> correctAnswer == "T" || correctAnswer.equals("true", true)
                    else -> false
                }
                user == correct
            }
            else -> userAnswer.toString() == correctAnswer.toString()
        }
    }

    /** 推断题型：T/F→判断，多字母→多选，单字母→单选 */
    fun inferType(answer: String): String = when {
        answer == "T" || answer == "F" -> "judge"
        answer.length > 1 && answer.all { it in 'A'..'Z' } -> "multiple"
        else -> "single"
    }

    fun getOptionLabels(count: Int): List<String> = OPTION_LABELS.take(count)
}
