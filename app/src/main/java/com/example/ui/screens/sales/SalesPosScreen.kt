package com.example.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.domain.FinancialEngine
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.CartItem

@Composable
fun SalesPosScreen(
    products: List<Product>,
    customers: List<Customer>,
    cartItems: List<CartItem>,
    selectedCustomer: Customer?,
    cartDiscount: Double,
    cartTaxRate: Double,
    currency: String,
    onAddToCart: (Product) -> Unit,
    onUpdateQuantity: (Long, Double) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onSelectCustomer: (Customer?) -> Unit,
    onSetDiscount: (Double) -> Unit,
    onSetTaxRate: (Double) -> Unit,
    onCompleteSale: (Double, String, (Long) -> Unit, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subtotal = remember(cartItems) {
        FinancialEngine.calculateSubtotal(cartItems.map { it.total })
    }
    val taxAmount = remember(subtotal, cartDiscount, cartTaxRate) {
        FinancialEngine.calculateTax(maxOf(0.0, subtotal - cartDiscount), cartTaxRate)
    }
    val grandTotal = remember(subtotal, cartDiscount, taxAmount) {
        FinancialEngine.calculateTotal(subtotal, cartDiscount, taxAmount)
    }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products.filter { it.isActive }
        else products.filter {
            it.isActive && (it.name.contains(searchQuery, ignoreCase = true) ||
                    it.sku.contains(searchQuery, ignoreCase = true) ||
                    it.barcode.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(modifier = modifier.fillMaxSize().testTag("sales_pos_screen")) {
        // Customer Selector Bar & Search
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
                    .clickable { showCustomerPicker = true }
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedCustomer?.name ?: stringResource(R.string.pos_walk_in_customer),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (cartItems.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearCart,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(stringResource(R.string.pos_clear_cart), fontSize = 12.sp)
                }
            }
        }

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.pos_search_product)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("pos_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // Products Horizontal or Grid Picker
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts) { product ->
                Card(
                    modifier = Modifier
                        .clickable { onAddToCart(product) }
                        .testTag("pos_product_item_${product.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (product.name.length > 18) product.name.take(16) + "..." else product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FinancialEngine.formatCurrency(product.sellingPrice, currency),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${stringResource(R.string.product_stock)}: ${product.stock}",
                            fontSize = 11.sp,
                            color = if (product.stock <= product.minStock) StatusDanger else Color.Gray
                        )
                    }
                }
            }
        }

        // Cart Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stringResource(R.string.pos_cart)} (${cartItems.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (cartItems.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.pos_subtotal)}: ${FinancialEngine.formatCurrency(subtotal, currency)}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cart Items List
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.ShoppingCart,
                    title = stringResource(R.string.pos_cart),
                    description = stringResource(R.string.pos_empty_cart)
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
                                    stringResource(R.string.purchases_cost_each, FinancialEngine.formatCurrency(item.unitPrice, currency)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quantity Controls (- / +)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, item.quantity - 1.0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.action_delete), modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = FinancialEngine.formatQuantity(item.quantity),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, item.quantity + 1.0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.product_add_title), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = FinancialEngine.formatCurrency(item.total, currency),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = { onRemoveFromCart(item.product.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Bottom Checkout Bar
        if (cartItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.pos_subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FinancialEngine.formatCurrency(subtotal, currency))
                    }
                    if (cartDiscount > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.pos_discount), color = StatusDanger)
                            Text("- ${FinancialEngine.formatCurrency(cartDiscount, currency)}", color = StatusDanger)
                        }
                    }
                    if (taxAmount > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${stringResource(R.string.pos_tax)} (${cartTaxRate}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FinancialEngine.formatCurrency(taxAmount, currency))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.pos_total), style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = FinancialEngine.formatCurrency(grandTotal, currency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { showCheckoutDialog = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("pos_checkout_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.pos_charge_btn, FinancialEngine.formatCurrency(grandTotal, currency)), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text(stringResource(R.string.pos_select_customer), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCustomer(null)
                                    showCustomerPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(stringResource(R.string.pos_walk_in_customer), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(customers) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCustomer(c)
                                    showCustomerPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(c.name, fontWeight = FontWeight.Bold)
                                    if (c.phone.isNotBlank()) Text(c.phone, fontSize = 12.sp, color = Color.Gray)
                                }
                                if (c.balance != 0.0) {
                                    Text(
                                        "Bal: ${FinancialEngine.formatCurrency(c.balance, currency)}",
                                        fontSize = 12.sp,
                                        color = if (c.balance > 0) StatusDanger else StatusSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCustomerPicker = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    // Complete Checkout Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            grandTotal = grandTotal,
            currency = currency,
            customer = selectedCustomer,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { amountPaid, method ->
                onCompleteSale(
                    amountPaid,
                    method,
                    {
                        showCheckoutDialog = false
                    },
                    { err ->
                        errorMessage = err
                    }
                )
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(stringResource(R.string.error_transaction), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
fun CheckoutDialog(
    grandTotal: Double,
    currency: String,
    customer: Customer?,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountPaidText by remember { mutableStateOf(grandTotal.toString()) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    val paymentMethods = listOf("Cash", "Card", "Bank Transfer", "Cheque", "Other")

    val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0
    val balanceDue = FinancialEngine.calculateBalance(grandTotal, amountPaid)
    val changeAmount = if (amountPaid > grandTotal) amountPaid - grandTotal else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pos_checkout_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.pos_total_due), style = MaterialTheme.typography.titleMedium)
                    Text(
                        FinancialEngine.formatCurrency(grandTotal, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Payment Method Selector
                Text(stringResource(R.string.pos_payment_method), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(paymentMethods) { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                // Amount Paid Input
                OutlinedTextField(
                    value = amountPaidText,
                    onValueChange = { amountPaidText = it },
                    label = { Text("${stringResource(R.string.pos_amount_paid)} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("checkout_paid_input"),
                    singleLine = true
                )

                // Quick Cash Presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { amountPaidText = grandTotal.toString() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.pos_pay_exact), fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { amountPaidText = "0.0" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.pos_pay_credit), fontSize = 11.sp)
                    }
                }

                if (changeAmount > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.pos_change_due), fontWeight = FontWeight.Bold, color = StatusSuccess)
                        Text(FinancialEngine.formatCurrency(changeAmount, currency), fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                } else if (balanceDue > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.pos_remaining_due), fontWeight = FontWeight.Bold, color = StatusDanger)
                        Text(FinancialEngine.formatCurrency(balanceDue, currency), fontWeight = FontWeight.Bold, color = StatusDanger)
                    }
                    if (customer == null) {
                        Text(
                            "Warning: Unpaid balance on walk-in customer will not be tracked in customer accounts.",
                            fontSize = 11.sp,
                            color = StatusWarning
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountPaid, selectedMethod) },
                modifier = Modifier.testTag("confirm_checkout_button")
            ) {
                Text(stringResource(R.string.pos_confirm_print))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
