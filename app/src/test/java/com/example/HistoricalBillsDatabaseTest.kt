package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.BillDao
import com.example.data.BillRepository
import com.example.model.BillItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HistoricalBillsDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var billDao: BillDao
    private lateinit var repository: BillRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        billDao = db.billDao()
        repository = BillRepository(billDao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun saveBillAndRetrieveByDate() = runBlocking {
        val items = listOf(
            BillItem(serialNumber = 1, itemName = "Atta", weightOrQuantity = "10 kg", price = 356.0, isVerified = true),
            BillItem(serialNumber = 2, itemName = "Sugar", weightOrQuantity = "5 kg", price = 280.0, isVerified = true)
        )

        val id = repository.saveBill(
            billNumber = "1001",
            formattedDateTime = "21 Sep 2026, 10:00 AM",
            customerName = "Ramesh",
            customerPhone = "9876543210",
            customerHouseNo = "House #12",
            items = items,
            totalAmount = 636.0,
            isVerified = true
        )

        assertTrue(id > 0)

        val allBills = repository.allBills.first()
        assertEquals(1, allBills.size)

        val savedBill = allBills[0]
        assertEquals("1001", savedBill.billNumber)
        assertEquals("Ramesh", savedBill.customerName)
        assertEquals(636.0, savedBill.totalAmount, 0.01)
        assertNotNull(savedBill.dateKey)
        assertTrue(savedBill.dateKey.isNotBlank())

        // Verify parsing JSON items back
        val parsedItems = repository.parseItemsJson(savedBill.itemsJson)
        assertEquals(2, parsedItems.size)
        assertEquals("Atta", parsedItems[0].itemName)
        assertEquals(356.0, parsedItems[0].price)
    }
}
