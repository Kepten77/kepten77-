package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.service.XDripReceiver
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.MainViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DashboardViewModel(mainViewModel.repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    
    private lateinit var xDripReceiver: XDripReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        xDripReceiver = XDripReceiver(mainViewModel.repository) {
            if (mainViewModel.isFootballModeActive.value) "football" else "regular"
        }
        XDripReceiver.register(this, xDripReceiver)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainApp(mainViewModel, dashboardViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        XDripReceiver.unregister(this, xDripReceiver)
    }
}
