package com.example.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.dashboard.BlockHeader
import com.example.ui.dashboard.McPixelButton
import com.example.ui.dashboard.McSlotItem
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

@Composable
fun SettingsScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    MinecraftBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "НАСТРОЙКИ СИСТЕМЫ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = McGold,
                fontFamily = FontFamily.Monospace
            )

            // Info Card about the app structure in Pixel Art style
            McSlotItem {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📒 ТАКТИЧЕСКИЙ ДНЕВНИК СД1",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = McGold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Автономная пиксельная система контроля сахарного диабета 1 типа у Трофима. Все собранные данные (сенсорные сахара, ХЕ, дробные замеры и инъекции инсулина) хранятся исключительно локально.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }

            // Sunday Ritual Section (Relocated from the main board!)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BlockHeader("ВОСКРЕСНЫЙ РИТУАЛ")
                
                McSlotItem(borderColor = McDirt, backgroundColor = Color(0xFF2E241E)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Очищать чанки базы данных и выгружать аналитическую карту СД1 рекомендуется каждое воскресенье для сохранения высокой скорости отклика.",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            McPixelButton(
                                text = "ВЫГРУЗИТЬ КАРТУ",
                                onClick = {
                                    coroutineScope.launch {
                                        val meals = mainViewModel.repository.allMealRecords.first()
                                        val bgs = mainViewModel.repository.allBgRecords.first()
                                        val insulins = mainViewModel.repository.allInsulinRecords.first()
                                        
                                        val csvFile = generateCsv(context, meals, bgs, insulins)
                                        shareCsv(context, csvFile)
                                    }
                                },
                                backgroundColor = McGrass,
                                modifier = Modifier.weight(1f)
                            )
                            McPixelButton(
                                text = "ОЧИСТИТЬ ЧАНК",
                                onClick = { showDialog = true },
                                backgroundColor = McRedstone,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer versioning label
            Text(
                text = "ВЕРСИЯ: BETA 2.0-MC\nСЕРВЕНАЯ АВТОНОМИЯ: СБОРКА РАЗРЕШЕНА",
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "ОЧИСТИТЬ ЧАНК БД?",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = McRedstone
                )
            },
            text = {
                Text(
                    text = "Внимание! Локальная база будет полностью занулена. Выгрузи карту перед этим действием!",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = McWhite
                )
            },
            confirmButton = {
                McPixelButton(
                    text = "ДА, ОЧИСТИТЬ",
                    onClick = {
                        mainViewModel.clearAllData()
                        showDialog = false
                    },
                    backgroundColor = McRedstone
                )
            },
            dismissButton = {
                McPixelButton(
                    text = "ОТМЕНА",
                    onClick = { showDialog = false },
                    backgroundColor = McStone
                )
            },
            shape = RectangleShape,
            containerColor = Color(0xFF333333)
        )
    }
}

private fun generateCsv(
    context: Context,
    meals: List<com.example.data.MealRecord>,
    bgs: List<com.example.data.BgRecord>,
    insulins: List<com.example.data.InsulinRecord>
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

private fun shareCsv(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "ОТПРАВИТЬ СД1 КАРТУ"))
}
