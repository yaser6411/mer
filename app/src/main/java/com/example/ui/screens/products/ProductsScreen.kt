package com.example.ui.screens.products

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import com.example.data.local.entity.StockMovement
import com.example.domain.FinancialEngine
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductsScreen(
    products: List<Product>,
    categories: List<Category>,
    currency: String,
    onSaveProduct: (Product, () -> Unit, (String) -> Unit) -> Unit,
    onAdjustStock: (Long, Double, String, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onFetchMovements: suspend (Long) -> List<StockMovement>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var onlyLowStock by remember { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    var productToAdjust by remember { mutableStateOf<Product?>(null) }
    var showAdjustDialog by remember { mutableStateOf(false) }

    var productToViewHistory by remember { mutableStateOf<Product?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Filter products
    val filteredProducts = remember(products, searchQuery, selectedCategoryId, onlyLowStock) {
        products.filter { product ->
            val matchesQuery = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.sku.contains(searchQuery, ignoreCase = true) ||
                    product.barcode.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesLowStock = !onlyLowStock || product.stock <= product.minStock

            matchesQuery && matchesCategory && matchesLowStock
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("products_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("product_search_input"),
                    placeholder = { Text(stringResource(R.string.product_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null && !onlyLowStock,
                            onClick = {
                                selectedCategoryId = null
                                onlyLowStock = false
                            },
                            label = { Text(stringResource(R.string.product_filter_all)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = onlyLowStock,
                            onClick = { onlyLowStock = !onlyLowStock },
                            label = { Text(stringResource(R.string.product_low_stock)) },
                            leadingIcon = {
                                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                            },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory,
                    title = "No Products Found",
                    description = if (products.isEmpty()) "Your inventory is currently empty. Tap '+ Add Product' to create your first item." else "No products match the selected search or filter.",
                    actionButtonText = if (products.isEmpty()) "Add Product" else null,
                    onAction = {
                        productToEdit = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            currency = currency,
                            onEdit = {
                                productToEdit = product
                                showAddEditDialog = true
                            },
                            onAdjust = {
                                productToAdjust = product
                                showAdjustDialog = true
                            },
                            onHistory = {
                                productToViewHistory = product
                                showHistoryDialog = true
                            },
                            onDelete = {
                                productToDelete = product
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Product
        FloatingActionButton(
            onClick = {
                productToEdit = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.product_add_title), tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    // Add / Edit Product Dialog
    if (showAddEditDialog) {
        AddEditProductDialog(
            product = productToEdit,
            categories = categories,
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedProduct ->
                onSaveProduct(updatedProduct, { showAddEditDialog = false }, {})
            }
        )
    }

    // Stock Adjustment Dialog
    if (showAdjustDialog && productToAdjust != null) {
        StockAdjustmentDialog(
            product = productToAdjust!!,
            onDismiss = { showAdjustDialog = false },
            onConfirm = { newStock, reason ->
                onAdjustStock(productToAdjust!!.id, newStock, reason, { showAdjustDialog = false }, {})
            }
        )
    }

    // Stock History Dialog
    if (showHistoryDialog && productToViewHistory != null) {
        StockHistoryDialog(
            product = productToViewHistory!!,
            onDismiss = { showHistoryDialog = false },
            onFetchMovements = onFetchMovements
        )
    }

    // Confirm Delete Dialog
    if (productToDelete != null) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            onConfirm = {
                onDeleteProduct(productToDelete!!)
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    currency: String,
    onEdit: () -> Unit,
    onAdjust: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.sku.isNotBlank() || product.barcode.isNotBlank()) {
                        Text(
                            text = buildString {
                                if (product.sku.isNotBlank()) append("SKU: ${product.sku}  ")
                                if (product.barcode.isNotBlank()) append("Barcode: ${product.barcode}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(
                    status = when {
                        product.stock <= 0 -> "OUT_OF_STOCK"
                        product.stock <= product.minStock -> "LOW_STOCK"
                        else -> "IN_STOCK"
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.product_selling_price), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        FinancialEngine.formatCurrency(product.sellingPrice, currency),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(stringResource(R.string.product_cost_price), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        FinancialEngine.formatCurrency(product.purchasePrice, currency),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.product_stock_qty), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${FinancialEngine.formatQuantity(product.stock)} ${product.unit}",
                        fontWeight = FontWeight.Bold,
                        color = if (product.stock <= product.minStock) StatusDanger else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onHistory, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.History, contentDescription = stringResource(R.string.product_stock_history), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onAdjust, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Tune, contentDescription = stringResource(R.string.product_adjust_title), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.product_edit_title), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: Product?,
    categories: List<Category>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var sellingPriceText by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "0.0") }
    var purchasePriceText by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "0.0") }
    var stockText by remember { mutableStateOf(product?.stock?.toString() ?: "0.0") }
    var minStockText by remember { mutableStateOf(product?.minStock?.toString() ?: "5.0") }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }
    var selectedCatId by remember { mutableStateOf(product?.categoryId) }
    var notes by remember { mutableStateOf(product?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) stringResource(R.string.product_add_title) else stringResource(R.string.product_edit_title), fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    item {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.product_name_required)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text(stringResource(R.string.product_sku)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text(stringResource(R.string.product_barcode)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = { sellingPriceText = it },
                            label = { Text("${stringResource(R.string.product_selling_price)} ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = purchasePriceText,
                            onValueChange = { purchasePriceText = it },
                            label = { Text("${stringResource(R.string.product_cost_price)} ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text(stringResource(R.string.product_stock_qty)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = minStockText,
                            onValueChange = { minStockText = it },
                            label = { Text(stringResource(R.string.product_min_alert)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(stringResource(R.string.product_unit_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.product_notes_optional)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Product name cannot be empty."
                        return@Button
                    }
                    val sell = sellingPriceText.toDoubleOrNull() ?: 0.0
                    val cost = purchasePriceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toDoubleOrNull() ?: 0.0
                    val minStock = minStockText.toDoubleOrNull() ?: 0.0

                    val result = (product ?: Product(name = name)).copy(
                        name = name.trim(),
                        sku = sku.trim(),
                        barcode = barcode.trim(),
                        sellingPrice = sell,
                        purchasePrice = cost,
                        stock = stock,
                        minStock = minStock,
                        unit = unit.trim().ifBlank { "pcs" },
                        categoryId = selectedCatId,
                        notes = notes.trim(),
                        updatedDate = System.currentTimeMillis()
                    )
                    onSave(result)
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var newStockText by remember { mutableStateOf(product.stock.toString()) }
    var reason by remember { mutableStateOf("Manual adjustment / count") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.product_adjust_named, product.name), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.product_current_stock_label, product.stock, product.unit), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = newStockText,
                    onValueChange = { newStockText = it },
                    label = { Text(stringResource(R.string.product_new_stock_qty)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.product_adjust_reason)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = newStockText.toDoubleOrNull() ?: product.stock
                    onConfirm(qty, reason)
                }
            ) {
                Text(stringResource(R.string.product_confirm_adjustment))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun StockHistoryDialog(
    product: Product,
    onDismiss: () -> Unit,
    onFetchMovements: suspend (Long) -> List<StockMovement>
) {
    var movements by remember { mutableStateOf<List<StockMovement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    LaunchedEffect(product.id) {
        movements = onFetchMovements(product.id)
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.product_movements_title, product.name), fontWeight = FontWeight.Bold) },
        text = {
            if (isLoading) {
                Text(stringResource(R.string.product_loading_movements))
            } else if (movements.isEmpty()) {
                Text(stringResource(R.string.product_no_movements))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movements) { m ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = m.type,
                                        fontWeight = FontWeight.Bold,
                                        color = if (m.quantity >= 0) StatusSuccess else StatusDanger
                                    )
                                    Text(
                                        text = "${if (m.quantity >= 0) "+" else ""}${m.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (m.quantity >= 0) StatusSuccess else StatusDanger
                                    )
                                }
                                Text(stringResource(R.string.product_movement_balance, m.previousStock, m.newStock), fontSize = 12.sp)
                                if (m.notes.isNotBlank()) {
                                    Text(stringResource(R.string.product_movement_note, m.notes), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(dateFormat.format(Date(m.timestamp)), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}
