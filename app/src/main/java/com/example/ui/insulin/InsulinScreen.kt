package com.example.ui.insulin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dashboard.McSlotItem
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun InsulinScreen(mainViewModel: MainViewModel) {
    val insulinRecords by mainViewModel.repository.allInsulinRecords.collectAsState(initial = emptyList())
    
    // Get start/end of the current week (Monday 00:00 to Sunday 23:59:59)
    val weekBounds = remember(insulinRecords) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        if (cal.timeInMillis > System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, -1)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val end = cal.timeInMillis // Next Monday
        start to end
    }

    val currentWeekInsulins = remember(insulinRecords, weekBounds) {
        insulinRecords.filter { it.timestamp in weekBounds.first until weekBounds.second }
    }

    // Novorapid Calculations
    val novorapidRecords = currentWeekInsulins.filter { it.insulinType == "Novorapid" }
    val novorapidBodySum = novorapidRecords.sumOf { it.dose }
    val novorapidPrimeSum = novorapidRecords.sumOf { it.primeDose }
    val novorapidTotalSum = novorapidBodySum + novorapidPrimeSum

    // Tresiba Calculations
    val tresibaRecords = currentWeekInsulins.filter { it.insulinType == "Tresiba" }
    val tresibaBodySum = tresibaRecords.sumOf { it.dose }
    val tresibaPrimeSum = tresibaRecords.sumOf { it.primeDose }
    val tresibaTotalSum = tresibaBodySum + tresibaPrimeSum

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(McDarkStone)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "АНАЛИТИКА ИНСУЛИНОВ И РАСХОДА",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = McGold,
                fontFamily = FontFamily.Monospace
            )

            // Minecraft statistics label block 1: NOVORAPID
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "📈 НОВОРАПИД (PLAYER STATS)",
                    fontSize = 12.sp,
                    color = McGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                
                McSlotItem(borderColor = McGrass) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(
                            label = "ВВЕДЕНО В ТЕЛО: ",
                            value = String.format(Locale.US, "%.1f ЕД", novorapidBodySum),
                            valueColor = McWhite
                        )
                        StatRow(
                            label = "ТЕХНИЧЕСКИЙ СБРОС (ПРАЙМ): ",
                            value = String.format(Locale.US, "%.1f ЕД", novorapidPrimeSum),
                            valueColor = McRedstone
                        )
                        Divider(color = Color.Black, thickness = 2.dp)
                        StatRow(
                            label = "ВСЕГО ПОТРАЧЕНО: ",
                            value = String.format(Locale.US, "%.1f ЕД", novorapidTotalSum),
                            valueColor = McGold
                        )
                    }
                }
            }

            // Minecraft statistics label block 2: TRESIBA
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "📉 ТРЕСИБА (BASAL STATS)",
                    fontSize = 12.sp,
                    color = McGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                
                McSlotItem(borderColor = McDirt) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(
                            label = "ВВЕДЕНО В ТЕЛО: ",
                            value = String.format(Locale.US, "%.1f ЕД", tresibaBodySum),
                            valueColor = McWhite
                        )
                        StatRow(
                            label = "ТЕХНИЧЕСКИЙ СБРОС (ПРАЙМ): ",
                            value = String.format(Locale.US, "%.1f ЕД", tresibaPrimeSum),
                            valueColor = McRedstone
                        )
                        Divider(color = Color.Black, thickness = 2.dp)
                        StatRow(
                            label = "ВСЕГО ПОТРАЧЕНО: ",
                            value = String.format(Locale.US, "%.1f ЕД", tresibaTotalSum),
                            valueColor = McGold
                        )
                    }
                }
            }

            // Game hint banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(1.dp)
                    .background(Color(0xFF32485C))
                    .padding(10.dp)
            ) {
                Text(
                    text = "📎 ИТОГИ ПОДШИТЫ ИСКЛЮЧИТЕЛЬНО ЗА КАРТУ МЕЛИОРАЦИИ С ПОНЕДЕЛЬНИКА ПО ВОСКРЕСЕНЬЕ ЕДИНОВРЕМЕННО.",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
