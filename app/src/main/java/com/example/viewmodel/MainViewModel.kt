package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BgRecord
import com.example.data.DiaryRepository
import com.example.data.InsulinRecord
import com.example.data.MealRecord
import com.example.data.prioritizedByGlucometer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = DiaryRepository(db.diaryDao())

    private val _isFootballModeActive = MutableStateFlow(false)
    val isFootballModeActive: StateFlow<Boolean> = _isFootballModeActive.asStateFlow()

    fun toggleFootballMode() {
        _isFootballModeActive.value = !_isFootballModeActive.value
    }

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(10000L) // Update every 10 seconds for real-time reactivity
        }
    }

    // Latest BG with priority for manual Glucometer over xDrip sensor in the same timeframe
    val latestBg: StateFlow<BgRecord?> = repository.allBgRecords.combine(tickerFlow) { records, _ ->
        records.prioritizedByGlucometer().firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // State flow containing the calculated variables for Carbohydrate Coefficient (УК)
    // Counts unique days across meals and insulin administrations
    val ukAnalysisState = repository.allMealRecords.combine(repository.allInsulinRecords) { meals, insulins ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val uniqueDays = mutableSetOf<String>()
        meals.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        insulins.forEach { uniqueDays.add(sdf.format(Date(it.timestamp))) }
        
        val daysCount = uniqueDays.size
        val isReady = daysCount >= 7

        // Calculation of UK per interval: Sum(Novorapid) / Sum(XE)
        fun calculateUk(hourStart: Int, hourEnd: Int): Double {
            val intervalMeals = meals.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hour in hourStart..hourEnd
            }
            val totalXe = intervalMeals.sumOf { it.xe }
            val totalInsulin = intervalMeals.sumOf { it.novorapidDose }
            return if (totalXe > 0.1) {
                totalInsulin / totalXe
            } else {
                1.0 // fallback default
            }
        }

        val ukMorning = if (isReady) calculateUk(0, 12) else 1.5
        val ukDay = if (isReady) calculateUk(13, 15) else 1.2
        val ukEvening = if (isReady) calculateUk(16, 20) else 1.0
        val ukNight = if (isReady) calculateUk(21, 23) else 1.0

        UkAnalysisResult(
            daysCollected = daysCount,
            isReady = isReady,
            ukMorning = ukMorning,
            ukDay = ukDay,
            ukEvening = ukEvening,
            ukNight = ukNight
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UkAnalysisResult(0, false, 1.5, 1.2, 1.0, 1.0)
    )

    fun getCurrentUk(): Double {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val stats = ukAnalysisState.value
        return when (hour) {
            in 0..12 -> stats.ukMorning
            in 13..15 -> stats.ukDay
            in 16..20 -> stats.ukEvening
            else -> stats.ukNight
        }
    }

    // Calculate IOB (Insulin on Board) for Novorapid in the last 4 hours
    val activeInsulin: StateFlow<Double> = combine(
        repository.allInsulinRecords,
        repository.allMealRecords,
        tickerFlow
    ) { insulin, meals, now ->
        val fourHoursInMillis = 4 * 60 * 60 * 1000L
        
        var totalIob = 0.0

        // Standalone Novorapid ulas/injections
        insulin.filter { it.insulinType == "Novorapid" && (now - it.timestamp) < fourHoursInMillis }.forEach { record ->
            val elapsedMinutes = (now - record.timestamp) / 60000.0
            val remainingRatio = (240.0 - elapsedMinutes) / 240.0
            if (remainingRatio > 0) {
                totalIob += record.dose * remainingRatio
            }
        }

        // Meal Novorapid boluses
        meals.filter { (now - it.timestamp) < fourHoursInMillis && it.novorapidDose > 0.0 }.forEach { record ->
            val elapsedMinutes = (now - record.timestamp) / 60000.0
            val remainingRatio = (240.0 - elapsedMinutes) / 240.0
            if (remainingRatio > 0) {
                totalIob += record.novorapidDose * remainingRatio
            }
        }

        totalIob
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val dbItems = repository.allBgRecords.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val footballBgRecords: StateFlow<List<BgRecord>> = repository.footballBgRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val footballMealRecords: StateFlow<List<MealRecord>> = repository.footballMealRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val footballInsulinRecords: StateFlow<List<InsulinRecord>> = repository.footballInsulinRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

data class UkAnalysisResult(
    val daysCollected: Int,
    val isReady: Boolean,
    val ukMorning: Double,
    val ukDay: Double,
    val ukEvening: Double,
    val ukNight: Double
)
