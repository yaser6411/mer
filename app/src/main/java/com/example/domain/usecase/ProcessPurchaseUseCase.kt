package com.example.domain.usecase

import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.repository.MercuryRepository
import com.example.domain.FinancialEngine
import com.example.ui.viewmodel.PurchaseCartItem

/**
 * Use case to record a supplier procurement/purchase order with inventory updates.
 */
class ProcessPurchaseUseCase(
    private val repository: MercuryRepository
) {
    suspend operator fun invoke(
        items: List<PurchaseCartItem>,
        supplierId: Long?,
        supplierName: String,
        amountPaid: Double,
        paymentMethod: String,
        notes: String = ""
    ): Result<Long> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot record purchase with no items."))
        }

        val total = FinancialEngine.calculateSubtotal(items.map { it.total })
        val balance = FinancialEngine.calculateBalance(total, amountPaid)
        val status = FinancialEngine.determinePaymentStatus(total, amountPaid).name
        val refNumber = "PO-${System.currentTimeMillis().toString().takeLast(6)}"

        val purchase = Purchase(
            invoiceNumber = refNumber,
            date = System.currentTimeMillis(),
            supplierId = supplierId,
            supplierName = supplierName.ifBlank { "Direct Supplier" },
            subtotal = total,
            total = total,
            amountPaid = amountPaid,
            balance = balance,
            paymentStatus = status,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val purchaseItems = items.map { item ->
            PurchaseItem(
                purchaseId = 0L,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitCost = item.unitCost,
                total = item.total
            )
        }

        return try {
            val purchaseId = repository.executePurchaseTransaction(
                purchase = purchase,
                items = purchaseItems,
                paymentAmount = amountPaid,
                paymentMethod = paymentMethod,
                supplierId = supplierId
            )
            Result.success(purchaseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
