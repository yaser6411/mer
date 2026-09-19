package com.example

import com.example.domain.FinancialEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialEngineTest {

    @Test
    fun testSubtotalCalculation() {
        val itemTotals = listOf(10.50, 20.25, 4.25)
        val subtotal = FinancialEngine.calculateSubtotal(itemTotals)
        assertEquals(35.0, subtotal, 0.001)
    }

    @Test
    fun testItemTotalWithDiscount() {
        val total = FinancialEngine.calculateItemTotal(quantity = 2.0, unitPrice = 15.0, discount = 5.0)
        // 2 * 15 - 5 = 25.0
        assertEquals(25.0, total, 0.001)
    }

    @Test
    fun testTaxCalculation() {
        val tax = FinancialEngine.calculateTax(taxableAmount = 100.0, taxRatePercent = 15.0)
        assertEquals(15.0, tax, 0.001)
    }

    @Test
    fun testTotalCalculation() {
        val grandTotal = FinancialEngine.calculateTotal(subtotal = 100.0, discount = 10.0, tax = 13.5)
        // 100 - 10 + 13.5 = 103.5
        assertEquals(103.5, grandTotal, 0.001)
    }

    @Test
    fun testBalanceCalculation() {
        val balance = FinancialEngine.calculateBalance(total = 100.0, amountPaid = 60.0)
        assertEquals(40.0, balance, 0.001)

        val fullPaidBalance = FinancialEngine.calculateBalance(total = 100.0, amountPaid = 100.0)
        assertEquals(0.0, fullPaidBalance, 0.001)

        val overpaidBalance = FinancialEngine.calculateBalance(total = 100.0, amountPaid = 120.0)
        assertEquals(0.0, overpaidBalance, 0.001)
    }

    @Test
    fun testPaymentStatusDetermination() {
        assertEquals(FinancialEngine.PaymentStatus.PAID, FinancialEngine.determinePaymentStatus(total = 100.0, amountPaid = 100.0))
        assertEquals(FinancialEngine.PaymentStatus.PAID, FinancialEngine.determinePaymentStatus(total = 100.0, amountPaid = 120.0))
        assertEquals(FinancialEngine.PaymentStatus.PARTIALLY_PAID, FinancialEngine.determinePaymentStatus(total = 100.0, amountPaid = 50.0))
        assertEquals(FinancialEngine.PaymentStatus.UNPAID, FinancialEngine.determinePaymentStatus(total = 100.0, amountPaid = 0.0))
    }

    @Test
    fun testQuantityFormatting() {
        assertEquals("5", FinancialEngine.formatQuantity(5.0))
        assertEquals("5.5", FinancialEngine.formatQuantity(5.5))
        assertEquals("10.25", FinancialEngine.formatQuantity(10.25))
    }

    @Test
    fun testZeroTotalSalePaymentStatus() {
        assertEquals(FinancialEngine.PaymentStatus.PAID, FinancialEngine.determinePaymentStatus(total = 0.0, amountPaid = 0.0))
    }

    @Test
    fun testProfitCalculations() {
        val cogs = FinancialEngine.calculateCOGS(listOf(Pair(10.0, 5.0), Pair(2.0, 12.50)))
        // 10*5 + 2*12.5 = 50 + 25 = 75.0
        assertEquals(75.0, cogs, 0.001)

        val grossProfit = FinancialEngine.calculateGrossProfit(revenue = 120.0, cogs = 75.0)
        assertEquals(45.0, grossProfit, 0.001)

        val netProfit = FinancialEngine.calculateNetProfit(grossProfit = 45.0, expenses = 15.0)
        assertEquals(30.0, netProfit, 0.001)
    }

    @Test
    fun testHalfUpRoundingPrecision() {
        val value = FinancialEngine.roundTwoDecimals(1.005)
        assertEquals(1.01, value, 0.0001)
    }
}
