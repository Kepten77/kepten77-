package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DiaryRepository
import com.example.data.MealRecord
import com.example.data.prioritizedByGlucometer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class DashboardViewModel(private val repository: DiaryRepository) : ViewModel() {

    enum class TimePeriod { TODAY, YESTERDAY, WEEK }

    val selectedPeriod = MutableStateFlow(TimePeriod.TODAY)

    private fun getPeriodBounds(period: TimePeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (period) {
            TimePeriod.TODAY -> {
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                start to cal.timeInMillis
            }
            TimePeriod.YESTERDAY -> {
                val end = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.timeInMillis to end
            }
            TimePeriod.WEEK -> {
                // From Monday to Sunday. Let's find Monday
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                if (cal.timeInMillis > System.currentTimeMillis()) {
                    cal.add(Calendar.WEEK_OF_YEAR, -1) // Past Monday if it jumped ahead
                }
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 7) // Next Monday
                start to cal.timeInMillis
            }
        }
    }

    // Helper method to detect if food text contains protein/fat indicators
    private fun isMealBalanced(foodText: String): Boolean {
        val text = foodText.lowercase()
        if (text.isBlank()) return false
        val proteinFatKeywords = listOf(
            "яйцо", "яйца", "мясо", "рыб", "куриц", "творог", "сыр", "колбас", "сосис", 
            "масло", "орех", "сметан", "свинин", "говядин", "кефир", "молок", "сливк", 
            "сало", "ветчин", "паштет", "котлет", "стейк", "куриц", "индейк", "яйц", "омлет",
            "белок", "жир", "протеин", "сырок", "сосиск", "ветчин", "бекон", "салями", "фарш"
        )
        return proteinFatKeywords.any { text.contains(it) }
    }

    val dashboardStats = combine(
        repository.allMealRecords,
        repository.allBgRecords,
        repository.allInsulinRecords,
        selectedPeriod
    ) { meals, bgs, insulins, period ->
        val (start, end) = getPeriodBounds(period)
        
        val periodMeals = meals.filter { it.timestamp in start until end }
        val periodBgs = bgs.filter { it.timestamp in start until end }.prioritizedByGlucometer()
        val periodInsulins = insulins.filter { it.timestamp in start until end }

        // Analyze Plates (only regular MEAL types are counted; SNACK events are ignored here)
        val regularMeals = periodMeals.filter { it.eventType == "MEAL" }
        val balancedMealsCount = regularMeals.count { isMealBalanced(it.foodText) }
        val nakedCarbsCount = regularMeals.count { !isMealBalanced(it.foodText) }

        // Totals (summing EVERYTHING - meals, snacks, boluses)
        val totalXe = periodMeals.sumOf { it.xe }
        
        // Sum of Novorapid = meal-associated Novorapid + standalone Novorapid injections
        val totalNovorapid = periodMeals.sumOf { it.novorapidDose } + 
                periodInsulins.filter { it.insulinType == "Novorapid" }.sumOf { it.dose }
                
        // Sum of Tresiba
        val totalTresiba = periodInsulins.filter { it.insulinType == "Tresiba" }.sumOf { it.dose }
        
        DashboardStats(
            balancedMeals = balancedMealsCount,
            nakedCarbs = nakedCarbsCount,
            totalXe = totalXe,
            totalNovorapid = totalNovorapid,
            totalTresiba = totalTresiba,
            bgRecords = periodBgs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats(0, 0, 0.0, 0.0, 0.0, emptyList())
    )
}

data class DashboardStats(
    val balancedMeals: Int,
    val nakedCarbs: Int,
    val totalXe: Double,
    val totalNovorapid: Double,
    val totalTresiba: Double,
    val bgRecords: List<com.example.data.BgRecord>
)
