package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.BusinessProfile
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.Sale
import com.example.domain.FinancialEngine
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.ScreenDestination
import java.util.Calendar

@Composable
fun DashboardScreen(
    profile: BusinessProfile?,
    sales: List<Sale>,
    purchases: List<Purchase>,
    expenses: List<Expense>,
    lowStockProducts: List<Product>,
    inventoryCostValue: Double,
    customerReceivables: Double,
    supplierPayables: Double,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = profile?.currency ?: "USD"

    // Real-time calculations for Today
    val (todayStart, todayEnd) = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        start to end
    }

    // Real-time calculations for Month
    val (monthStart, monthEnd) = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        start to end
    }

    val todaySalesTotal = remember(sales, todayStart, todayEnd) {
        sales.filter { it.date in todayStart..todayEnd }.sumOf { it.total }
    }
    val todayPurchasesTotal = remember(purchases, todayStart, todayEnd) {
        purchases.filter { it.date in todayStart..todayEnd }.sumOf { it.total }
    }
    val todayExpensesTotal = remember(expenses, todayStart, todayEnd) {
        expenses.filter { it.date in todayStart..todayEnd }.sumOf { it.amount }
    }
    val todayEstProfit = remember(todaySalesTotal, todayExpensesTotal) {
        todaySalesTotal - todayExpensesTotal
    }

    val monthlyRevenue = remember(sales, monthStart, monthEnd) {
        sales.filter { it.date in monthStart..monthEnd }.sumOf { it.total }
    }
    val monthlyExpenses = remember(expenses, monthStart, monthEnd) {
        expenses.filter { it.date in monthStart..monthEnd }.sumOf { it.amount }
    }
    val monthlyNetProfit = remember(monthlyRevenue, monthlyExpenses) {
        monthlyRevenue - monthlyExpenses
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile?.name?.ifBlank { "MERCURY Business" } ?: "MERCURY Business",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Offline-First Management System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currency,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "New Sale",
                    icon = Icons.Default.PointOfSale,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenDestination.SALES_POS) }
                )
                QuickActionButton(
                    title = "Add Product",
                    icon = Icons.Default.Inventory,
                    color = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenDestination.PRODUCTS) }
                )
                QuickActionButton(
                    title = "Purchase",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFF4F46E5),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenDestination.PURCHASES) }
                )
                QuickActionButton(
                    title = "Expense",
                    icon = Icons.Default.MoneyOff,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenDestination.EXPENSES) }
                )
            }
        }

        // Today's Performance Row
        item {
            Text(
                text = "Today's Performance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.dashboard_today_sales),
                    value = FinancialEngine.formatCurrency(todaySalesTotal, currency),
                    icon = Icons.Default.AttachMoney,
                    accentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.dashboard_today_profit),
                    value = FinancialEngine.formatCurrency(todayEstProfit, currency),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = if (todayEstProfit >= 0) StatusSuccess else StatusDanger,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.dashboard_today_purchases),
                    value = FinancialEngine.formatCurrency(todayPurchasesTotal, currency),
                    icon = Icons.Default.ShoppingCart,
                    accentColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.dashboard_today_expenses),
                    value = FinancialEngine.formatCurrency(todayExpensesTotal, currency),
                    icon = Icons.Default.MoneyOff,
                    accentColor = StatusDanger,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Monthly Summary
        item {
            Text(
                text = stringResource(R.string.dashboard_monthly_overview),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.dashboard_monthly_revenue), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(monthlyRevenue, currency), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.dashboard_monthly_expenses), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(monthlyExpenses, currency), fontWeight = FontWeight.SemiBold, color = StatusDanger)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.dashboard_monthly_net_profit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            FinancialEngine.formatCurrency(monthlyNetProfit, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (monthlyNetProfit >= 0) StatusSuccess else StatusDanger
                        )
                    }
                }
            }
        }

        // Inventory, Receivables & Payables
        item {
            Text(
                text = stringResource(R.string.dashboard_balances_inventory),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.dashboard_inventory_value),
                    value = FinancialEngine.formatCurrency(inventoryCostValue, currency),
                    icon = Icons.Default.Inventory,
                    accentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.dashboard_customer_receivables),
                    value = FinancialEngine.formatCurrency(customerReceivables, currency),
                    icon = Icons.Default.People,
                    accentColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    subtitle = stringResource(R.string.dashboard_owed_by_customers)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            MetricCard(
                title = stringResource(R.string.dashboard_supplier_payables),
                value = FinancialEngine.formatCurrency(supplierPayables, currency),
                icon = Icons.Default.AccountBalance,
                accentColor = Color(0xFFDC2626),
                modifier = Modifier.fillMaxWidth(),
                subtitle = stringResource(R.string.dashboard_owed_to_suppliers)
            )
        }

        // Low Stock Alert Section
        if (lowStockProducts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.dashboard_low_stock_alerts)} (${lowStockProducts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusDanger
                    )
                    Text(
                        text = stringResource(R.string.action_view_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigate(ScreenDestination.PRODUCTS) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(lowStockProducts.take(5)) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ScreenDestination.PRODUCTS) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                stringResource(R.string.product_current_stock_label, product.minStock, product.unit),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(if (product.stock <= 0) "OUT_OF_STOCK" else "LOW_STOCK")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick)
            .testTag("quick_action_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.09f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
