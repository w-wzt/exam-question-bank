package com.example.examquestionbank.util

import com.example.examquestionbank.domain.model.Question
import com.google.gson.Gson
import com.opencsv.CSVReaderBuilder
import com.opencsv.exceptions.CsvException
import java.io.InputStream
import java.io.InputStreamReader

object FileParser {

    private val gson = Gson()

    /**
     * 解析 CSV/TSV/TXT 文件为题目列表
     * 列顺序：题目内容 | 分类 | 答案 | 选项A | 选项B | 选项C | 选项D
     */
    fun parseFile(inputStream: InputStream, fileName: String): List<Question> {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val separator = when (extension) {
            "tsv" -> '\t'
            else -> ','
        }
        return when (extension) {
            "csv", "tsv" -> parseCsv(inputStream, separator)
            "txt" -> parseTxt(inputStream)
            else -> parseCsv(inputStream, separator)
        }
    }

    private fun parseCsv(inputStream: InputStream, separator: Char): List<Question> {
        val reader = CSVReaderBuilder(InputStreamReader(inputStream, "UTF-8"))
            .withCSVParser(com.opencsv.CSVParserBuilder().withSeparator(separator).build())
            .build()

        val questions = mutableListOf<Question>()
        try {
            val rows = reader.readAll()
            for (i in rows.indices) {
                val row = rows[i]
                if (i == 0 && isHeaderRow(row)) continue
                val question = rowToQuestion(row, i)
                if (question != null) questions.add(question)
            }
        } catch (_: CsvException) {
            // 忽略CSV解析错误
        } finally {
            reader.close()
        }
        return questions
    }

    private fun parseTxt(inputStream: InputStream): List<Question> {
        val text = inputStream.bufferedReader("UTF-8").readText()
        inputStream.close()
        val blocks = text.split(Regex("(?=\n\\d+[.、．])")).filter { it.isNotBlank() }
        val questions = mutableListOf<Question>()
        for ((index, block) in blocks.withIndex()) {
            val question = textBlockToQuestion(block.trim(), index)
            if (question != null) questions.add(question)
        }
        return questions
    }

    /** 首行检测：含"题目/题干/内容/question/content/序号/编号"视为表头跳过 */
    private fun isHeaderRow(row: Array<String>): Boolean {
        if (row.isEmpty()) return false
        val first = row[0].trim().lowercase()
        return first.contains("题目") || first.contains("题干") || first.contains("内容") ||
            first.contains("question") || first.contains("content") ||
            first.contains("序号") || first.contains("编号")
    }

    /**
     * 列顺序：题目内容 | 分类 | 答案 | 选项A | 选项B | 选项C | 选项D
     */
    private fun rowToQuestion(row: Array<String>, index: Int): Question? {
        if (row.size < 3) return null
        val content = row.getOrElse(0) { "" }.trim()
        val category = row.getOrElse(1) { "" }.trim()
        val answer = row.getOrElse(2) { "" }.trim()
        val optionA = row.getOrElse(3) { "" }.trim()
        val optionB = row.getOrElse(4) { "" }.trim()
        val optionC = row.getOrElse(5) { "" }.trim()
        val optionD = row.getOrElse(6) { "" }.trim()

        if (content.isBlank() || answer.isBlank()) return null

        // 构建选项列表
        val optionList = mutableListOf<String>()
        if (optionA.isNotBlank()) optionList.add(optionA)
        if (optionB.isNotBlank()) optionList.add(optionB)
        if (optionC.isNotBlank()) optionList.add(optionC)
        if (optionD.isNotBlank()) optionList.add(optionD)

        // 选项存储为 JSON 数组格式
        val optionsJson = gson.toJson(optionList)

        // 题型推断：用 QuestionUtils.inferType(answer)
        val type = QuestionUtils.inferType(answer)

        return Question(
            bankId = 0,
            type = type,
            content = content,
            options = QuestionUtils.parseOptions(optionsJson),
            answer = answer,
            explanation = "",
            difficulty = 2,
            category = category,
            tags = emptyList(),
            sortOrder = index
        )
    }

    private fun textBlockToQuestion(block: String, index: Int): Question? {
        val lines = block.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        var content = ""
        val optionLines = mutableListOf<String>()
        var answer = ""
        var explanation = ""

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("答案", ignoreCase = true) || trimmed.startsWith("正确答案", ignoreCase = true) -> {
                    answer = trimmed.substringAfter(":").substringAfter("：").trim()
                }
                trimmed.startsWith("解析", ignoreCase = true) -> {
                    explanation = trimmed.substringAfter(":").substringAfter("：").trim()
                }
                Regex("^[A-H][.、．:：\\s]").containsMatchIn(trimmed) -> {
                    optionLines.add(trimmed)
                }
                content.isBlank() -> {
                    content = trimmed.replace(Regex("^[\\d]+[.、．]\\s*"), "")
                }
                else -> {
                    content += "\n" + trimmed
                }
            }
        }

        if (content.isBlank() || answer.isBlank()) return null

        // 将选项行解析为纯内容列表
        val optionContents = optionLines.map { line ->
            Regex("^[A-H][.、．:：\\s]+(.+)").find(line)?.groupValues?.get(1)?.trim() ?: line.trim()
        }
        val optionsJson = gson.toJson(optionContents)

        val type = QuestionUtils.inferType(answer)

        return Question(
            bankId = 0,
            type = type,
            content = content,
            options = QuestionUtils.parseOptions(optionsJson),
            answer = answer,
            explanation = explanation,
            difficulty = 2,
            category = "",
            tags = emptyList(),
            sortOrder = index
        )
    }
}
