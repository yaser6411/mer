package com.example.domain.usecase

import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.repository.MercuryRepository
import com.example.domain.FinancialEngine
import com.example.ui.viewmodel.CartItem

/**
 * Use case to process and persist a POS sale transaction with business rule validation.
 */
class ProcessSaleUseCase(
    private val repository: MercuryRepository
) {
    suspend operator fun invoke(
        cartItems: List<CartItem>,
        customerId: Long?,
        customerName: String,
        discount: Double,
        taxRatePercent: Double,
        amountPaid: Double,
        paymentMethod: String,
        notes: String = ""
    ): Result<Long> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot process a sale with an empty cart."))
        }

        val itemTotals = cartItems.map { it.total }
        val subtotal = FinancialEngine.calculateSubtotal(itemTotals)
        val taxableAmount = maxOf(0.0, subtotal - discount)
        val tax = FinancialEngine.calculateTax(taxableAmount, taxRatePercent)
        val total = FinancialEngine.calculateTotal(subtotal, discount, tax)
        val balance = FinancialEngine.calculateBalance(total, amountPaid)
        val paymentStatus = FinancialEngine.determinePaymentStatus(total, amountPaid).name

        val invoiceNumber = repository.generateNextInvoiceNumber()

        val sale = Sale(
            invoiceNumber = invoiceNumber,
            date = System.currentTimeMillis(),
            customerId = customerId,
            customerName = customerName.ifBlank { "Walk-in Customer" },
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            total = total,
            amountPaid = amountPaid,
            balance = balance,
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val saleItems = cartItems.map { item ->
            SaleItem(
                saleId = 0L,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                purchasePrice = item.product.purchasePrice,
                discount = item.discount,
                total = item.total
            )
        }

        return try {
            val saleId = repository.executeSaleTransaction(
                sale = sale,
                items = saleItems,
                paymentAmount = amountPaid,
                paymentMethod = paymentMethod,
                customerId = customerId
            )
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
