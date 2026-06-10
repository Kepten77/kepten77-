package com.example.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Настройки и Экспорт", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = {
                coroutineScope.launch {
                    val meals = mainViewModel.repository.allMealRecords.first()
                    val bgs = mainViewModel.repository.allBgRecords.first()
                    val insulins = mainViewModel.repository.allInsulinRecords.first()
                    
                    val csvFile = generateCsv(context, meals, bgs, insulins)
                    shareCsv(context, csvFile)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выгрузить неделю (CSV)")
        }

        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Очистить память")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Очистить БД") },
            text = { Text("Вы уверены? Это необратимо удалит все записи из базы данных.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            mainViewModel.repository.clearAllData()
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Отмена") }
            }
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
    
    writer.append("Type,Timestamp,Value1,Value2,Value3,Scenario\n")
    
    bgs.forEach {
        writer.append("BG,${it.timestamp},${it.bgValue},${it.direction},,${it.scenario}\n")
    }
    meals.forEach {
        writer.append("Meal,${it.timestamp},${it.xe},${it.novorapidDose},${it.pauseMinutes},${it.scenario}\n")
    }
    insulins.forEach {
        writer.append("Insulin,${it.timestamp},${it.insulinType},${it.dose},${it.primeDose},${it.scenario}\n")
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
    context.startActivity(Intent.createChooser(intent, "Отправить CSV-файл"))
}
