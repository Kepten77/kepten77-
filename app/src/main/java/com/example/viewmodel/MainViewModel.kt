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
import kotlinx.coroutines.Dispatchers
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

    private val _footballGameStartTime = MutableStateFlow<Long?>(null)
    val footballGameStartTime: StateFlow<Long?> = _footballGameStartTime.asStateFlow()

    private val _footballControlEndTime = MutableStateFlow<Long?>(null)
    val footballControlEndTime: StateFlow<Long?> = _footballControlEndTime.asStateFlow()

    init {
        // 1. Automatic Weekly Cleanup of records elder than this week's Monday 00:00
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                if (cal.timeInMillis > System.currentTimeMillis()) {
                    cal.add(Calendar.WEEK_OF_YEAR, -1)
                }
                val currentWeekMondayStart = cal.timeInMillis

                db.diaryDao().deleteBgRecordsBefore(currentWeekMondayStart)
                db.diaryDao().deleteMealRecordsBefore(currentWeekMondayStart)
                db.diaryDao().deleteInsulinRecordsBefore(currentWeekMondayStart)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFootballMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val nextState = !_isFootballModeActive.value
            _isFootballModeActive.value = nextState
            if (nextState) {
                _footballGameStartTime.value = System.currentTimeMillis()
                _footballControlEndTime.value = null
            } else {
                _footballGameStartTime.value = null
                _footballControlEndTime.value = System.currentTimeMillis() + 60 * 60 * 1000L // 60 minutes countdown
            }
        }
    }

    fun startFootballWithSnapshot(initialBg: Double, initialXe: Double, initialInsulin: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            _footballGameStartTime.value = now
            _isFootballModeActive.value = true
            _footballControlEndTime.value = null

            if (initialBg > 0.0) {
                repository.insertBgRecord(
                    BgRecord(
                        timestamp = now,
                        bgValue = initialBg,
                        direction = "РУЧНОЙ",
                        isFromXdrip = false,
                        scenario = "football_active"
                    )
                )
            }

            if (initialXe > 0.0) {
                repository.insertMealRecord(
                    MealRecord(
                        timestamp = now,
                        foodText = "Футбол: старт замера",
                        xe = initialXe,
                        novorapidDose = if (initialInsulin > 0.0) initialInsulin else 0.0,
                        pauseMinutes = 0,
                        bgBefore = initialBg,
                        eventType = "MEAL",
                        isBalanced = false,
                        scenario = "football_active"
                    )
                )
            }

            if (initialInsulin > 0.0 && initialXe <= 0.0) {
                repository.insertInsulinRecord(
                    InsulinRecord(
                        timestamp = now,
                        insulinType = "Novorapid",
                        dose = initialInsulin,
                        primeDose = 1.0,
                        scenario = "football_active"
                    )
                )
            }
        }
    }

    fun stopFootballWithControl() {
        viewModelScope.launch(Dispatchers.IO) {
            _isFootballModeActive.value = false
            _footballGameStartTime.value = null
            _footballControlEndTime.value = System.currentTimeMillis() + 60 * 60 * 1000L
        }
    }

    fun logFootballQuickSnack(xe: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            repository.insertMealRecord(
                MealRecord(
                    timestamp = now,
                    foodText = "Спорт-подъел",
                    xe = xe,
                    novorapidDose = 0.0,
                    pauseMinutes = 0,
                    bgBefore = 0.0,
                    eventType = "SNACK",
                    isBalanced = false,
                    scenario = "football_active"
                )
            )
        }
    }

    fun logFootballQuickInsulin(dose: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            repository.insertInsulinRecord(
                InsulinRecord(
                    timestamp = now,
                    insulinType = "Novorapid",
                    dose = dose,
                    primeDose = 1.0, // Obligatory 1 unit mechanical wastage
                    scenario = "football_active"
                )
            )
        }
    }

    fun insertBgRecord(record: BgRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBgRecord(record)
        }
    }

    fun insertMealRecord(record: MealRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMealRecord(record)
        }
    }

    fun insertInsulinRecord(record: InsulinRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertInsulinRecord(record)
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
        }
    }

    fun getCurrentActiveScenario(): String {
        return when {
            _isFootballModeActive.value -> "football_active"
            (_footballControlEndTime.value ?: 0L) > System.currentTimeMillis() -> "football_control"
            else -> "regular"
        }
    }

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(10000L) // Update every 10 seconds for real-time reactivity
        }
    }

    // Latest BG with priority for manual Glucometer over xDrip sensor
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
