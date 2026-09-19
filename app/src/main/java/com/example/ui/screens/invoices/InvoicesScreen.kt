package com.example.ui.screens.invoices

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.BusinessProfile
import com.example.data.local.entity.Invoice
import com.example.data.local.entity.SaleItem
import com.example.domain.FinancialEngine
import com.example.invoice.InvoicePdfGenerator
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
    invoices: List<Invoice>,
    profile: BusinessProfile?,
    currency: String,
    onFetchSaleItems: suspend (Long) -> List<SaleItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedInvoiceForPreview by remember { mutableStateOf<Invoice?>(null) }
    var previewItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var isLoadingPreview by remember { mutableStateOf(false) }

    val filteredInvoices = remember(invoices, searchQuery, selectedStatusFilter) {
        invoices.filter { inv ->
            val matchesQuery = searchQuery.isBlank() ||
                    inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    inv.customerName.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedStatusFilter) {
                "ALL" -> true
                "PAID" -> inv.status.equals("PAID", ignoreCase = true)
                "PARTIAL" -> inv.status.equals("PARTIALLY_PAID", ignoreCase = true)
                "UNPAID" -> inv.status.equals("UNPAID", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesStatus
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxSize().testTag("invoices_screen")) {
        // Search & Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.invoices_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "ALL" to stringResource(R.string.filter_all),
                    "PAID" to stringResource(R.string.status_paid),
                    "PARTIAL" to stringResource(R.string.status_partially_paid),
                    "UNPAID" to stringResource(R.string.status_unpaid)
                ).forEach { (statusKey, labelText) ->
                    FilterChip(
                        selected = selectedStatusFilter == statusKey,
                        onClick = { selectedStatusFilter = statusKey },
                        label = { Text(labelText) }
                    )
                }
            }
        }

        // Invoice List
        if (filteredInvoices.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Description,
                title = stringResource(R.string.invoices_empty_title),
                description = if (invoices.isEmpty()) stringResource(R.string.invoices_empty_desc) else stringResource(R.string.invoices_no_match_desc)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInvoices, key = { it.id }) { inv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedInvoiceForPreview = inv
                                isLoadingPreview = true
                                scope.launch {
                                    previewItems = onFetchSaleItems(inv.saleId)
                                    isLoadingPreview = false
                                }
                            }
                            .testTag("invoice_item_${inv.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("#${inv.invoiceNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(inv.customerName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge(inv.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${stringResource(R.string.pos_total)}: ${FinancialEngine.formatCurrency(inv.total, currency)}", fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.pos_paid_label, FinancialEngine.formatCurrency(inv.paid, currency)), color = StatusSuccess)
                                if (inv.balance > 0) {
                                    Text(stringResource(R.string.pos_due_label, FinancialEngine.formatCurrency(inv.balance, currency)), color = StatusDanger, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateFormat.format(Date(inv.issueDate)), fontSize = 11.sp, color = Color.Gray)
                                Row {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val items = onFetchSaleItems(inv.saleId)
                                                val file = InvoicePdfGenerator.generateInvoicePdf(context, inv, items, profile)
                                                InvoicePdfGenerator.shareInvoicePdf(context, file)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.invoices_share_pdf), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val items = onFetchSaleItems(inv.saleId)
                                                val file = InvoicePdfGenerator.generateInvoicePdf(context, inv, items, profile)
                                                InvoicePdfGenerator.printInvoice(context, file, "Invoice_${inv.invoiceNumber}")
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = stringResource(R.string.invoices_print), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invoice Detail & Action Dialog
    if (selectedInvoiceForPreview != null) {
        val inv = selectedInvoiceForPreview!!
        AlertDialog(
            onDismissRequest = { selectedInvoiceForPreview = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.invoice_item_title, inv.invoiceNumber), fontWeight = FontWeight.Bold)
                    StatusBadge(inv.status)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(stringResource(R.string.invoices_customer_label, inv.customerName), fontWeight = FontWeight.SemiBold)
                        if (inv.customerPhone.isNotBlank()) Text(stringResource(R.string.invoices_phone_label, inv.customerPhone), fontSize = 12.sp)
                        Text(stringResource(R.string.invoices_date_label, dateFormat.format(Date(inv.issueDate))), fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (isLoadingPreview) {
                        item { Text(stringResource(R.string.invoices_loading_items)) }
                    } else {
                        items(previewItems) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.quantity}x ${item.productName}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text(FinancialEngine.formatCurrency(item.total, currency), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.pos_subtotal))
                            Text(FinancialEngine.formatCurrency(inv.subtotal, currency))
                        }
                        if (inv.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.pos_discount), color = StatusDanger)
                                Text("- ${FinancialEngine.formatCurrency(inv.discount, currency)}", color = StatusDanger)
                            }
                        }
                        if (inv.tax > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.pos_tax))
                                Text(FinancialEngine.formatCurrency(inv.tax, currency))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.invoices_grand_total), fontWeight = FontWeight.Bold)
                            Text(FinancialEngine.formatCurrency(inv.total, currency), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.pos_amount_paid))
                            Text(FinancialEngine.formatCurrency(inv.paid, currency), color = StatusSuccess)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.invoices_balance_due), fontWeight = FontWeight.Bold)
                            Text(FinancialEngine.formatCurrency(inv.balance, currency), fontWeight = FontWeight.Bold, color = if (inv.balance > 0) StatusDanger else Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                val file = InvoicePdfGenerator.generateInvoicePdf(context, inv, previewItems, profile)
                                InvoicePdfGenerator.shareInvoicePdf(context, file)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.invoices_share_pdf))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                val file = InvoicePdfGenerator.generateInvoicePdf(context, inv, previewItems, profile)
                                InvoicePdfGenerator.printInvoice(context, file, "Invoice_${inv.invoiceNumber}")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.invoices_print))
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedInvoiceForPreview = null }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}
