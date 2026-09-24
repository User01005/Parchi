package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM saved_bills ORDER BY timestamp DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM saved_bills WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun getBillsByDate(dateKey: String): Flow<List<BillEntity>>

    @Query("SELECT DISTINCT dateKey FROM saved_bills ORDER BY dateKey DESC")
    fun getDistinctDates(): Flow<List<String>>

    @Query("SELECT SUM(totalAmount) FROM saved_bills WHERE dateKey = :dateKey")
    fun getTotalSalesByDate(dateKey: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("DELETE FROM saved_bills WHERE id = :billId")
    suspend fun deleteBillById(billId: Long)
}
