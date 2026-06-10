package com.example.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.BgRecord
import com.example.data.InsulinRecord
import com.example.data.MealRecord
import com.example.ui.theme.*
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.MainViewModel
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    mainViewModel: MainViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val stats by dashboardViewModel.dashboardStats.collectAsState()
    val isFootball by mainViewModel.isFootballModeActive.collectAsState()
    val activeInsulin by mainViewModel.activeInsulin.collectAsState()
    val latestBg by mainViewModel.latestBg.collectAsState()
    val selectedPeriod by dashboardViewModel.selectedPeriod.collectAsState()
    val ukResult by mainViewModel.ukAnalysisState.collectAsState()
    
    val context = LocalContext.current

    // Pulsing Redstone color animation for borders
    val infiniteTransition = rememberInfiniteTransition(label = "redstone_pulse")
    val pulseColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF6F0E0E),
        targetValue = Color(0xFFFF2E2E),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_color"
    )

    // Redstone Border when Football scenario is active
    val borderModifier = if (isFootball) {
        Modifier
            .fillMaxSize()
            .border(6.dp, pulseColor)
            .padding(6.dp)
    } else {
        Modifier.fillMaxSize()
    }

    MinecraftBackground(
        modifier = borderModifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title and last sugar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo box
                    Box(
                        modifier = Modifier
                            .border(BorderStroke(2.dp, Color.Black))
                            .background(Color(0xFF272727))
                            .padding(2.dp)
                            .border(BorderStroke(2.dp, McGold))
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Дневник СД1",
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    
                    if (latestBg != null) {
                        val isGlucometer = true // All are manual in our offline app
                        val borderCol = McGold
                        val textValue = "⭐" + String.format(Locale.US, "%.1f", latestBg!!.bgValue)
                        val textColor = getBgColorCode(latestBg!!.bgValue)
                        val subText = "ГЛЮКОМЕТР"

                        McSlotItem(
                            modifier = Modifier.width(110.dp),
                            borderColor = borderCol
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isFootball) {
                                        Text(
                                            text = "⚔️",
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(end = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = textValue,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = subText,
                                    fontSize = 9.sp,
                                    color = McGold,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Empty sugar indicator
                        McSlotItem(
                            modifier = Modifier.width(110.dp),
                            borderColor = Color.Gray
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "--",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "НЕТ ЗАМЕРОВ",
                                    fontSize = 9.sp,
                                    color = Color.LightGray,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Блок 1 (УК и Активный инсулин)
            item {
                Column {
                    BlockHeader("БЛОК 1: УК И АКТИВНЫЙ ИНСУЛИН")
                    McSlotItem {
                        Column(modifier = Modifier.padding(6.dp)) {
                            if (!ukResult.isReady) {
                                Text(
                                    text = "АНАЛИЗ УК: ИДЕТ СБОР ДАННЫХ (ДЕНЬ ${ukResult.daysCollected} ИЗ 7)",
                                    color = McGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "АНАЛИЗ УК: ДАННЫЕ РАССЧИТАНЫ",
                                    color = McGrass,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "УТРО (0-12): " + String.format(Locale.US, "%.1f", ukResult.ukMorning),
                                        fontSize = 11.sp,
                                        color = McWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "ДЕНЬ (13-15): " + String.format(Locale.US, "%.1f", ukResult.ukDay),
                                        fontSize = 11.sp,
                                        color = McWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "ВЕЧЕР (16-20): " + String.format(Locale.US, "%.1f", ukResult.ukEvening),
                                        fontSize = 11.sp,
                                        color = McWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "НОЧЬ (21-23): " + String.format(Locale.US, "%.1f", ukResult.ukNight),
                                        fontSize = 11.sp,
                                        color = McWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "IOB (АКТИВНЫЙ)",
                                        fontSize = 10.sp,
                                        color = Color.LightGray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.2f ЕД", activeInsulin),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = McGold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Блок 2 (Статистика Тарелок)
            item {
                Column {
                    BlockHeader("БЛОК 2: СТАТИСТИКА ТАРЕЛОК")
                    McSlotItem {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "СБАЛАНСИРОВАННЫХ ПРИЕМОВ ПИЩИ: ${stats.balancedMeals}",
                                fontSize = 12.sp,
                                color = McGrass,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ГОЛЫХ УГЛЕВОДОВ (НЕСБАЛАНСИРОВАННЫХ): ${stats.nakedCarbs}",
                                fontSize = 12.sp,
                                color = McRedstone,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Блок 3 (Итоги)
            item {
                Column {
                    BlockHeader("БЛОК 3: ИТОГИ")
                    McSlotItem {
                        Column(modifier = Modifier.padding(6.dp)) {
                            // Period Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val periods = listOf(
                                    DashboardViewModel.TimePeriod.TODAY to "СЕГОДНЯ",
                                    DashboardViewModel.TimePeriod.YESTERDAY to "ВЧЕРА",
                                    DashboardViewModel.TimePeriod.WEEK to "НЕДЕЛЯ"
                                )
                                periods.forEach { (p, label) ->
                                    val isSelected = selectedPeriod == p
                                    Text(
                                        text = "[$label]",
                                        color = if (isSelected) McGold else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clickable { dashboardViewModel.selectedPeriod.value = p }
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                McSumItem(title = "ХЕ", value = String.format(Locale.US, "%.1f", stats.totalXe))
                                McSumItem(title = "НОВОРАПИД", value = String.format(Locale.US, "%.1f", stats.totalNovorapid))
                                McSumItem(title = "ТРЕСИБА", value = String.format(Locale.US, "%.1f", stats.totalTresiba))
                            }
                        }
                    }
                }
            }

            // Блок 4 (Интервалы глюкозы)
            item {
                Column {
                    BlockHeader("БЛОК 4: ИНТЕРВАЛЫ ГЛЮКОЗЫ")
                    McSlotItem {
                        Column(modifier = Modifier.padding(6.dp)) {
                            GlucoseIntervalsMinecraft(stats.bgRecords)
                        }
                    }
                }
            }

            // Блок 5 (Индикация Футбола)
            item {
                Column {
                    BlockHeader("БЛОК 5: ИНДИКАЦИЯ ФУТБОЛА")
                    McSlotItem(borderColor = if (isFootball) McRedstone else Color.Black) {
                        Column(
                            modifier = Modifier.padding(6.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isFootball) {
                                Text(
                                    text = "⚔️ РЕЖИМ БИТВЫ АКТИВЕН ⚔️",
                                    color = McRedstone,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Трофим тренируется! Рамка экрана пульсирует редстоуном, а все записи помечаются меткой спортивной сессии.",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "🛡️ ТРЕНИРОВКА НЕАКТИВНА",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Конец списка
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BlockHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        color = McGold,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
    )
}

@Composable
fun McSumItem(title: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color.Black)
            .padding(1.dp)
            .background(Color(0xFF3C3C3C))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = McGold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun GlucoseIntervalsMinecraft(records: List<BgRecord>) {
    val morning = mutableListOf<BgRecord>()
    val day = mutableListOf<BgRecord>()
    val evening = mutableListOf<BgRecord>()
    val night = mutableListOf<BgRecord>()

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    records.forEach { record ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = record.timestamp
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..12 -> morning.add(record)
            in 13..15 -> day.add(record)
            in 16..20 -> evening.add(record)
            else -> night.add(record)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        McIntervalRow("УТРО (00:00-12:00)", morning, timeFormat)
        McIntervalRow("ОБЕД (13:00-15:00)", day, timeFormat)
        McIntervalRow("ВЕЧЕР (16:00-20:00)", evening, timeFormat)
        McIntervalRow("НОЧЬ (21:00-00:00)", night, timeFormat)
    }
}

@Composable
fun McIntervalRow(title: String, records: List<BgRecord>, timeFormat: SimpleDateFormat) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        if (records.isEmpty()) {
            Text(
                text = "НЕТ ЗАМЕРОВ",
                fontSize = 10.sp,
                color = Color.DarkGray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(1.dp)
                    .background(Color(0xFF272727))
                    .padding(6.dp)
            ) {
                val itemsData = records.take(6).map { 
                    val time = timeFormat.format(Date(it.timestamp))
                    val bgVal = String.format(Locale.US, "%.1f", it.bgValue)
                    val isGluco = true
                    Triple(time, bgVal, isGluco)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsData.forEachIndexed { index, triple ->
                        val time = triple.first
                        val bgVal = triple.second
                        val isGluco = triple.third
                        
                        Text(
                            text = time,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        val displayValue = "⭐$bgVal"
                        val textColor = getBgColorCode(bgVal.toDoubleOrNull() ?: 5.5)
                        
                        Text(
                            text = displayValue,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (index < itemsData.size - 1) {
                            Text(
                                text = " | ",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getBgColorCode(bg: Double): Color {
    return when {
        bg > 9.8 -> McRedstone // High (Redstone)
        bg < 4.0 -> Color(0xFF55FFFF) // Hypo (Light Blue/Cyan)
        else -> McGrass // Normal (Grass green)
    }
}

@Composable
fun McSlotItem(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Black,
    backgroundColor: Color = Color(0xFF4A4A4A),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(borderColor)
            .padding(2.dp) // pixel edge
            .background(backgroundColor)
            .padding(10.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun McPixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF8C8C8C),
    borderColor: Color = Color.Black,
    textColor: Color = McWhite
) {
    Box(
        modifier = modifier
            .background(borderColor)
            .padding(2.dp) // black outline
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

private fun generateCsvFile(
    context: Context,
    meals: List<MealRecord>,
    bgs: List<BgRecord>,
    insulins: List<InsulinRecord>
): File {
    val dir = File(context.cacheDir, "exports")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "diary_export.csv")
    val writer = FileWriter(file)
    
    writer.append("Type,Timestamp,Food_Description,Value1,Value2,Scenario\n")
    
    bgs.forEach {
        writer.append("BG,${it.timestamp},,${it.bgValue},${it.direction},${it.scenario}\n")
    }
    meals.forEach {
        writer.append("Meal,${it.timestamp},${it.foodText},${it.xe},${it.novorapidDose},${it.scenario}\n")
    }
    insulins.forEach {
        writer.append("Insulin,${it.timestamp},,${it.insulinType},${it.dose},${it.scenario}\n")
    }
    
    writer.flush()
    writer.close()
    return file
}

private fun shareCsvFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "ОТПРАВИТЬ СД1 КАРТУ"))
}
