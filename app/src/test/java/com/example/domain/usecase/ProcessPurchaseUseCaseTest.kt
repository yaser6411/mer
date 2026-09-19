package com.example.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MercuryDatabase
import com.example.data.local.entity.Product
import com.example.data.repository.MercuryRepository
import com.example.ui.viewmodel.PurchaseCartItem
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProcessPurchaseUseCaseTest {

    private lateinit var db: MercuryDatabase
    private lateinit var repository: MercuryRepository
    private lateinit var useCase: ProcessPurchaseUseCase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MercuryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MercuryRepository(db)
        useCase = ProcessPurchaseUseCase(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun processPurchase_withEmptyItems_returnsFailure() = runTest {
        val result = useCase(
            items = emptyList(),
            supplierId = null,
            supplierName = "Supplier A",
            amountPaid = 0.0,
            paymentMethod = "Cash"
        )
        assertTrue(result.isFailure)
        assertEquals("Cannot record purchase with no items.", result.exceptionOrNull()?.message)
    }

    @Test
    fun processPurchase_withValidItems_increasesStockAndPersistsPurchase() = runTest {
        val product = Product(
            name = "Paper Cups",
            sku = "CUP-01",
            sellingPrice = 5.0,
            purchasePrice = 2.0,
            stock = 50.0
        )
        val productId = repository.insertProduct(product)
        val insertedProduct = repository.getProductByIdSync(productId)!!

        val items = listOf(
            PurchaseCartItem(product = insertedProduct, quantity = 25.0, unitCost = 2.0)
        )

        val result = useCase(
            items = items,
            supplierId = null,
            supplierName = "Eco Supplies Ltd",
            amountPaid = 50.0,
            paymentMethod = "Cash",
            notes = "Stock replenishment"
        )

        assertTrue(result.isSuccess)
        val purchaseId = result.getOrNull()!!
        assertTrue(purchaseId > 0)

        // Verify stock increased from 50.0 to 75.0
        val updatedProduct = repository.getProductByIdSync(productId)!!
        assertEquals(75.0, updatedProduct.stock, 0.01)

        val purchase = repository.getPurchaseByIdSync(purchaseId)!!
        assertEquals("Eco Supplies Ltd", purchase.supplierName)
        assertEquals(50.0, purchase.total, 0.01)
        assertEquals(50.0, purchase.amountPaid, 0.01)
        assertEquals("PAID", purchase.paymentStatus)
    }
}
