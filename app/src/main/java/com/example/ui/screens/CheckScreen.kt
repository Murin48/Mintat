package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.legal.LegalData
import com.example.data.model.*
import com.example.ui.components.HeroBanner
import com.example.ui.components.OcpbAssistanceCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CheckScreen(
    viewModel: MainViewModel,
    onCallOcpb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contractText by viewModel.contractText.collectAsState()
    val housingType by viewModel.housingType.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val currentReport by viewModel.currentReport.collectAsState()
    val sortByRisk by viewModel.sortByRisk.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Remember sorted clauses at composable top-level (outside LazyColumn builder)
    val sortedClauses = remember(currentReport, sortByRisk) {
        val report = currentReport ?: return@remember emptyList()
        if (sortByRisk) {
            report.clauses.sortedBy { it.risk.sortOrder }
        } else {
            report.clauses.sortedBy { it.no }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("check_screen_scroll"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            HeroBanner()
        }

        // Section 1: Select or Paste Contract
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. เลือกสัญญาเช่าเพื่อตรวจสอบ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = "— หรือทดลองด้วยสัญญาจำลอง 3 รูปแบบ (สมมติขึ้นเพื่อทดสอบ) —",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    // 3 Sample Contracts
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LegalData.SAMPLES.forEach { sample ->
                            SampleContractCard(
                                sample = sample,
                                isSelected = contractText == sample.text,
                                onSelect = { viewModel.loadSample(sample) }
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorder)

                    // Text Field for Contract Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ข้อความสัญญาเช่า:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        if (contractText.isNotBlank()) {
                            TextButton(
                                onClick = { viewModel.clearContract() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("ล้างข้อความ", fontSize = 12.sp, color = RiskRedText)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = contractText,
                        onValueChange = { viewModel.setContractText(it) },
                        placeholder = {
                            Text(
                                "พิมพ์หรือวางข้อความสัญญาเช่าลงในช่องนี้...\nตัวอย่างเช่น:\nข้อ 1. ค่าเช่าเดือนละ 4,500 บาท\nข้อ 2. เงินประกัน 2 เดือน...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 260.dp)
                            .testTag("contract_text_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Housing Type Radio Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ประเภทที่พัก (ถ้าทราบ ช่วยให้ผลตรงขึ้น):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HousingType.values().forEach { type ->
                                FilterChip(
                                    selected = housingType == type,
                                    onClick = { viewModel.setHousingType(type) },
                                    label = {
                                        Text(
                                            text = type.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (housingType == type) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BlueBadge,
                                        selectedLabelColor = NavyPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Privacy Notice
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BlueAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "🔒 ระบบปิดเลขบัตรประชาชน เบอร์โทร อีเมลให้อัตโนมัติก่อนส่งวิเคราะห์ เพื่อความปลอดภัยของข้อมูลส่วนตัว",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = RiskRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Run Analysis Button
                    Button(
                        onClick = { viewModel.runAnalysis() },
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_analysis_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "กำลังวิเคราะห์สัญญาตามเกณฑ์ สคบ. ...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ เริ่มตรวจสอบสัญญาตามเกณฑ์ สคบ.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Results Display
        if (currentReport != null) {
            val report = currentReport!!

            item {
                Text(
                    text = "ผลการตรวจสอบสัญญา",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Stats Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "ข้อทั้งหมด",
                        count = "${report.totalCount}",
                        bg = RiskGrayBg,
                        border = RiskGrayBorder,
                        textColor = RiskGray,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "🔴 เสี่ยงสูง",
                        count = "${report.highRiskCount}",
                        bg = RiskRedBg,
                        border = RiskRedBorder,
                        textColor = RiskRedText,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "🟡 ควรระวัง",
                        count = "${report.mediumRiskCount}",
                        bg = RiskYellowBg,
                        border = RiskYellowBorder,
                        textColor = RiskYellowText,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "🟢 เป็นธรรม",
                        count = "${report.lowRiskCount}",
                        bg = RiskGreenBg,
                        border = RiskGreenBorder,
                        textColor = RiskGreenText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Contract Overview Summary Card
            if (report.overview != null) {
                item {
                    ContractOverviewCard(overview = report.overview)
                }
            }

            // Sort Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "รายการข้อสัญญา (${report.clauses.size} ข้อ):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "เรียงข้อเสี่ยงสูงไว้บนสุด",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Switch(
                            checked = sortByRisk,
                            onCheckedChange = { viewModel.setSortByRisk(it) },
                            modifier = Modifier.testTag("sort_by_risk_switch")
                        )
                    }
                }
            }

            // Clause items
            items(sortedClauses, key = { "${it.no}_${it.rawText.hashCode()}" }) { clause ->
                ClauseDetailCard(
                    clause = clause,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            item {
                OcpbAssistanceCard(onCall = onCallOcpb)
            }
        }
    }
}

@Composable
private fun SampleContractCard(
    sample: SampleContract,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        color = if (isSelected) sample.riskClass.backgroundColor() else Color(0xFFFAFAFA),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) sample.riskClass.borderColor() else CardBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = sample.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sample.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) sample.riskClass.textColor() else TextPrimary
                    )
                }
                Text(
                    text = sample.desc,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            TextButton(
                onClick = onSelect,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isSelected) "เลือกอยู่" else "ใช้ตัวอย่างนี้",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) NavyPrimary else BlueAccent
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    count: String,
    bg: Color,
    border: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun ContractOverviewCard(
    overview: ContractOverview
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "สรุปภาพรวมสัญญา",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyPrimary
                )
            }

            HorizontalDivider(color = CardBorder)

            OverviewRow(label = "ค่าเช่าห้อง:", value = overview.rent)
            OverviewRow(label = "เงินประกัน/มัดจำ:", value = overview.deposit)
            OverviewRow(label = "ค่าน้ำ-ค่าไฟ:", value = overview.utilities)
            OverviewRow(label = "ค่าปรับ/เงื่อนไขเลิกสัญญา:", value = overview.penalty)

            if (overview.missing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ เรื่องสำคัญที่สัญญาไม่ได้ระบุ:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RiskYellowText
                )
                overview.missing.forEach { item ->
                    Text(
                        text = "• $item",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun ClauseDetailCard(
    clause: ContractClause,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(clause.risk == RiskLevel.HIGH) }
    var showRawText by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = clause.risk.backgroundColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, clause.risk.borderColor()),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Emoji, Clause Title, Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = clause.risk.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ข้อ ${clause.no}: " + clause.rawText.take(45).replace("\n", " ") + (if (clause.rawText.length > 45) "..." else ""),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = clause.risk.chipColor(),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = clause.risk.badgeLabel,
                        color = clause.risk.textColor(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = if (expanded) "▲" else "▼",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Summary
                    if (clause.summary.isNotBlank()) {
                        Text(
                            text = clause.summary,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }

                    // Issues
                    if (clause.issues.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "ประเด็นที่ควรระวัง:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = clause.risk.textColor()
                            )
                            clause.issues.forEach { issue ->
                                Row(
                                    modifier = Modifier.padding(start = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "•", fontSize = 12.sp, color = clause.risk.textColor())
                                    Text(text = issue, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    // Suggestion
                    if (clause.suggestion.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "💡 ข้อแนะนำ:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = clause.suggestion,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Law references
                    if (clause.lawRef.isNotEmpty()) {
                        Text(
                            text = "อ้างอิง: ${clause.lawRef.joinToString(" / ")}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    // Toggle Raw Text
                    TextButton(
                        onClick = { showRawText = !showRawText },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showRawText) "ซ่อนข้อความต้นฉบับ ▲" else "ดูข้อความต้นฉบับ ▼",
                            fontSize = 11.sp,
                            color = BlueAccent
                        )
                    }

                    if (showRawText) {
                        Surface(
                            color = Color(0xFFF9FAFB),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = clause.rawText,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
