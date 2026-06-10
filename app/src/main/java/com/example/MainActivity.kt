package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.MainViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var initError: String? = null
        var mainVmField: MainViewModel? = null
        var dashboardVmField: DashboardViewModel? = null

        try {
            // Retrieve viewmodels inside try-catch to absorb any Room or Android crash surfaces at start
            val mViewModel: MainViewModel by viewModels()
            val dViewModel: DashboardViewModel by viewModels {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return DashboardViewModel(mViewModel.repository) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                }
            }
            mainVmField = mViewModel
            dashboardVmField = dViewModel
            
            // Force lazy trigger to capture startup exceptions immediately
            mViewModel.repository
            dViewModel.selectedPeriod
        } catch (e: Exception) {
            e.printStackTrace()
            initError = e.localizedMessage ?: e.toString()
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (initError != null) {
                        EmergencyErrorView(initError)
                    } else {
                        MainApp(mainVmField!!, dashboardVmField!!)
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyErrorView(errorText: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8C8C8C)) // Emergency gray block background
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(2.dp)
                .background(Color(0xFF333333))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "КРИТИЧЕСКАЯ ОШИБКА ИНИЦИАЛИЗАЦИИ",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = errorText,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            
            Text(
                text = "АНАЛИТИКА / СУБД СБОЙ. ПОЖАЛУЙСТА, ОБНУЛИТЕ ИЛИ ПЕРЕУСТАНОВИТЕ ПРИЛОЖЕНИЕ.",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
