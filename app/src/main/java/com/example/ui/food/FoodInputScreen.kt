package com.example.ui.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodInputScreen(mainViewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val isFootball by mainViewModel.isFootballModeActive.collectAsState()
    val latestBgState by mainViewModel.latestBg.collectAsState()
    val ukResult by mainViewModel.ukAnalysisState.collectAsState()

    // 1. Event Type Switcher
    // "MEAL" (Прием пищи), "SNACK" (Перекус), "CORRECTION" (Подколка без еды), "BASAL" (Тресиба)
    var selectedEventType by remember { mutableStateOf("MEAL") }

    // Forms fields
    var timeStr by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var bgInput by remember { mutableStateOf("") }
    var glucoInput by remember { mutableStateOf("") }
    
    var selectedScenario by remember { mutableStateOf("regular") }
    
    // Auto default/adjust based on global football state
    LaunchedEffect(isFootball) {
        selectedScenario = if (isFootball) "football_active" else "regular"
    }

    // Auto populate xDrip value but allow typing/overriding completely
    LaunchedEffect(latestBgState) {
        if (latestBgState != null && bgInput.isEmpty()) {
            bgInput = String.format(Locale.US, "%.1f", latestBgState!!.bgValue)
        }
    }

    // Food fields
    var foodText by remember { mutableStateOf("") }
    var xeInput by remember { mutableStateOf("") }
    var pauseInput by remember { mutableStateOf("") }

    // Insulin doses (Novorapid or Tresiba depending on type)
    var insulinDoseInput by remember { mutableStateOf("") }

    // Hints information Box showing the current estimated carbohydrate ratio
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val currentUkHint = when (hour) {
        in 0..12 -> ukResult.ukMorning
        in 13..15 -> ukResult.ukDay
        in 16..20 -> ukResult.ukEvening
        else -> ukResult.ukNight
    }

    MinecraftBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "РЕГИСТРАЦИЯ СОБЫТИЯ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = McGold,
                fontFamily = FontFamily.Monospace
            )

            // Event selector in full pixel art segmented box
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ТИП СОБЫТИЯ:",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val types = listOf(
                        "MEAL" to "ЕДА",
                        "SNACK" to "ПЕРЕКУС",
                        "STANDALONE" to "УКОЛ"
                    )
                    types.forEach { (type, label) ->
                        val isSelected = selectedEventType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedEventType = type }
                                .background(if (isSelected) McDirt else Color.Transparent)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "[$label]",
                                color = if (isSelected) McGold else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Scenario selection in full pixel art segmented box
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "СЦЕНАРИЙ ЗАПИСИ (СПОРТ / ОБЫЧНЫЙ):",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val scenarios = listOf(
                        "regular" to "ОБЫЧНЫЙ",
                        "football_active" to "⚔️ ИГРА / СТАРТ",
                        "football_control" to "🛡️ КОНТРОЛЬ"
                    )
                    scenarios.forEach { (scen, label) ->
                        val isSelected = selectedScenario == scen
                        val bgCol = if (isSelected) {
                            if (scen == "regular") McDirt else if (scen == "football_active") McRedstone else McGrass
                        } else Color.Transparent
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedScenario = scen }
                                .background(bgCol)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) McWhite else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // General Info Block
            McSlotItem {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Time field
                    McTextFieldMinecraft(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = "ВРЕМЯ ЗАПИСИ (ЧЧ:ММ)"
                    )

                    // Sugar level xDrip/Glucometer (fully active and editable!)
                    McTextFieldMinecraft(
                        value = bgInput,
                        onValueChange = { bgInput = it },
                        label = "СЕНСОР XDRIP (АВТОМАТИЧЕСКИ, ММОЛЬ/Л)",
                        keyboardType = KeyboardType.Number
                    )

                    // Glucometer manual stripe entry (real blood drop)
                    McTextFieldMinecraft(
                        value = glucoInput,
                        onValueChange = { glucoInput = it },
                        label = "ГЛЮКОМЕТР (ПО КАПЛЕ КРОВИ, ММОЛЬ/Л) ⭐",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // Food Block: Activated if not choosing Standalone Insulin
            if (selectedEventType == "MEAL" || selectedEventType == "SNACK") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "СОСТАВ ПРИЕМА ПИЩИ",
                        fontSize = 11.sp,
                        color = McGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    McSlotItem {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            McTextFieldMinecraft(
                                value = foodText,
                                onValueChange = { foodText = it },
                                label = "ПРИЕМ ПИЩИ (НАПРИМЕР: ГРЕЧКА 100Г, ХЛЕБ)"
                            )

                            McTextFieldMinecraft(
                                value = xeInput,
                                onValueChange = { xeInput = it },
                                label = "КОЛИЧЕСТВО ХЕ",
                                keyboardType = KeyboardType.Number
                            )

                            McTextFieldMinecraft(
                                value = pauseInput,
                                onValueChange = { pauseInput = it },
                                label = "ПАУЗА ПЕРЕД ЕДОЙ (МИН)",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }

            // Standalone Insulin Config
            var selectedInsulinTypeForStandalone by remember { mutableStateOf("Novorapid") }
            if (selectedEventType == "STANDALONE") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ИНЪЕКЦИЯ САНС ЕДА",
                        fontSize = 11.sp,
                        color = McGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    McSlotItem {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(2.dp)
                        ) {
                            val insulins = listOf("Novorapid" to "НОВОРАПИД", "Tresiba" to "ТРЕСИБА")
                            insulins.forEach { (type, label) ->
                                val isSelected = selectedInsulinTypeForStandalone == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedInsulinTypeForStandalone = type }
                                        .background(if (isSelected) McGrass else Color.Transparent)
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) McWhite else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Insulin Dosage Block (fully active, manually input!)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ДОЗИРОВКА ИНСУЛИНА",
                    fontSize = 11.sp,
                    color = McGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                McSlotItem {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        McTextFieldMinecraft(
                            value = insulinDoseInput,
                            onValueChange = { insulinDoseInput = it },
                            label = "ВВЕДЕНО В ТЕЛО (ЕД ЧИСТАЯ)",
                            keyboardType = KeyboardType.Number
                        )

                        // Minecraft style hint box about current estimated UK
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(1.dp)
                                .background(Color(0xFF333333))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "💡 ПОДСКАЗКА: РЕКОМЕНДУЕМЫЙ УК СЕЙЧАС ~ ${String.format(Locale.US, "%.1f", currentUkHint)}. ПРИ ВВЕДЕНИИ ${xeInput.toDoubleOrNull() ?: 0.0} ХЕ СОВЕТУЕМ КОЛОТЬ ~ ${String.format(Locale.US, "%.1f", (xeInput.toDoubleOrNull() ?: 0.0) * currentUkHint)} ЕД.",
                                fontSize = 10.sp,
                                color = McGold,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Cartridge wastage warning (Minecraft style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(1.dp)
                    .background(Color(0xFF5B3939))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🔧 СБРОС РУЧКИ: ПРИ ЖЕСТКОЙ КНОПКЕ СОХРАНЕНИЯ В БАЗУ ТАКЖЕ ДОБАВЛЯЕТСЯ СПИСАНИЕ +1 ЕД ТЕХНИЧЕСКОГО СБРОСА ДЛЯ РАСХОДА КАРТРИДЖА.",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }

            // Main Save Button
            McPixelButton(
                text = "СОХРАНИТЬ СОБЫТИЕ",
                onClick = {
                    val doseVal = insulinDoseInput.toDoubleOrNull() ?: 0.0
                    val bgVal = bgInput.toDoubleOrNull() ?: 0.0
                    val glucoVal = glucoInput.toDoubleOrNull() ?: 0.0
                    val isStandalone = selectedEventType == "STANDALONE"

                    coroutineScope.launch {
                        // 1. If physical glucometer manual reading was submitted, save separately in Room!
                        if (glucoVal > 0.0) {
                            val glucoRecord = BgRecord(
                                timestamp = System.currentTimeMillis(),
                                bgValue = glucoVal,
                                direction = "РУЧНОЙ",
                                isFromXdrip = false,
                                scenario = selectedScenario
                            )
                            mainViewModel.repository.insertBgRecord(glucoRecord)
                        }

                        // Determine effective pre-prandial blood glucose (prefer Glucometer over xDrip sensor)
                        val effectiveBgBefore = if (glucoVal > 0.0) glucoVal else bgVal

                        // 2. If physical food was logged
                        if (!isStandalone) {
                            val record = MealRecord(
                                timestamp = System.currentTimeMillis(),
                                foodText = foodText,
                                xe = xeInput.toDoubleOrNull() ?: 0.0,
                                novorapidDose = doseVal,
                                pauseMinutes = pauseInput.toIntOrNull() ?: 0,
                                bgBefore = effectiveBgBefore,
                                eventType = selectedEventType, // "MEAL" or "SNACK"
                                isBalanced = false, // analyzed inside ViewModel automatically via keywords
                                scenario = selectedScenario
                            )
                            mainViewModel.repository.insertMealRecord(record)
                        }

                        // 3. If insulin logged
                        if (doseVal > 0.0 || isStandalone) {
                            val insType = if (isStandalone) selectedInsulinTypeForStandalone else "Novorapid"
                            
                            val record = InsulinRecord(
                                timestamp = System.currentTimeMillis(),
                                insulinType = insType,
                                dose = doseVal,
                                primeDose = 1.0, // Obligatory 1 unit mechanical wastage cartridge prime
                                scenario = selectedScenario
                            )
                            mainViewModel.repository.insertInsulinRecord(record)
                        }

                        // Clear all forms inputs
                        foodText = ""
                        xeInput = ""
                        pauseInput = ""
                        insulinDoseInput = ""
                        bgInput = ""
                        glucoInput = ""
                    }
                },
                backgroundColor = McGrass,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McTextFieldMinecraft(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Black, RectangleShape),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2C2C2C),
                unfocusedContainerColor = Color(0xFF2C2C2C),
                disabledContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = McGold,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RectangleShape,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            singleLine = true
        )
    }
}
