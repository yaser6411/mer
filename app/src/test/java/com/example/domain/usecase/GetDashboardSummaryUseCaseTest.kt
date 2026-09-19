package com.example.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MercuryDatabase
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.repository.MercuryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GetDashboardSummaryUseCaseTest {

    private lateinit var db: MercuryDatabase
    private lateinit var repository: MercuryRepository
    private lateinit var useCase: GetDashboardSummaryUseCase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MercuryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MercuryRepository(db)
        useCase = GetDashboardSummaryUseCase(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getDashboardSummary_aggregatesFinancialMetricsCorrectly() = runTest {
        val product = Product(
            name = "Test Item",
            sku = "TST-01",
            sellingPrice = 100.0,
            purchasePrice = 50.0,
            stock = 10.0,
            minStock = 15.0 // low stock
        )
        val productId = repository.insertProduct(product)

        // Insert expense
        repository.insertExpense(
            Expense(
                category = "Utilities",
                amount = 150.0,
                date = System.currentTimeMillis(),
                notes = "Electricity"
            )
        )

        // Record a sale
        val sale = Sale(
            invoiceNumber = "INV-001",
            date = System.currentTimeMillis(),
            customerName = "Customer A",
            subtotal = 200.0,
            total = 200.0,
            amountPaid = 200.0
        )
        val saleItem = SaleItem(
            saleId = 0L,
            productId = productId,
            productName = product.name,
            quantity = 2.0,
            unitPrice = 100.0,
            total = 200.0
        )
        repository.executeSaleTransaction(sale, listOf(saleItem), 200.0, "Cash", null)

        val summary = useCase().first()

        assertEquals(200.0, summary.todaySales, 0.01)
        assertEquals(150.0, summary.todayExpenses, 0.01)
        assertEquals(1, summary.lowStockCount)
        assertEquals(400.0, summary.inventoryValue, 0.01) // (10 - 2) * 50 = 400
    }
}
