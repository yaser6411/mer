package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.Sale
import com.example.domain.FinancialEngine
import com.example.ui.components.MetricCard
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import java.util.Calendar

@Composable
fun ReportsScreen(
    sales: List<Sale>,
    expenses: List<Expense>,
    products: List<Product>,
    customerReceivables: Double,
    supplierPayables: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf("THIS_MONTH") } // TODAY, 7_DAYS, THIS_MONTH, ALL_TIME

    val now = System.currentTimeMillis()
    val filterStartTime = remember(selectedTimeframe) {
        val cal = Calendar.getInstance()
        when (selectedTimeframe) {
            "TODAY" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "7_DAYS" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> 0L
        }
    }

    val periodSales = remember(sales, filterStartTime) {
        sales.filter { it.date >= filterStartTime }
    }
    val periodExpenses = remember(expenses, filterStartTime) {
        expenses.filter { it.date >= filterStartTime }
    }

    val totalRevenue = remember(periodSales) { periodSales.sumOf { it.total } }
    val totalExpenses = remember(periodExpenses) { periodExpenses.sumOf { it.amount } }
    val netProfit = remember(totalRevenue, totalExpenses) { totalRevenue - totalExpenses }
    val averageOrderValue = remember(totalRevenue, periodSales) {
        if (periodSales.isNotEmpty()) totalRevenue / periodSales.size else 0.0
    }

    val totalInventoryCost = remember(products) { products.sumOf { it.stock * it.purchasePrice } }
    val totalInventoryRetail = remember(products) { products.sumOf { it.stock * it.sellingPrice } }
    val potentialMargin = remember(totalInventoryCost, totalInventoryRetail) { totalInventoryRetail - totalInventoryCost }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeframe Selector Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "TODAY" to stringResource(R.string.reports_timeframe_today),
                    "7_DAYS" to stringResource(R.string.reports_timeframe_7_days),
                    "THIS_MONTH" to stringResource(R.string.reports_timeframe_this_month),
                    "ALL_TIME" to stringResource(R.string.reports_timeframe_all_time)
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedTimeframe == key,
                        onClick = { selectedTimeframe = key },
                        label = { Text(label) }
                    )
                }
            }
        }

        // P&L Statement Card
        item {
            Text(stringResource(R.string.reports_pl_statement), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_gross_revenue), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(totalRevenue, currency), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_operating_expenses), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(totalExpenses, currency), fontWeight = FontWeight.Bold, color = StatusDanger)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_net_profit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            FinancialEngine.formatCurrency(netProfit, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) StatusSuccess else StatusDanger
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_orders_count), fontSize = 12.sp, color = Color.Gray)
                        Text(stringResource(R.string.reports_orders_summary, periodSales.size.toString(), FinancialEngine.formatCurrency(averageOrderValue, currency)), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Inventory Valuation Summary
        item {
            Text(stringResource(R.string.reports_inventory_valuation), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_total_inventory_cost), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(totalInventoryCost, currency), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_expected_retail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(totalInventoryRetail, currency), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.reports_projected_margin), fontWeight = FontWeight.Bold)
                        Text(
                            FinancialEngine.formatCurrency(potentialMargin, currency),
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess
                        )
                    }
                }
            }
        }

        // Debt Position
        item {
            Text(stringResource(R.string.reports_debt_position), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = stringResource(R.string.reports_receivables_customers),
                    value = FinancialEngine.formatCurrency(customerReceivables, currency),
                    icon = Icons.Default.AttachMoney,
                    accentColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.reports_payables_suppliers),
                    value = FinancialEngine.formatCurrency(supplierPayables, currency),
                    icon = Icons.Default.MoneyOff,
                    accentColor = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
