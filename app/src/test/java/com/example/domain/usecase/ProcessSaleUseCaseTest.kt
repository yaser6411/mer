package com.example.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MercuryDatabase
import com.example.data.local.entity.Product
import com.example.data.repository.MercuryRepository
import com.example.ui.viewmodel.CartItem
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
class ProcessSaleUseCaseTest {

    private lateinit var db: MercuryDatabase
    private lateinit var repository: MercuryRepository
    private lateinit var useCase: ProcessSaleUseCase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MercuryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MercuryRepository(db)
        useCase = ProcessSaleUseCase(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun processSale_withEmptyCart_returnsFailure() = runTest {
        val result = useCase(
            cartItems = emptyList(),
            customerId = null,
            customerName = "Walk-in",
            discount = 0.0,
            taxRatePercent = 0.0,
            amountPaid = 0.0,
            paymentMethod = "Cash"
        )
        assertTrue(result.isFailure)
        assertEquals("Cannot process a sale with an empty cart.", result.exceptionOrNull()?.message)
    }

    @Test
    fun processSale_withValidCart_deductsInventoryAndPersistsSale() = runTest {
        val testProduct = Product(
            name = "Arabica Coffee",
            sku = "COF-01",
            sellingPrice = 20.0,
            purchasePrice = 10.0,
            stock = 15.0
        )
        val productId = repository.insertProduct(testProduct)
        val insertedProduct = repository.getProductByIdSync(productId)!!

        val cartItems = listOf(
            CartItem(product = insertedProduct, quantity = 3.0, unitPrice = 20.0, discount = 5.0)
        )

        val result = useCase(
            cartItems = cartItems,
            customerId = null,
            customerName = "Sarah Connor",
            discount = 5.0,
            taxRatePercent = 10.0,
            amountPaid = 55.0,
            paymentMethod = "Cash",
            notes = "First customer"
        )

        assertTrue(result.isSuccess)
        val saleId = result.getOrNull()!!
        assertTrue(saleId > 0)

        // Verify stock was reduced from 15.0 to 12.0
        val updatedProduct = repository.getProductByIdSync(productId)!!
        assertEquals(12.0, updatedProduct.stock, 0.01)

        // Verify sale record
        val sale = repository.getSaleByIdSync(saleId)!!
        assertEquals("Sarah Connor", sale.customerName)
        assertEquals(55.0, sale.subtotal, 0.01)
    }
}
