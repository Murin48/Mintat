package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.legal.LegalData
import com.example.ui.components.AppHeader
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CheckScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.theme.LeaseCheckTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LeaseCheckTheme {
                MainApp(
                    viewModel = viewModel,
                    onCallOcpb = { dialOcpb() }
                )
            }
        }
    }

    private fun dialOcpb() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${LegalData.OCPB_HOTLINE}")
        }
        startActivity(intent)
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel,
    onCallOcpb: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(onCallOcpb = onCallOcpb)
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Check") },
                    label = { Text("ตรวจสอบสัญญา", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFDBEAFE)
                    ),
                    modifier = Modifier.testTag("nav_check_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Calculator") },
                    label = { Text("คำนวณส่วนต่าง", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFDBEAFE)
                    ),
                    modifier = Modifier.testTag("nav_calc_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Rules") },
                    label = { Text("เกณฑ์ สคบ.", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFDBEAFE)
                    ),
                    modifier = Modifier.testTag("nav_rules_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "History") },
                    label = { Text("ประวัติ", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFDBEAFE)
                    ),
                    modifier = Modifier.testTag("nav_history_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CheckScreen(viewModel = viewModel, onCallOcpb = onCallOcpb)
                1 -> CalculatorScreen(viewModel = viewModel, onCallOcpb = onCallOcpb)
                2 -> RulesScreen(onCallOcpb = onCallOcpb)
                3 -> HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
