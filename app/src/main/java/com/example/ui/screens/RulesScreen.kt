package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.legal.LegalData
import com.example.data.model.LegalRule
import com.example.ui.components.OcpbAssistanceCard
import com.example.ui.theme.*

@Composable
fun RulesScreen(
    onCallOcpb: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rules_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OcpbAssistanceCard(onCall = onCallOcpb)
        }

        // Section 1: 10 Prohibitions
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚫", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "10 ข้อห้ามของผู้ให้เช่า (ประกาศ สคบ. พ.ศ. 2568)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Text(
                            text = "📌 ขอบเขต: ใช้กับผู้ให้เช่าที่มีที่พักให้เช่าตั้งแต่ 3 หน่วยขึ้นไป (เช่น อพาร์ตเมนต์ ห้องเช่า) ข้อสัญญาที่ฝ่าฝืนถือว่าไม่มีผลบังคับ\n⚖️ บทลงโทษ: ผู้ประกอบธุรกิจที่ฝ่าฝืนมีโทษจำคุกไม่เกิน 1 ปี ปรับไม่เกิน 200,000 บาท หรือทั้งจำทั้งปรับ ตาม พ.ร.บ.คุ้มครองผู้บริโภค",
                            color = Color(0xFF78350F),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegalData.PROHIBITIONS.forEachIndexed { index, rule ->
                            LegalRuleCard(index = index + 1, rule = rule)
                        }
                    }
                }
            }
        }

        // Section 2: Tenant Rights
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "สิทธิของผู้เช่าที่กฎหมายคุ้มครอง",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegalData.RIGHTS.forEachIndexed { index, rule ->
                            LegalRuleCard(index = index + 1, rule = rule)
                        }
                    }
                }
            }
        }

        // Section 3: Registered Dormitories
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏫", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "กรณีหอพักจดทะเบียนตาม พ.ร.บ.หอพัก พ.ศ. 2558",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LegalData.DORM_NOTES.forEachIndexed { index, note ->
                            Surface(
                                color = LightSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = BlueBadge,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "หอพัก",
                                            color = NavyPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${index + 1}. $note",
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Disclaimer Footer
        item {
            Text(
                text = LegalData.DISCLAIMER,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LegalRuleCard(
    index: Int,
    rule: LegalRule
) {
    Surface(
        color = if (rule.isBan) RiskRedBg else RiskGreenBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rule.isBan) RiskRedBorder else RiskGreenBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (rule.isBan) RiskRedChip else RiskGreenChip,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = rule.tag,
                    color = if (rule.isBan) RiskRedText else RiskGreenText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$index. ${rule.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                if (rule.detail.isNotBlank()) {
                    Text(
                        text = rule.detail,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
