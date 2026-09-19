package com.example.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FinancialEngine {

    enum class PaymentStatus {
        PAID,
        PARTIALLY_PAID,
        UNPAID
    }

    data class SaleTotals(
        val subtotal: Double,
        val discount: Double,
        val tax: Double,
        val total: Double,
        val amountPaid: Double,
        val balance: Double,
        val paymentStatus: PaymentStatus,
        val cogs: Double,
        val grossProfit: Double
    )

    fun calculateItemTotal(quantity: Double, unitPrice: Double, discount: Double = 0.0): Double {
        val qty = java.math.BigDecimal.valueOf(quantity)
        val price = java.math.BigDecimal.valueOf(unitPrice)
        val disc = java.math.BigDecimal.valueOf(discount)
        val raw = qty.multiply(price).subtract(disc)
        return maxOf(0.0, raw.setScale(2, java.math.RoundingMode.HALF_UP).toDouble())
    }

    fun calculateSubtotal(itemTotals: List<Double>): Double {
        var sum = java.math.BigDecimal.ZERO
        for (total in itemTotals) {
            sum = sum.add(java.math.BigDecimal.valueOf(total))
        }
        return sum.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun calculateTax(taxableAmount: Double, taxRatePercent: Double): Double {
        if (taxRatePercent <= 0.0 || taxableAmount <= 0.0) return 0.0
        val amount = java.math.BigDecimal.valueOf(taxableAmount)
        val rate = java.math.BigDecimal.valueOf(taxRatePercent).divide(java.math.BigDecimal.valueOf(100.0), 6, java.math.RoundingMode.HALF_UP)
        return amount.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun calculateTotal(subtotal: Double, discount: Double, tax: Double): Double {
        val sub = java.math.BigDecimal.valueOf(subtotal)
        val disc = java.math.BigDecimal.valueOf(discount)
        val tx = java.math.BigDecimal.valueOf(tax)
        val netBeforeTax = sub.subtract(disc).max(java.math.BigDecimal.ZERO)
        return netBeforeTax.add(tx).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun calculateBalance(total: Double, amountPaid: Double): Double {
        val tot = java.math.BigDecimal.valueOf(total)
        val paid = java.math.BigDecimal.valueOf(amountPaid)
        val bal = tot.subtract(paid).max(java.math.BigDecimal.ZERO)
        return bal.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun determinePaymentStatus(total: Double, amountPaid: Double): PaymentStatus {
        val roundedTotal = roundTwoDecimals(total)
        val roundedPaid = roundTwoDecimals(amountPaid)
        return when {
            roundedPaid >= roundedTotal -> PaymentStatus.PAID
            roundedPaid <= 0.0 -> PaymentStatus.UNPAID
            else -> PaymentStatus.PARTIALLY_PAID
        }
    }

    fun calculateCOGS(items: List<Pair<Double, Double>>): Double {
        // Pair(quantity, purchasePrice)
        var sum = java.math.BigDecimal.ZERO
        for ((qty, cost) in items) {
            val q = java.math.BigDecimal.valueOf(qty)
            val c = java.math.BigDecimal.valueOf(cost)
            sum = sum.add(q.multiply(c))
        }
        return sum.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun calculateGrossProfit(revenue: Double, cogs: Double): Double {
        val rev = java.math.BigDecimal.valueOf(revenue)
        val cost = java.math.BigDecimal.valueOf(cogs)
        return rev.subtract(cost).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun calculateNetProfit(grossProfit: Double, expenses: Double): Double {
        val gross = java.math.BigDecimal.valueOf(grossProfit)
        val exp = java.math.BigDecimal.valueOf(expenses)
        return gross.subtract(exp).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    fun roundTwoDecimals(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return java.math.BigDecimal.valueOf(value)
            .setScale(2, java.math.RoundingMode.HALF_UP)
            .toDouble()
    }

    fun formatCurrency(amount: Double, currencyCode: String = "USD"): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formattedNumber = formatter.format(amount)
        return when (currencyCode.uppercase()) {
            "USD", "$" -> "$$formattedNumber"
            "EUR", "€" -> "€$formattedNumber"
            "GBP", "£" -> "£$formattedNumber"
            "SAR" -> "$formattedNumber SAR"
            "AED" -> "$formattedNumber AED"
            "EGP" -> "$formattedNumber EGP"
            else -> "$formattedNumber $currencyCode"
        }
    }

    fun formatQuantity(quantity: Double, unit: String = ""): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val isWhole = quantity % 1.0 == 0.0
        val pattern = if (isWhole) "#,##0" else "#,##0.##"
        val formatted = DecimalFormat(pattern, symbols).format(quantity)
        return if (unit.isNotBlank()) "$formatted $unit" else formatted
    }
}
