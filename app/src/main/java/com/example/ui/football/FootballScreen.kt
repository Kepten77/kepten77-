package com.example.ui.football

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BgRecord
import com.example.data.InsulinRecord
import com.example.data.MealRecord
import com.example.ui.dashboard.McPixelButton
import com.example.ui.dashboard.McSlotItem
import com.example.ui.dashboard.getBgColorCode
import com.example.ui.food.McTextFieldMinecraft
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FootballScreen(mainViewModel: MainViewModel) {
    val isFootballActive by mainViewModel.isFootballModeActive.collectAsState()
    val gameStartTime by mainViewModel.footballGameStartTime.collectAsState()
    val controlEndTime by mainViewModel.footballControlEndTime.collectAsState()

    val bgRecords by mainViewModel.footballBgRecords.collectAsState()
    val mealRecords by mainViewModel.footballMealRecords.collectAsState()
    val insulinRecords by mainViewModel.footballInsulinRecords.collectAsState()

    // Aggregate sessions for historical logs
    val sessions = remember(bgRecords, mealRecords, insulinRecords) {
        aggregateFootballSessions(bgRecords, mealRecords, insulinRecords)
    }

    // Active Timer update ticker (runs only when football is active)
    var matchDurationMinutes by remember { mutableStateOf(0L) }
    LaunchedEffect(isFootballActive, gameStartTime) {
        while (isFootballActive && gameStartTime != null) {
            val diffMs = System.currentTimeMillis() - gameStartTime!!
            matchDurationMinutes = diffMs / 60000L
            delay(10000L) // Update every 10 seconds
        }
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

            // DUAL STATES BLOCK
            if (isFootballActive) {
                // ACTIVE IN-BATTLE CONTROLS
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, McRedstone)
                            .background(Color(0xFF3A1C1C))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚔️ В БИТВЕ ⚔️",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = McGold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Text(
                            text = "ДЛИТЕЛЬНОСТЬ МАТЧА: $matchDurationMinutes МИНУТ",
                            fontSize = 12.sp,
                            color = McWhite,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large One-Tap Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            McPixelButton(
                                text = "🥯 ПОДЪЕЛ\n+1 ХЕ",
                                onClick = {
                                    mainViewModel.logFootballQuickSnack(1.0)
                                },
                                backgroundColor = McGrass,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                            )

                            McPixelButton(
                                text = "💉 ПОДКОЛОЛ\n+1 ЕД",
                                onClick = {
                                    mainViewModel.logFootballQuickInsulin(1.0)
                                },
                                backgroundColor = McGold,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        McPixelButton(
                            text = "⏹️ СТОП ТРЕНИРОВКА",
                            onClick = {
                                mainViewModel.stopFootballWithControl()
                            },
                            backgroundColor = Color(0xFF555555),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // START TRAINING PANEL (WITH SNAPSHOT FORM)
                item {
                    var startBgField by remember { mutableStateOf("") }
                    var startXeField by remember { mutableStateOf("") }
                    var startInsulinField by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, McGold)
                            .background(Color(0xFF2E2E2E))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "НОВАЯ ТРЕНИРОВКА (СТАРТ)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = McGold,
                            fontFamily = FontFamily.Monospace
                        )

                        // If post-match control 60m countdown is active, show countdown notice!
                        val isControlActive = (controlEndTime ?: 0L) > System.currentTimeMillis()
                        if (isControlActive) {
                            val timeRemainingSec = (controlEndTime!! - System.currentTimeMillis()) / 1000L
                            val minRemaining = timeRemainingSec / 60
                            val secRemaining = timeRemainingSec % 60
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .padding(1.dp)
                                    .background(Color(0xFF1B3D2B))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🛡️ ПОСТ-МАТЧЕВЫЙ КОНТРОЛЬ АКТИВЕН: ОСТАЛОСЬ ${minRemaining}:${String.format(Locale.US, "%02d", secRemaining)} МИН. ВСЕ ЗАПРОСЫ ПОЛУЧАЮТ МЕТКУ CONTROL.",
                                    fontSize = 10.sp,
                                    color = McWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        McTextFieldMinecraft(
                            value = startBgField,
                            onValueChange = { startBgField = it },
                            label = "СТАРТОВЫЙ САХАР (ММОЛЬ/Л)",
                            keyboardType = KeyboardType.Number
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                McTextFieldMinecraft(
                                    value = startXeField,
                                    onValueChange = { startXeField = it },
                                    label = "ХЕ НА ХОДУ",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                McTextFieldMinecraft(
                                    value = startInsulinField,
                                    onValueChange = { startInsulinField = it },
                                    label = "НОВОРАПИД КОК",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }

                        McPixelButton(
                            text = "⚔️ НАЧАТЬ ТРЕНИРОВКУ",
                            onClick = {
                                val bgVal = startBgField.toDoubleOrNull() ?: 0.0
                                val xeVal = startXeField.toDoubleOrNull() ?: 0.0
                                val insVal = startInsulinField.toDoubleOrNull() ?: 0.0
                                mainViewModel.startFootballWithSnapshot(bgVal, xeVal, insVal)
                                // reset local fields
                                startBgField = ""
                                startXeField = ""
                                startInsulinField = ""
                            },
                            backgroundColor = McRedstone,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // HISTORICAL SESSIONS LIST
            item {
                Text(
                    text = "ИСТОРИЯ МАТЧЕЙ И СЕССИЙ ЗА НЕДЕЛЮ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = McGold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

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
                                text = "НЕТ СПОРТИВНЫХ ЗАПИСЕЙ ЗА НЕДЕЛЮ",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
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
        // Collapsible trigger header
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = McGold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "СОБЫТИЙ: ${session.totalRecordsCount} (АКТ: ${session.runRecords.size} / ПОСТ: ${session.controlRecords.size})",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = if (expanded) "[- СВЕРНУТЬ]" else "[+ РАЗВЕРНУТЬ]",
                fontSize = 11.sp,
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // STARTING STATS
                Column {
                    Text(
                        text = "⛳ SNAPSHOT СТАРТА:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = McGrass,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(1.dp)
                            .background(Color(0xFF2A2A2A))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val startBg = session.startBg
                            val startBgStr = if (startBg != null) {
                                "${String.format(Locale.US, "%.1f", startBg.bgValue)} ммоль/л"
                            } else {
                                "НЕТ"
                            }
                            Text(
                                text = "• САХАР НА СТАРТЕ: $startBgStr",
                                fontSize = 11.sp,
                                color = if (startBg != null) getBgColorCode(startBg.bgValue) else Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )

                            val startMealsStr = if (session.startMeals.isNotEmpty()) {
                                session.startMeals.joinToString(", ") { "${it.xe} ХЕ" }
                            } else {
                                "0 ХЕ"
                            }
                            Text(
                                text = "• УГЛЕВОДЫ (ХЕ): $startMealsStr",
                                fontSize = 11.sp,
                                color = McWhite,
                                fontFamily = FontFamily.Monospace
                            )

                            val startInsulinsStr = if (session.startInsulins.isNotEmpty()) {
                                session.startInsulins.joinToString(", ") { "${it.dose} ЕД Novo" }
                            } else {
                                "0 ЕД"
                            }
                            Text(
                                text = "• ИНСУЛИН СТАРТА: $startInsulinsStr",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // PROCESS LOG EVENTS
                Column {
                    Text(
                        text = "⚔️ В ПРОЦЕССЕ ТРЕНИРОВКИ:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = McRedstone,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val midEvents = session.processEvents
                    if (midEvents.isEmpty()) {
                        Text(
                            text = "НЕТ ПОДЪЕЛОВ / ПОДКОЛОК В ИГРЕ",
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

                // POST-GAME CONTROL RESULTS
                Column {
                    Text(
                        text = "🛡️ ПОСТ-МАТЧЕВЫЙ КОНТРОЛЬ (+60 МИН):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF55FFFF),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    val controlLogs = session.controlLogs
                    if (controlLogs.isEmpty()) {
                        Text(
                            text = "НЕТ КОНТРОЛЬНЫХ ЗАМЕРОВ",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            controlLogs.forEach { log ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                        .padding(1.dp)
                                        .background(Color(0xFF1E282E))
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

        // Earliest BG record as START snapshot
        val startBg = activeBgs.firstOrNull()
        
        // Start insulin and food loaded together at the start (within first 15 minutes)
        val startMeals = activeMeals.filter { Math.abs(it.timestamp - startThreshold) <= 15 * 60 * 1000L }
        val startInsulins = activeInsulins.filter { Math.abs(it.timestamp - startThreshold) <= 15 * 60 * 1000L }

        // Process updates
        val lastBg = activeBgs.lastOrNull()
        val endBg = if (activeBgs.size > 1) lastBg else null

        val processEvents = mutableListOf<String>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Add BG updates in process
        activeBgs.forEach { bg ->
            if (bg != startBg && bg != endBg) {
                processEvents.add("[⏱️ ${timeFormat.format(Date(bg.timestamp))}] ЗАМЕР: ${String.format(Locale.US, "%.1f", bg.bgValue)} ммоль/л")
            }
        }
        // Add meals in process
        activeMeals.forEach { meal ->
            if (!startMeals.contains(meal)) {
                processEvents.add("[⏱️ ${timeFormat.format(Date(meal.timestamp))}] ПОДЪЕЛ: ${meal.xe} ХЕ (${meal.foodText})")
            }
        }
        // Add insulin in process
        activeInsulins.forEach { insulin ->
            if (!startInsulins.contains(insulin)) {
                processEvents.add("[⏱️ ${timeFormat.format(Date(insulin.timestamp))}] ПОДКОЛКА: ${insulin.dose} ЕД (${insulin.insulinType})")
            }
        }

        // Control Logs after Game
        val controlLogs = mutableListOf<String>()
        controlBgs.forEach { bg ->
            controlLogs.add("[⏱️ ${timeFormat.format(Date(bg.timestamp))}] ЗАМЕР: ${String.format(Locale.US, "%.1f", bg.bgValue)} ммоль/л")
        }
        controlMeals.forEach { meal ->
            controlLogs.add("[⏱️ ${timeFormat.format(Date(meal.timestamp))}] ПОДЪЕЛ: ${meal.xe} ХЕ (${meal.foodText})")
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
                endBg = endBg ?: startBg,
                controlLogs = controlLogs
            )
        )
    }

    return sessionList
}
