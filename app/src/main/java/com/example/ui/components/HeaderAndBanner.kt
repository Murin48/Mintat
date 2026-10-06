package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    onCallOcpb: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛡️", fontSize = 20.sp)
                }

                Text(
                    text = "LeaseCheck",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Surface(
                    color = BlueBadge,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "สคบ. 2568",
                        color = NavyPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            FilledTonalButton(
                onClick = onCallOcpb,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = RiskRedChip,
                    contentColor = RiskRedText
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("header_call_ocpb_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call 1166",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "โทร 1166",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun HeroBanner(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(NavyPrimary, NavyDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "⚖️", fontSize = 20.sp)
                    Text(
                        text = "ระบบตรวจสอบสัญญาเช่าหอพักอัตโนมัติ",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "วิเคราะห์ข้อสัญญาเทียบกับเกณฑ์ สคบ. พ.ศ. 2568 จำแนกข้อสัญญาตามสี 3 ระดับ เพื่อช่วยให้ผู้เช่าเข้าใจสิทธิของตัวเอง",
                    color = Color(0xFFDBEAFE),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // 3-Level Risk Chips
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    RiskChip(
                        emoji = "🔴",
                        text = "สีแดง: เข้าข่ายต้องห้าม / เสี่ยงสูง",
                        bg = Color(0x30EF4444),
                        border = Color(0xFFF87171),
                        textColor = Color(0xFFFECACA)
                    )
                    RiskChip(
                        emoji = "🟡",
                        text = "สีเหลือง: ข้อควรระวัง / เสี่ยงปานกลาง",
                        bg = Color(0x30FACC15),
                        border = Color(0xFFFACC15),
                        textColor = Color(0xFFFEF08A)
                    )
                    RiskChip(
                        emoji = "🟢",
                        text = "สีเขียว: เป็นธรรม / สอดคล้องเกณฑ์",
                        bg = Color(0x3010B981),
                        border = Color(0xFF34D399),
                        textColor = Color(0xFFA7F3D0)
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskChip(
    emoji: String,
    text: String,
    bg: Color,
    border: Color,
    textColor: Color
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OcpbAssistanceCard(
    modifier: Modifier = Modifier,
    onCall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RiskRedChip),
        border = androidx.compose.foundation.BorderStroke(1.dp, RiskRedBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "📞 ถูกเอาเปรียบ? ปรึกษา สคบ.",
                    fontWeight = FontWeight.Bold,
                    color = RiskRedText,
                    fontSize = 15.sp
                )
                Text(
                    text = "สายด่วนร้องเรียนผู้บริโภค ให้คำปรึกษาและรับเรื่องร้องเรียนฟรี",
                    color = Color(0xFF7F1D1D),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = RiskRed),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier.testTag("hotline_call_button")
            ) {
                Text(
                    text = "โทร 1166",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
