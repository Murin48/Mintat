package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OcpbAssistanceCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.DecimalFormat

@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    onCallOcpb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rent by viewModel.rent.collectAsState()
    val deposit by viewModel.deposit.collectAsState()
    val advance by viewModel.advance.collectAsState()
    val eRate by viewModel.eRate.collectAsState()
    val eUnits by viewModel.eUnits.collectAsState()
    val eOfficial by viewModel.eOfficial.collectAsState()
    val wRate by viewModel.wRate.collectAsState()
    val wUnits by viewModel.wUnits.collectAsState()
    val wOfficial by viewModel.wOfficial.collectAsState()
    val months by viewModel.months.collectAsState()
    val result by viewModel.calcResult.collectAsState()

    val formatter = remember { DecimalFormat("#,##0.00") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧮", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "เครื่องคำนวณส่วนต่างค่าน้ำ-ค่าไฟ และเงินประกัน",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "เปรียบเทียบอัตราที่หอพักเรียกเก็บจริง กับอัตราทางการตามเกณฑ์ สคบ. พ.ศ. 2568 เพื่อดูยอดเงินที่ถูกเก็บเกิน",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    HorizontalDivider(color = CardBorder)

                    // 1. Rent and Deposits
                    Text(
                        text = "1. ค่าเช่าและเงินประกัน (บาท)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyPrimary
                    )

                    OutlinedTextField(
                        value = rent,
                        onValueChange = { viewModel.updateCalc(rentVal = it) },
                        label = { Text("ค่าเช่าห้องพัก (บาท/เดือน)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("calc_rent_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = deposit,
                            onValueChange = { viewModel.updateCalc(depositVal = it) },
                            label = { Text("เงินประกันที่จ่าย (บาท)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("calc_deposit_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = advance,
                            onValueChange = { viewModel.updateCalc(advanceVal = it) },
                            label = { Text("ค่าเช่าล่วงหน้า (บาท)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("calc_advance_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    HorizontalDivider(color = CardBorder)

                    // 2. Electricity
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2. ค่าไฟฟ้า",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = eRate,
                            onValueChange = { viewModel.updateCalc(eRateVal = it) },
                            label = { Text("หอพักคิด (บ./หน่วย)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = eUnits,
                            onValueChange = { viewModel.updateCalc(eUnitsVal = it) },
                            label = { Text("หน่วยไฟที่ใช้/เดือน") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = eOfficial,
                            onValueChange = { viewModel.updateCalc(eOfficialVal = it) },
                            label = { Text("อัตราทางการ (บ.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    HorizontalDivider(color = CardBorder)

                    // 3. Water
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💧", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3. ค่าน้ำประปา",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wRate,
                            onValueChange = { viewModel.updateCalc(wRateVal = it) },
                            label = { Text("หอพักคิด (บ./หน่วย)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = wUnits,
                            onValueChange = { viewModel.updateCalc(wUnitsVal = it) },
                            label = { Text("หน่วยน้ำที่ใช้/เดือน") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = wOfficial,
                            onValueChange = { viewModel.updateCalc(wOfficialVal = it) },
                            label = { Text("อัตราทางการ (บ.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    HorizontalDivider(color = CardBorder)

                    // 4. Contract Months
                    OutlinedTextField(
                        value = months,
                        onValueChange = { viewModel.updateCalc(monthsVal = it) },
                        label = { Text("ระยะเวลาสัญญาเช่า (เดือน)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Calculation Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().testTag("calc_summary_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📊 ผลสรุปยอดเงินที่ถูกเก็บเกินจริง",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    HorizontalDivider(color = CardBorder)

                    // Electricity Overcharge
                    ResultRow(
                        label = "ส่วนต่างค่าไฟต่อเดือน (ฐาน $eOfficial บ./หน่วย):",
                        value = if (result.eDiff > 0) "+${formatter.format(result.eDiff)} บาท/เดือน" else "ไม่คิดเกิน",
                        isBad = result.eDiff > 0
                    )

                    // Water Overcharge
                    ResultRow(
                        label = "ส่วนต่างค่าน้ำต่อเดือน (ฐาน $wOfficial บ./หน่วย):",
                        value = if (result.wDiff > 0) "+${formatter.format(result.wDiff)} บาท/เดือน" else "ไม่คิดเกิน",
                        isBad = result.wDiff > 0
                    )

                    // Deposit Ceiling Check
                    ResultRow(
                        label = "เงินประกัน + ล่วงหน้า (รวม ${formatter.format(result.collected)} บ.) เทียบเพดาน 3 เดือน (${formatter.format(result.cap)} บ.):",
                        value = if (result.over > 0) "+${formatter.format(result.over)} บาท (เกินเพดาน)" else "ไม่เกินเพดาน (ถูกต้อง)",
                        isBad = result.over > 0
                    )

                    HorizontalDivider(color = CardBorder)

                    // Total Surcharge Loss
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "รวมยอดเสียเปรียบ:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "(น้ำ-ไฟ $months เดือน + ส่วนเกินเงินประกัน)",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = "${formatter.format(result.total)} บาท",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = if (result.total > 0) RiskRed else RiskGreen
                        )
                    }

                    // Tip box
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "💡 ส่วนที่เก็บเกินอาจขอคืนได้ หรือร้องเรียนต่อ สคบ. สายด่วน 1166 ควรเก็บบิลและสัญญาไว้เป็นหลักฐาน ทั้งนี้เพดานเงินประกันใช้เมื่อเข้าข่ายประกาศ สคบ. (ผู้ให้เช่าตั้งแต่ 3 หน่วยขึ้นไป)",
                            color = NavyPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        item {
            OcpbAssistanceCard(onCall = onCallOcpb)
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    isBad: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isBad) RiskRedText else RiskGreenText
        )
    }
}
