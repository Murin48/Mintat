package com.example.data.util

import java.util.regex.Pattern

object TextExtractor {

    private val THAI_DIGIT_MAP = mapOf(
        '๐' to '0', '๑' to '1', '๒' to '2', '๓' to '3', '๔' to '4',
        '๕' to '5', '๖' to '6', '๗' to '7', '๘' to '8', '๙' to '9'
    )

    private val SPLIT_PATTERNS = listOf(
        "(?m)^[ \t]*ข้อ[ \t]*\\d+",
        "(?m)^[ \t]*\\d+[.)][ \t]+"
    )

    fun convertThaiDigits(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            sb.append(THAI_DIGIT_MAP[c] ?: c)
        }
        return sb.toString()
    }

    fun redact(text: String): String {
        var result = text
        // 13-digit Thai Citizen ID
        result = result.replace(Regex("\\b\\d{1}[- ]?\\d{4}[- ]?\\d{5}[- ]?\\d{2}[- ]?\\d{1}\\b"), "[เลขบัตร]")
        // Thai mobile and landline phones
        result = result.replace(Regex("\\b0\\d{1,2}[- ]?\\d{3}[- ]?\\d{4}\\b"), "[เบอร์โทร]")
        // Emails
        result = result.replace(Regex("[\\w.+-]+@[\\w-]+\\.[\\w.-]+"), "[อีเมล]")
        return result
    }

    fun splitClauses(text: String): List<String> {
        val converted = convertThaiDigits(text.trim())
        if (converted.isBlank()) return emptyList()

        for (patternStr in SPLIT_PATTERNS) {
            val pattern = Pattern.compile(patternStr)
            val matcher = pattern.matcher(converted)
            val indices = mutableListOf<Int>()
            while (matcher.find()) {
                indices.add(matcher.start())
            }

            if (indices.size >= 3) {
                val clauses = mutableListOf<String>()
                for (i in indices.indices) {
                    val start = indices[i]
                    val end = if (i + 1 < indices.size) indices[i + 1] else converted.length
                    val clause = converted.substring(start, end).trim()
                    if (clause.length >= 20) {
                        clauses.add(clause)
                    }
                }
                if (clauses.size >= 3) {
                    return clauses
                }
            }
        }

        // If not matched or fewer than 3 clauses, try splitting by double line break or clause keyword
        val fallback = converted.split(Regex("\n\n+"))
            .map { it.trim() }
            .filter { it.length >= 15 }

        return if (fallback.isNotEmpty()) fallback else listOf(converted)
    }
}
