package com.example.domain.usecase

import com.example.data.repository.MercuryRepository
import com.example.domain.FinancialEngine
import com.example.ui.viewmodel.DashboardSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

/**
 * Use case to aggregate core business KPIs for the executive dashboard.
 * Encapsulates the financial logic for daily, monthly, and asset valuations.
 */
class GetDashboardSummaryUseCase(
    private val repository: MercuryRepository
) {
    operator fun invoke(): Flow<DashboardSummary> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis

        val endOfDay = System.currentTimeMillis() + 86400000L

        val todaySalesFlow = repository.getSalesTotalBetweenDates(startOfDay, endOfDay)
        val todayPurchasesFlow = repository.getPurchasesTotalBetweenDates(startOfDay, endOfDay)
        val todayExpensesFlow = repository.getExpensesTotalBetweenDates(startOfDay, endOfDay)
        val monthlySalesFlow = repository.getSalesTotalBetweenDates(startOfMonth, endOfDay)
        val monthlyExpensesFlow = repository.getExpensesTotalBetweenDates(startOfMonth, endOfDay)
        val inventoryValueFlow = repository.totalInventoryCostValue
        val lowStockCountFlow = repository.lowStockCount
        val receivablesFlow = repository.totalReceivables
        val payablesFlow = repository.totalPayables

        return combine(
            combine(todaySalesFlow, todayPurchasesFlow, todayExpensesFlow) { s, pu, e ->
                Triple(s, pu, e)
            },
            combine(monthlySalesFlow, monthlyExpensesFlow, inventoryValueFlow) { ms, me, inv ->
                Triple(ms, me, inv)
            },
            combine(lowStockCountFlow, receivablesFlow, payablesFlow) { low, rec, pay ->
                Triple(low, rec, pay)
            }
        ) { t1, t2, t3 ->
            val (todaySales, todayPurchases, todayExpenses) = t1
            val (monthlySales, monthlyExpenses, inventoryValue) = t2
            val (lowStock, receivables, payables) = t3

            val netTodayProfit = FinancialEngine.calculateNetProfit(todaySales - todayPurchases, todayExpenses)
            val netMonthlyProfit = FinancialEngine.calculateNetProfit(monthlySales, monthlyExpenses)

            DashboardSummary(
                todaySales = todaySales,
                todayPurchases = todayPurchases,
                todayExpenses = todayExpenses,
                todayProfit = netTodayProfit,
                monthlyRevenue = monthlySales,
                monthlyExpenses = monthlyExpenses,
                monthlyProfit = netMonthlyProfit,
                inventoryValue = inventoryValue,
                lowStockCount = lowStock,
                customerBalances = receivables,
                supplierBalances = payables
            )
        }
    }
}
