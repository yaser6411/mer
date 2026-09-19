package com.example.ui.screens.purchases

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.Supplier
import com.example.domain.FinancialEngine
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.PurchaseCartItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchasesScreen(
    products: List<Product>,
    suppliers: List<Supplier>,
    purchases: List<Purchase>,
    cartItems: List<PurchaseCartItem>,
    selectedSupplier: Supplier?,
    currency: String,
    onAddToCart: (Product, Double, Double) -> Unit,
    onUpdateQuantity: (Long, Double) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onSelectSupplier: (Supplier?) -> Unit,
    onCompletePurchase: (String, Double, String, String, (Long) -> Unit, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: New Purchase, 1: Purchase History
    var searchQuery by remember { mutableStateOf("") }
    var showSupplierPicker by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subtotal = remember(cartItems) {
        FinancialEngine.calculateSubtotal(cartItems.map { it.total })
    }

    Column(modifier = modifier.fillMaxSize().testTag("purchases_screen")) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.purchases_tab_new)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.purchases_tab_history, purchases.size)) }
            )
        }

        if (selectedTab == 0) {
            // New Purchase View
            Column(modifier = Modifier.fillMaxSize()) {
                // Supplier Selector Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { showSupplierPicker = true }
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedSupplier?.name ?: stringResource(R.string.purchases_select_supplier),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    if (cartItems.isNotEmpty()) {
                        OutlinedButton(onClick = onClearCart) {
                            Text(stringResource(R.string.action_clear), fontSize = 12.sp)
                        }
                    }
                }

                // Search Products to Purchase
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.purchases_search_product)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Products Picker Row
                val filteredProducts = remember(products, searchQuery) {
                    if (searchQuery.isBlank()) products
                    else products.filter {
                        it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            modifier = Modifier
                                .clickable { onAddToCart(product, 1.0, product.purchasePrice) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (product.name.length > 16) product.name.take(14) + "..." else product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text("${stringResource(R.string.product_cost_price)}: ${FinancialEngine.formatCurrency(product.purchasePrice, currency)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text(stringResource(R.string.purchases_stock_current, product.stock, product.unit), fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // Purchase Cart Items List
                if (cartItems.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyStateView(
                            icon = Icons.Default.ShoppingCart,
                            title = stringResource(R.string.purchases_empty_title),
                            description = stringResource(R.string.purchases_empty_desc)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cartItems, key = { it.product.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.product.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            stringResource(R.string.purchases_cost_each, FinancialEngine.formatCurrency(item.unitCost, currency)),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { onUpdateQuantity(item.product.id, item.quantity - 1.0) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.action_delete), modifier = Modifier.size(16.dp))
                                        }
                                        Text(FinancialEngine.formatQuantity(item.quantity), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                        IconButton(onClick = { onUpdateQuantity(item.product.id, item.quantity + 1.0) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.product_add_title), modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(FinancialEngine.formatCurrency(item.total, currency), fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { onRemoveFromCart(item.product.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Summary Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stringResource(R.string.purchases_total_cost), style = MaterialTheme.typography.labelMedium)
                                Text(
                                    FinancialEngine.formatCurrency(subtotal, currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = { showCheckoutDialog = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(stringResource(R.string.purchases_record_btn), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Purchase History View
            if (purchases.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ShoppingCart,
                    title = stringResource(R.string.purchases_no_records_title),
                    description = stringResource(R.string.purchases_no_records_desc)
                )
            } else {
                val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases, key = { it.id }) { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("#${p.invoiceNumber}", fontWeight = FontWeight.Bold)
                                        Text(stringResource(R.string.purchases_supplier_label, p.supplierName), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(p.paymentStatus)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${stringResource(R.string.pos_total)}: ${FinancialEngine.formatCurrency(p.total, currency)}", fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.pos_paid_label, FinancialEngine.formatCurrency(p.amountPaid, currency)), color = StatusSuccess)
                                    if (p.balance > 0) {
                                        Text(stringResource(R.string.pos_due_label, FinancialEngine.formatCurrency(p.balance, currency)), color = StatusDanger, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dateFormat.format(Date(p.date)), fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    // Supplier Picker
    if (showSupplierPicker) {
        AlertDialog(
            onDismissRequest = { showSupplierPicker = false },
            title = { Text(stringResource(R.string.purchases_select_supplier), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSupplier(null)
                                    showSupplierPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(stringResource(R.string.purchases_direct_supplier), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(suppliers) { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSupplier(s)
                                    showSupplierPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.name, fontWeight = FontWeight.Bold)
                                if (s.balance != 0.0) {
                                    Text(stringResource(R.string.purchases_payable_label, FinancialEngine.formatCurrency(s.balance, currency)), color = StatusDanger, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSupplierPicker = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    // Record Purchase Dialog
    if (showCheckoutDialog) {
        var invoiceNum by remember { mutableStateOf("PO-${System.currentTimeMillis() % 100000}") }
        var paidText by remember { mutableStateOf(subtotal.toString()) }
        var method by remember { mutableStateOf("Cash") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text(stringResource(R.string.purchases_complete_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.purchases_supplier_label, selectedSupplier?.name ?: stringResource(R.string.purchases_direct_supplier)), fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.purchases_total_amount, FinancialEngine.formatCurrency(subtotal, currency)), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = invoiceNum,
                        onValueChange = { invoiceNum = it },
                        label = { Text(stringResource(R.string.purchases_invoice_no)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("${stringResource(R.string.pos_amount_paid)} ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.product_notes_optional)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paid = paidText.toDoubleOrNull() ?: subtotal
                        onCompletePurchase(
                            invoiceNum,
                            paid,
                            method,
                            notes,
                            { showCheckoutDialog = false },
                            { err -> errorMessage = err }
                        )
                    }
                ) {
                    Text(stringResource(R.string.purchases_confirm_stock_in))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCheckoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error) },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}
