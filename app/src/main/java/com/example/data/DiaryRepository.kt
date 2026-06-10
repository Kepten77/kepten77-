package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class DiaryRepository(private val dao: DiaryDao) {

    val allBgRecords: Flow<List<BgRecord>> = dao.getAllBgRecords()
    val allMealRecords: Flow<List<MealRecord>> = dao.getAllMealRecords()
    val allInsulinRecords: Flow<List<InsulinRecord>> = dao.getAllInsulinRecords()
    val latestBgRecord: Flow<BgRecord?> = dao.getLatestBgRecord()

    val footballBgRecords: Flow<List<BgRecord>> = dao.getFootballBgRecords()
    val footballMealRecords: Flow<List<MealRecord>> = dao.getFootballMealRecords()
    val footballInsulinRecords: Flow<List<InsulinRecord>> = dao.getFootballInsulinRecords()

    fun getBgRecordsBetween(start: Long, end: Long) = dao.getBgRecordsBetween(start, end)
    fun getMealRecordsBetween(start: Long, end: Long) = dao.getMealRecordsBetween(start, end)
    fun getInsulinRecordsBetween(start: Long, end: Long) = dao.getInsulinRecordsBetween(start, end)

    suspend fun insertBgRecord(record: BgRecord) = dao.insertBgRecord(record)
    suspend fun insertMealRecord(record: MealRecord) = dao.insertMealRecord(record)
    suspend fun insertInsulinRecord(record: InsulinRecord) = dao.insertInsulinRecord(record)

    suspend fun clearAllData() {
        dao.clearBgRecords()
        dao.clearMealRecords()
        dao.clearInsulinRecords()
    }
}
