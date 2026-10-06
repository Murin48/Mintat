package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import kotlinx.serialization.Serializable

@Serializable
enum class RiskLevel(
    val title: String,
    val emoji: String,
    val badgeLabel: String,
    val sortOrder: Int
) {
    HIGH("เสี่ยงสูง / ต้องห้าม", "🔴", "เสี่ยงสูง", 0),
    MEDIUM("ข้อควรระวัง", "🟡", "ควรระวัง", 1),
    LOW("เป็นธรรม / สอดคล้องเกณฑ์", "🟢", "เป็นธรรม", 3),
    UNKNOWN("วิเคราะห์ไม่สำเร็จ", "⚪", "ไม่ทราบผล", 2);

    fun backgroundColor(): Color = when (this) {
        HIGH -> RiskRedBg
        MEDIUM -> RiskYellowBg
        LOW -> RiskGreenBg
        UNKNOWN -> RiskGrayBg
    }

    fun borderColor(): Color = when (this) {
        HIGH -> RiskRedBorder
        MEDIUM -> RiskYellowBorder
        LOW -> RiskGreenBorder
        UNKNOWN -> RiskGrayBorder
    }

    fun textColor(): Color = when (this) {
        HIGH -> RiskRedText
        MEDIUM -> RiskYellowText
        LOW -> RiskGreenText
        UNKNOWN -> RiskGray
    }

    fun chipColor(): Color = when (this) {
        HIGH -> RiskRedChip
        MEDIUM -> RiskYellowChip
        LOW -> RiskGreenChip
        UNKNOWN -> RiskGrayBg
    }
}

enum class HousingType(val label: String, val code: String) {
    UNKNOWN("ไม่แน่ใจ", "unknown"),
    APARTMENT("อพาร์ตเมนต์ / ห้องเช่าทั่วไป", "apartment"),
    DORM("หอพักจดทะเบียน", "dorm")
}

@Serializable
data class ContractClause(
    val no: Int,
    val rawText: String,
    val summary: String = "",
    val risk: RiskLevel = RiskLevel.UNKNOWN,
    val issues: List<String> = emptyList(),
    val lawRef: List<String> = emptyList(),
    val suggestion: String = ""
)

@Serializable
data class ContractOverview(
    val rent: String = "ไม่ระบุในสัญญา",
    val deposit: String = "ไม่ระบุในสัญญา",
    val duration: String = "ไม่ระบุในสัญญา",
    val utilities: String = "ไม่ระบุในสัญญา",
    val penalty: String = "ไม่ระบุในสัญญา",
    val missing: List<String> = emptyList()
)

@Serializable
data class AnalysisReport(
    val id: String,
    val timestamp: Long,
    val title: String,
    val housingType: HousingType,
    val clauses: List<ContractClause>,
    val overview: ContractOverview? = null,
    val rawText: String
) {
    val totalCount: Int get() = clauses.size
    val highRiskCount: Int get() = clauses.count { it.risk == RiskLevel.HIGH }
    val mediumRiskCount: Int get() = clauses.count { it.risk == RiskLevel.MEDIUM }
    val lowRiskCount: Int get() = clauses.count { it.risk == RiskLevel.LOW }
    val unknownCount: Int get() = clauses.count { it.risk == RiskLevel.UNKNOWN }
}

data class SampleContract(
    val key: String,
    val title: String,
    val emoji: String,
    val riskClass: RiskLevel,
    val desc: String,
    val text: String
)

data class CalculationResult(
    val eDiff: Double,
    val wDiff: Double,
    val cap: Double,
    val collected: Double,
    val over: Double,
    val yearlyUtil: Double,
    val total: Double
)

data class LegalRule(
    val title: String,
    val detail: String,
    val tag: String,
    val isBan: Boolean
)
