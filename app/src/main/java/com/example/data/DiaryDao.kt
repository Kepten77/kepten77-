package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM bg_records ORDER BY timestamp DESC")
    fun getAllBgRecords(): Flow<List<BgRecord>>

    @Query("SELECT * FROM bg_records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getBgRecordsBetween(startTime: Long, endTime: Long): Flow<List<BgRecord>>

    @Query("SELECT * FROM bg_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBgRecord(): Flow<BgRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBgRecord(record: BgRecord)

    @Query("SELECT * FROM meal_records ORDER BY timestamp DESC")
    fun getAllMealRecords(): Flow<List<MealRecord>>

    @Query("SELECT * FROM meal_records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getMealRecordsBetween(startTime: Long, endTime: Long): Flow<List<MealRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealRecord(record: MealRecord)

    @Query("SELECT * FROM insulin_records ORDER BY timestamp DESC")
    fun getAllInsulinRecords(): Flow<List<InsulinRecord>>

    @Query("SELECT * FROM insulin_records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getInsulinRecordsBetween(startTime: Long, endTime: Long): Flow<List<InsulinRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsulinRecord(record: InsulinRecord)

    @Query("DELETE FROM bg_records")
    suspend fun clearBgRecords()

    @Query("DELETE FROM bg_records WHERE timestamp < :timestamp")
    suspend fun deleteBgRecordsBefore(timestamp: Long)

    @Query("DELETE FROM meal_records")
    suspend fun clearMealRecords()

    @Query("DELETE FROM meal_records WHERE timestamp < :timestamp")
    suspend fun deleteMealRecordsBefore(timestamp: Long)

    @Query("DELETE FROM insulin_records")
    suspend fun clearInsulinRecords()

    @Query("DELETE FROM insulin_records WHERE timestamp < :timestamp")
    suspend fun deleteInsulinRecordsBefore(timestamp: Long)

    @Query("SELECT * FROM bg_records WHERE scenario IN ('football_active', 'football_control') ORDER BY timestamp DESC")
    fun getFootballBgRecords(): Flow<List<BgRecord>>

    @Query("SELECT * FROM meal_records WHERE scenario IN ('football_active', 'football_control') ORDER BY timestamp DESC")
    fun getFootballMealRecords(): Flow<List<MealRecord>>

    @Query("SELECT * FROM insulin_records WHERE scenario IN ('football_active', 'football_control') ORDER BY timestamp DESC")
    fun getFootballInsulinRecords(): Flow<List<InsulinRecord>>
}
