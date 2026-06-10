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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FoodInputScreen(mainViewModel: MainViewModel) {
    val isFootballActive by mainViewModel.isFootballModeActive.collectAsState()
    val ukResult by mainViewModel.ukAnalysisState.collectAsState()

    // 1. Switch between MEAL (Прием пищи) and SNACK (Перекусы/купирование)
    var selectedEventType by remember { mutableStateOf("MEAL") } // "MEAL" or "SNACK"
    
    // Forms fields
    var timeStr by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var glucoseInput by remember { mutableStateOf("") }
    var foodText by remember { mutableStateOf("") }
    var xeInput by remember { mutableStateOf("") }
    var pauseInput by remember { mutableStateOf("") }
    
    // Insulin fields
    var insulinDoseInput by remember { mutableStateOf("") }
    var selectedInsulinType by remember { mutableStateOf("Novorapid") } // "Novorapid" or "Tresiba"

    // Help UK advice calculation
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

            // Switcher: ПРИЕМ ПИЩИ vs ПЕРЕКУС
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
                        "MEAL" to "ПРИЕМ ПИЩИ",
                        "SNACK" to "ПЕРЕКУС (КУПИРОВАНИЕ)"
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

            // Glucose input (manual only!)
            McSlotItem {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    McTextFieldMinecraft(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = "ВРЕМЯ ЗАПИСИ (ЧЧ:ММ)"
                    )

                    McTextFieldMinecraft(
                        value = glucoseInput,
                        onValueChange = { glucoseInput = it },
                        label = "УРОВЕНЬ ГЛЮКОЗЫ (ГЛЮКОМЕТР ММОЛЬ/Л)",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // Food intake details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ДАННЫЕ ПИЩИ",
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
                            label = "ПРИЕМ ПИЩИ (ПРИМЕР: КАША ГРЕЧНЕВАЯ 100Г, ХЛЕБ)"
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                McTextFieldMinecraft(
                                    value = xeInput,
                                    onValueChange = { xeInput = it },
                                    label = "КОЛИЧЕСТВО ХЕ",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
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
            }

            // Insulin input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ДАННЫЕ ИНСУЛИНА",
                    fontSize = 11.sp,
                    color = McGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                McSlotItem {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Choice of insulin type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(2.dp)
                        ) {
                            val insulins = listOf("Novorapid" to "НОВОРАПИД (УЛЬТРА)", "Tresiba" to "ТРЕСИБА (БАЗА)")
                            insulins.forEach { (type, label) ->
                                val isSelected = selectedInsulinType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedInsulinType = type }
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

                        McTextFieldMinecraft(
                            value = insulinDoseInput,
                            onValueChange = { insulinDoseInput = it },
                            label = "ВВЕДЕНО В ТЕЛО (ЕД ЧИСТАЯ)",
                            keyboardType = KeyboardType.Number
                        )

                        // Hint of UK coefficient calculator
                        if (xeInput.toDoubleOrNull() != null && selectedInsulinType == "Novorapid") {
                            val recommendedDose = (xeInput.toDoubleOrNull() ?: 0.0) * currentUkHint
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .padding(1.dp)
                                    .background(Color(0xFF333333))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "💡 ПОДСКАЗКА: РЕКОМЕНДУЕМЫЙ УК СЕЙЧАС ~ ${String.format(Locale.US, "%.1f", currentUkHint)}. ПРИ ВВЕДЕНИИ ${xeInput} ХЕ СОВЕТУЕМ КОЛОТЬ ~ ${String.format(Locale.US, "%.1f", recommendedDose)} ЕД.",
                                    fontSize = 10.sp,
                                    color = McGold,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Cartridge Purge wastage notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(1.dp)
                    .background(Color(0xFF5B3939))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🔧 СБРОС РУЧКИ: ПРИ СОХРАНЕНИИ УКОЛА В БАЗУ ДЛЯ КАРТРИДЖА ТАКЖЕ АВТОМАТИЧЕСКИ СПИСЫВАЕТСЯ +1 ЕД ТЕХНИЧЕСКОГО СБРОСА.",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }

            // Save submit button
            McPixelButton(
                text = "СОХРАНИТЬ СОБЫТИЕ",
                onClick = {
                    val doseVal = insulinDoseInput.toDoubleOrNull() ?: 0.0
                    val glucoVal = glucoseInput.toDoubleOrNull() ?: 0.0
                    val xeVal = xeInput.toDoubleOrNull() ?: 0.0
                    val pauseVal = pauseInput.toIntOrNull() ?: 0
                    val scenarioStr = mainViewModel.getCurrentActiveScenario()

                    // 1. Save glucose manual entry
                    if (glucoVal > 0.0) {
                        val record = BgRecord(
                            timestamp = System.currentTimeMillis(),
                            bgValue = glucoVal,
                            direction = "РУЧНОЙ",
                            isFromXdrip = false,
                            scenario = scenarioStr
                        )
                        mainViewModel.insertBgRecord(record)
                    }

                    // 2. Save meal intake
                    if (foodText.isNotBlank() || xeVal > 0.0) {
                        val record = MealRecord(
                            timestamp = System.currentTimeMillis(),
                            foodText = foodText,
                            xe = xeVal,
                            novorapidDose = if (selectedInsulinType == "Novorapid") doseVal else 0.0,
                            pauseMinutes = pauseVal,
                            bgBefore = glucoVal,
                            eventType = selectedEventType,
                            isBalanced = false, // Auto calculated in stats view
                            scenario = scenarioStr
                        )
                        mainViewModel.insertMealRecord(record)
                    }

                    // 3. Save standalone or related insulin uloc injection
                    if (doseVal > 0.0) {
                        val record = InsulinRecord(
                            timestamp = System.currentTimeMillis(),
                            insulinType = selectedInsulinType,
                            dose = doseVal,
                            primeDose = 1.0, // Every injection causes mechanical purge wastage of 1.0 Units
                            scenario = scenarioStr
                        )
                        mainViewModel.insertInsulinRecord(record)
                    }

                    // Clear fields
                    glucoseInput = ""
                    foodText = ""
                    xeInput = ""
                    pauseInput = ""
                    insulinDoseInput = ""
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
            text = label.uppercase(),
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
