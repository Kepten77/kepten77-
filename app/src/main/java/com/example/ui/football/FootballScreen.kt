package com.example.ui.football

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BgRecord
import com.example.data.InsulinRecord
import com.example.data.MealRecord
import com.example.ui.dashboard.McPixelButton
import com.example.ui.dashboard.McSlotItem
import com.example.ui.dashboard.getBgColorCode
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FootballScreen(mainViewModel: MainViewModel) {
    val isFootballActive by mainViewModel.isFootballModeActive.collectAsState()
    
    val bgRecords by mainViewModel.footballBgRecords.collectAsState()
    val mealRecords by mainViewModel.footballMealRecords.collectAsState()
    val insulinRecords by mainViewModel.footballInsulinRecords.collectAsState()

    // Grouping all sports records by calendar date
    val sessions = remember(bgRecords, mealRecords, insulinRecords) {
        aggregateFootballSessions(bgRecords, mealRecords, insulinRecords)
    }

    MinecraftBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "ИЗОЛИРОВАННАЯ АНАЛИТИКА: СПОРТ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = McGold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Interactive Toggle for Football Mode inside the tab!
            item {
                val accentColor = if (isFootballActive) McRedstone else Color.Black
                val backgroundCol = if (isFootballActive) Color(0xFF531E1E) else Color(0xFF2E2E2E)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(2.dp, accentColor))
                        .background(backgroundCol)
                        .clickable { mainViewModel.toggleFootballMode() }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "РЕЖИМ ФУТБОЛА",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFootballActive) McGold else McWhite,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isFootballActive) "⚔️ ТРЕНИРОВКА АКТИВНА СЕЙЧАС (ЗАПИСИ АВТО-КЛАССИФИЦИРУЮТСЯ)" else "🛡️ ТРЕНИРОВКА ВЫКЛЮЧЕНА (КЛИКНИ ДЛЯ СТАРТА)",
                                fontSize = 10.sp,
                                color = if (isFootballActive) McRedstone else Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Switch(
                            checked = isFootballActive,
                            onCheckedChange = { mainViewModel.toggleFootballMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = McRedstone,
                                checkedTrackColor = Color.Black,
                                uncheckedThumbColor = McStone,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }
                }
            }

            // List of Sessions or Empty state
            if (sessions.isEmpty()) {
                item {
                    McSlotItem(backgroundColor = Color(0xFF2C2C2C)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "НЕТ СПОРТИВНЫХ СЕССИЙ ЗА НЕДЕЛЮ",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Все записи с метками 'football_active' или 'football_control' автоматически появятся здесь.",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(sessions) { session ->
                    FootballSessionView(session)
                }
            }
        }
    }
}

@Composable
fun FootballSessionView(session: FootballSession) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.Black))
            .background(Color(0xFF333333))
    ) {
        // Day Header selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = session.dateStr.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = McGold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Записей: ${session.totalRecordsCount} (Активно: ${session.runRecords.size} / Контроль: ${session.controlRecords.size})",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = if (expanded) "[-]" else "[+]",
                fontSize = 14.sp,
                color = McGold,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECTION 1: SNAPSHOT СТАРТА
                Column {
                    Text(
                        text = "⛳ SNAPSHOT СТАРТА (ВХОД):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = McGrass,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(1.dp)
                            .background(Color(0xFF2A2A2A))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Initial Bg
                            val startBg = session.startBg
                            val startBgStr = if (startBg != null) {
                                "${String.format(Locale.US, "%.1f", startBg.bgValue)} ммоль/л (${if (startBg.isFromXdrip) startBg.direction else "ГЛЮКОМЕТР"})"
                            } else {
                                "НЕТ СТАРТОВОГО ЗАМЕРА"
                            }
                            Text(
                                text = "• Стартовый сахар: $startBgStr",
                                fontSize = 11.sp,
                                color = if (startBg != null) getBgColorCode(startBg.bgValue) else Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )

                            // Start Food/XE
                            val startMeals = session.startMeals
                            val startMealsStr = if (startMeals.isNotEmpty()) {
                                startMeals.joinToString(", ") { "${it.xe} ХЕ (${it.foodText.ifBlank { "Перекус" }})" }
                            } else {
                                "0 ХЕ"
                            }
                            Text(
                                text = "• Съедено углеводов: $startMealsStr",
                                fontSize = 11.sp,
                                color = McWhite,
                                fontFamily = FontFamily.Monospace
                            )

                            // Start Insulin
                            val startInsulins = session.startInsulins
                            val startInsulinsStr = if (startInsulins.isNotEmpty()) {
                                startInsulins.joinToString(", ") { "${it.dose} ЕД (${it.insulinType})" }
                            } else {
                                "0 ЕД"
                            }
                            Text(
                                text = "• Инсулин на входе: $startInsulinsStr",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // SECTION 2: В ПРОЦЕССЕ ТРЕНИРОВКИ
                Column {
                    Text(
                        text = "⚔️ В ПРОЦЕССЕ ТРЕНИРОВКИ (ОБНОВЛЕНИЯ НА ПОЛЕ):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = McRedstone,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val midEvents = session.processEvents
                    if (midEvents.isEmpty()) {
                        Text(
                            text = "Нет событий во время игры",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            midEvents.forEach { event ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                        .padding(1.dp)
                                        .background(Color(0xFF2C2525))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = event,
                                        fontSize = 11.sp,
                                        color = McWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 3: ФИНАЛ И КОНЕЦ ИГРЫ
                Column {
                    Text(
                        text = "🏁 ПОКАЗАНИЯ НА КОНЕЦ ИГРЫ:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = McTextGold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(1.dp)
                            .background(Color(0xFF2D2D2D))
                            .padding(8.dp)
                    ) {
                        val endBg = session.endBg
                        if (endBg != null) {
                            val glucoseCol = getBgColorCode(endBg.bgValue)
                            Text(
                                text = "Финальный сахар: ${String.format(Locale.US, "%.1f", endBg.bgValue)} ммоль/л",
                                fontSize = 11.sp,
                                color = glucoseCol,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = "Нет замеров на конец тренировки",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // SECTION 4: ОБЯЗАТЕЛЬНЫЙ ПОСТ-КОНТРОЛЬ ПОСЛЕ ИГРЫ (через 40-60 мин)
                Column {
                    Text(
                        text = "🛡️ КОНТРОЛЬ (ЧЕРЕЗ 40-60 МИНУТ ПОСЛЕ ИГРЫ):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = McWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val controlLogs = session.controlLogs
                    if (controlLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(1.dp)
                                .background(Color(0xFF332020))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ ВНИМАНИЕ! КОНТРОЛЬНЫЙ ЗАМЕР ОТСУТСТВУЕТ.\nЗарегистрируй показания со сценарием 'КОНТРОЛЬ ПОСЛЕ'!",
                                fontSize = 10.sp,
                                color = McRedstone,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            controlLogs.forEach { log ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                        .padding(1.dp)
                                        .background(Color(0xFF1F2922))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = log,
                                        fontSize = 11.sp,
                                        color = McWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data aggregate representing an isolated physical football session on a specific calendar day.
 */
data class FootballSession(
    val dateStr: String,
    val totalRecordsCount: Int,
    val runRecords: List<Any>,
    val controlRecords: List<Any>,
    val startBg: BgRecord?,
    val startMeals: List<MealRecord>,
    val startInsulins: List<InsulinRecord>,
    val processEvents: List<String>,
    val endBg: BgRecord?,
    val controlLogs: List<String>
)

private fun aggregateFootballSessions(
    bgRecords: List<BgRecord>,
    mealRecords: List<MealRecord>,
    insulinRecords: List<InsulinRecord>
): List<FootballSession> {
    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfDisplay = SimpleDateFormat("EEEE dd.MM", Locale("ru"))
    
    // Group everything by calendar date
    val bgsGrouped = bgRecords.groupBy { sdfKey.format(Date(it.timestamp)) }
    val mealsGrouped = mealRecords.groupBy { sdfKey.format(Date(it.timestamp)) }
    val insulinsGrouped = insulinRecords.groupBy { sdfKey.format(Date(it.timestamp)) }

    val allDates = (bgsGrouped.keys + mealsGrouped.keys + insulinsGrouped.keys).sortedDescending()
    val sessionList = mutableListOf<FootballSession>()

    for (dateKey in allDates) {
        val dateBgs = bgsGrouped[dateKey] ?: emptyList()
        val dateMeals = mealsGrouped[dateKey] ?: emptyList()
        val dateInsulins = insulinsGrouped[dateKey] ?: emptyList()

        val activeBgs = dateBgs.filter { it.scenario == "football_active" || it.scenario == "football" }.sortedBy { it.timestamp }
        val activeMeals = dateMeals.filter { it.scenario == "football_active" || it.scenario == "football" }.sortedBy { it.timestamp }
        val activeInsulins = dateInsulins.filter { it.scenario == "football_active" || it.scenario == "football" }.sortedBy { it.timestamp }

        val controlBgs = dateBgs.filter { it.scenario == "football_control" }.sortedBy { it.timestamp }
        val controlMeals = dateMeals.filter { it.scenario == "football_control" }.sortedBy { it.timestamp }
        val controlInsulins = dateInsulins.filter { it.scenario == "football_control" }.sortedBy { it.timestamp }

        if (activeBgs.isEmpty() && activeMeals.isEmpty() && activeInsulins.isEmpty() && 
            controlBgs.isEmpty() && controlMeals.isEmpty() && controlInsulins.isEmpty()) {
            continue
        }

        // 1. Determine start timestamp (earliest of active components)
        val timestamps = listOfNotNull(
            activeBgs.firstOrNull()?.timestamp,
            activeMeals.firstOrNull()?.timestamp,
            activeInsulins.firstOrNull()?.timestamp
        )
        val startThreshold = if (timestamps.isNotEmpty()) timestamps.minOrNull()!! else 0L

        // Earliest BG record as START snapshot (within 30 mins or just the first)
        val startBg = activeBgs.firstOrNull()
        
        // Start insulin and food loaded together at the start (within first 15 minutes of the workout)
        val startMeals = activeMeals.filter { Math.abs(it.timestamp - startThreshold) <= 15 * 60 * 1000L }
        val startInsulins = activeInsulins.filter { Math.abs(it.timestamp - startThreshold) <= 15 * 60 * 1000L }

        // Process updates (anything after the start window components but before the last active BG)
        val lastBg = activeBgs.lastOrNull()
        val endBg = if (activeBgs.size > 1) lastBg else null

        val processEvents = mutableListOf<String>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Add BG updates in process (not including start and end)
        activeBgs.forEach { bg ->
            if (bg != startBg && bg != endBg) {
                processEvents.add("[⏱️ ${timeFormat.format(Date(bg.timestamp))}] ЗАМЕР: ${String.format(Locale.US, "%.1f", bg.bgValue)} ммоль/л (${if (bg.isFromXdrip) bg.direction else "РАЗОВО"})")
            }
        }
        // Add meals in process
        activeMeals.forEach { meal ->
            if (!startMeals.contains(meal)) {
                processEvents.add("[⏱️ ${timeFormat.format(Date(meal.timestamp))}] СВЕЖИЕ УГЛЕВОДЫ: ${meal.xe} ХЕ (${meal.foodText.ifBlank { "Перекус на поле" }})")
            }
        }
        // Add insulin in process
        activeInsulins.forEach { insulin ->
            if (!startInsulins.contains(insulin)) {
                processEvents.add("[⏱️ ${timeFormat.format(Date(insulin.timestamp))}] ПОДКОЛКА: ${insulin.dose} ЕД (${insulin.insulinType})")
            }
        }
        // Sort process events chronologically by timestamps if needed (they are already mostly sorted relative to themselves)

        // Control Logs after Game
        val controlLogs = mutableListOf<String>()
        controlBgs.forEach { bg ->
            controlLogs.add("[⏱️ ${timeFormat.format(Date(bg.timestamp))}] ПОСТ-ЗАМЕР: ${String.format(Locale.US, "%.1f", bg.bgValue)} ммоль/л")
        }
        controlMeals.forEach { meal ->
            controlLogs.add("[⏱️ ${timeFormat.format(Date(meal.timestamp))}] СЪЕДЕНО: ${meal.xe} ХЕ (${meal.foodText})")
        }
        controlInsulins.forEach { insulin ->
            controlLogs.add("[⏱️ ${timeFormat.format(Date(insulin.timestamp))}] ПОДКОЛКА: ${insulin.dose} ЕД (${insulin.insulinType})")
        }

        // Format Russian Title
        var formattedDate = "Тренировка"
        try {
            val dateObj = sdfKey.parse(dateKey)
            if (dateObj != null) {
                val rawFormatted = sdfDisplay.format(dateObj)
                formattedDate = rawFormatted.substring(0, 1).uppercase() + rawFormatted.substring(1)
            }
        } catch (e: Exception) {
            formattedDate = dateKey
        }

        sessionList.add(
            FootballSession(
                dateStr = formattedDate,
                totalRecordsCount = activeBgs.size + activeMeals.size + activeInsulins.size + controlBgs.size + controlMeals.size + controlInsulins.size,
                runRecords = activeBgs + activeMeals + activeInsulins,
                controlRecords = controlBgs + controlMeals + controlInsulins,
                startBg = startBg,
                startMeals = startMeals,
                startInsulins = startInsulins,
                processEvents = processEvents,
                endBg = endBg ?: startBg, // Fallback to start if only one exists
                controlLogs = controlLogs
            )
        )
    }

    return sessionList
}
