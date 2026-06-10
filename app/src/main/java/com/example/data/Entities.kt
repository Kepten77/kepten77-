package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bg_records")
data class BgRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val bgValue: Double,
    val direction: String,
    val isFromXdrip: Boolean,
    val scenario: String
)

// Extension function to prioritize Glucometer over xDrip values in the same 15-minute intervals
fun List<BgRecord>.prioritizedByGlucometer(): List<BgRecord> {
    // Group records by 15-minute time frames: timestamp / (15 * 60 * 1000)
    val grouped = this.groupBy { it.timestamp / (15 * 60 * 1000L) }
    val result = mutableListOf<BgRecord>()
    for ((_, groupItems) in grouped) {
        val hasGluco = groupItems.any { !it.isFromXdrip }
        if (hasGluco) {
            // Keep only the Glucometer records in this time slot
            result.addAll(groupItems.filter { !it.isFromXdrip })
        } else {
            // Keep xDrip records
            result.addAll(groupItems)
        }
    }
    return result.sortedByDescending { it.timestamp }
}

@Entity(tableName = "meal_records")
data class MealRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val foodText: String, // "Прием пищи" (например: "Каша гречневая 100г, хлеб черный 50г")
    val xe: Double,
    val novorapidDose: Double,
    val pauseMinutes: Int,
    val bgBefore: Double,
    val eventType: String, // "MEAL" or "SNACK"
    val isBalanced: Boolean,
    val scenario: String
)

@Entity(tableName = "insulin_records")
data class InsulinRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val insulinType: String, // "Novorapid" or "Tresiba"
    val dose: Double,
    val primeDose: Double, // The technical prime portion, usually 1.0 
    val scenario: String
)
