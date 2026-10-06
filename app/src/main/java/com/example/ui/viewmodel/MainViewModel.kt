package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.legal.LegalData
import com.example.data.model.*
import com.example.data.service.GeminiContractService
import com.example.data.util.TextExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel : ViewModel() {

    // Tab state: 0 = Check, 1 = Calculator, 2 = Rules, 3 = History
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Input state
    private val _contractText = MutableStateFlow("")
    val contractText: StateFlow<String> = _contractText.asStateFlow()

    private val _housingType = MutableStateFlow(HousingType.UNKNOWN)
    val housingType: StateFlow<HousingType> = _housingType.asStateFlow()

    // Analysis state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentReport = MutableStateFlow<AnalysisReport?>(null)
    val currentReport: StateFlow<AnalysisReport?> = _currentReport.asStateFlow()

    private val _sortByRisk = MutableStateFlow(true)
    val sortByRisk: StateFlow<Boolean> = _sortByRisk.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Calculator inputs & state
    private val _rent = MutableStateFlow("4500")
    val rent: StateFlow<String> = _rent.asStateFlow()

    private val _deposit = MutableStateFlow("9000")
    val deposit: StateFlow<String> = _deposit.asStateFlow()

    private val _advance = MutableStateFlow("0")
    val advance: StateFlow<String> = _advance.asStateFlow()

    private val _eRate = MutableStateFlow("8.0")
    val eRate: StateFlow<String> = _eRate.asStateFlow()

    private val _eUnits = MutableStateFlow("150")
    val eUnits: StateFlow<String> = _eUnits.asStateFlow()

    private val _eOfficial = MutableStateFlow("4.20")
    val eOfficial: StateFlow<String> = _eOfficial.asStateFlow()

    private val _wRate = MutableStateFlow("20.0")
    val wRate: StateFlow<String> = _wRate.asStateFlow()

    private val _wUnits = MutableStateFlow("6")
    val wUnits: StateFlow<String> = _wUnits.asStateFlow()

    private val _wOfficial = MutableStateFlow("12.50")
    val wOfficial: StateFlow<String> = _wOfficial.asStateFlow()

    private val _months = MutableStateFlow("12")
    val months: StateFlow<String> = _months.asStateFlow()

    // Calculation result
    private val _calcResult = MutableStateFlow(calculateDifference())
    val calcResult: StateFlow<CalculationResult> = _calcResult.asStateFlow()

    // History
    private val _history = MutableStateFlow<List<AnalysisReport>>(emptyList())
    val history: StateFlow<List<AnalysisReport>> = _history.asStateFlow()

    init {
        // Pre-load default Boonanan sample to let users see immediately
        loadSample(LegalData.SAMPLES[0])
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setContractText(text: String) {
        _contractText.value = text
        _errorMessage.value = null
    }

    fun setHousingType(type: HousingType) {
        _housingType.value = type
    }

    fun setSortByRisk(sort: Boolean) {
        _sortByRisk.value = sort
    }

    fun loadSample(sample: SampleContract) {
        _contractText.value = sample.text
        _currentReport.value = null
        _errorMessage.value = null
        // Auto-set matching calculator inputs if Boonanan
        if (sample.key == "boonanan") {
            _rent.value = "4500"
            _deposit.value = "9000"
            _advance.value = "0"
            _eRate.value = "8.0"
            _wRate.value = "20.0"
            recalc()
        }
    }

    fun clearContract() {
        _contractText.value = ""
        _currentReport.value = null
        _errorMessage.value = null
    }

    fun runAnalysis() {
        val text = _contractText.value.trim()
        if (text.isBlank()) {
            _errorMessage.value = "ยังไม่มีสัญญาให้ตรวจ กรุณาพิมพ์/วางข้อความ หรือเลือกสัญญาตัวอย่าง"
            return
        }

        val clauses = TextExtractor.splitClauses(text)
        if (clauses.isEmpty()) {
            _errorMessage.value = "อ่านข้อความจากสัญญาไม่ได้ กรุณาตรวจสอบรูปแบบข้อความ"
            return
        }

        _isAnalyzing.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val (analyzedClauses, overview) = GeminiContractService.analyzeContract(
                    clauses = clauses,
                    housing = _housingType.value
                )

                val title = clauses.firstOrNull()?.take(50) ?: "สัญญาเช่าที่พัก"
                val report = AnalysisReport(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    title = title,
                    housingType = _housingType.value,
                    clauses = analyzedClauses,
                    overview = overview,
                    rawText = text
                )
                _currentReport.value = report

                // Save to in-memory history list
                val updatedHistory = listOf(report) + _history.value.filter { it.id != report.id }
                _history.value = updatedHistory
            } catch (e: Exception) {
                _errorMessage.value = "เกิดข้อผิดพลาดในการวิเคราะห์: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun updateCalc(
        rentVal: String? = null,
        depositVal: String? = null,
        advanceVal: String? = null,
        eRateVal: String? = null,
        eUnitsVal: String? = null,
        eOfficialVal: String? = null,
        wRateVal: String? = null,
        wUnitsVal: String? = null,
        wOfficialVal: String? = null,
        monthsVal: String? = null
    ) {
        rentVal?.let { _rent.value = it }
        depositVal?.let { _deposit.value = it }
        advanceVal?.let { _advance.value = it }
        eRateVal?.let { _eRate.value = it }
        eUnitsVal?.let { _eUnits.value = it }
        eOfficialVal?.let { _eOfficial.value = it }
        wRateVal?.let { _wRate.value = it }
        wUnitsVal?.let { _wUnits.value = it }
        wOfficialVal?.let { _wOfficial.value = it }
        monthsVal?.let { _months.value = it }
        recalc()
    }

    private fun recalc() {
        _calcResult.value = calculateDifference()
    }

    private fun calculateDifference(): CalculationResult {
        val r = _rent.value.toDoubleOrNull() ?: 0.0
        val dep = _deposit.value.toDoubleOrNull() ?: 0.0
        val adv = _advance.value.toDoubleOrNull() ?: 0.0
        val er = _eRate.value.toDoubleOrNull() ?: 0.0
        val eu = _eUnits.value.toDoubleOrNull() ?: 0.0
        val eo = _eOfficial.value.toDoubleOrNull() ?: 0.0
        val wr = _wRate.value.toDoubleOrNull() ?: 0.0
        val wu = _wUnits.value.toDoubleOrNull() ?: 0.0
        val wo = _wOfficial.value.toDoubleOrNull() ?: 0.0
        val m = (_months.value.toIntOrNull() ?: 12).coerceAtLeast(1)

        val eDiff = maxOf(0.0, (er - eo) * eu)
        val wDiff = maxOf(0.0, (wr - wo) * wu)
        val cap = 3.0 * r
        val collected = dep + adv
        val over = maxOf(0.0, collected - cap)
        val yearlyUtil = (eDiff + wDiff) * m
        val total = yearlyUtil + over

        return CalculationResult(
            eDiff = eDiff,
            wDiff = wDiff,
            cap = cap,
            collected = collected,
            over = over,
            yearlyUtil = yearlyUtil,
            total = total
        )
    }

    fun loadFromHistory(report: AnalysisReport) {
        _currentReport.value = report
        _contractText.value = report.rawText
        _housingType.value = report.housingType
        _selectedTab.value = 0
    }

    fun deleteHistory(id: String) {
        _history.value = _history.value.filter { it.id != id }
    }
}
