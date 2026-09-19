package com.example.ui.screens.expenses

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoneyOff
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
import com.example.data.local.entity.Expense
import com.example.domain.FinancialEngine
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.StatusDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun getExpenseCategoryLabel(cat: String): String {
    return when (cat.lowercase()) {
        "all" -> stringResource(R.string.expense_cat_all)
        "rent" -> stringResource(R.string.expense_cat_rent)
        "utilities" -> stringResource(R.string.expense_cat_utilities)
        "salaries" -> stringResource(R.string.expense_cat_salaries)
        "supplies" -> stringResource(R.string.expense_cat_supplies)
        "maintenance" -> stringResource(R.string.expense_cat_maintenance)
        "shipping" -> stringResource(R.string.expense_cat_shipping)
        "marketing" -> stringResource(R.string.expense_cat_marketing)
        "other" -> stringResource(R.string.expense_cat_other)
        else -> cat
    }
}

@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    currency: String,
    onSaveExpense: (Expense, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val predefinedCategories = listOf("All", "Rent", "Utilities", "Salaries", "Supplies", "Maintenance", "Shipping", "Marketing", "Other")
    var selectedCategory by remember { mutableStateOf("All") }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val filteredExpenses = remember(expenses, selectedCategory) {
        if (selectedCategory == "All") expenses
        else expenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalExpenseAmount = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Box(modifier = modifier.fillMaxSize().testTag("expenses_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.expenses_total_recorded), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FinancialEngine.formatCurrency(totalExpenseAmount, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StatusDanger
                        )
                    }
                    Text(stringResource(R.string.expenses_items_count, filteredExpenses.size), fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(predefinedCategories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(getExpenseCategoryLabel(cat)) }
                    )
                }
            }

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MoneyOff,
                    title = stringResource(R.string.expenses_empty_title),
                    description = if (expenses.isEmpty()) stringResource(R.string.expenses_empty_desc) else stringResource(R.string.expenses_no_category_desc, getExpenseCategoryLabel(selectedCategory)),
                    actionButtonText = if (expenses.isEmpty()) stringResource(R.string.expense_add_title) else null,
                    onAction = {
                        expenseToEdit = null
                        showDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("expense_card_${expense.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(getExpenseCategoryLabel(expense.category), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        FinancialEngine.formatCurrency(expense.amount, currency),
                                        fontWeight = FontWeight.Bold,
                                        color = StatusDanger,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.expenses_payment_method, expense.paymentMethod), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dateFormat.format(Date(expense.date)), fontSize = 12.sp, color = Color.Gray)
                                }
                                if (expense.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(expense.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            expenseToEdit = expense
                                            showDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { expenseToDelete = expense },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                expenseToEdit = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_expense_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.expense_add_title), tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    if (showDialog) {
        var category by remember { mutableStateOf(expenseToEdit?.category ?: "Utilities") }
        var amountText by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
        var method by remember { mutableStateOf(expenseToEdit?.paymentMethod ?: "Cash") }
        var refNum by remember { mutableStateOf(expenseToEdit?.referenceNumber ?: "") }
        var notes by remember { mutableStateOf(expenseToEdit?.notes ?: "") }
        var err by remember { mutableStateOf<String?>(null) }
        val amountErrorMsg = stringResource(R.string.expense_amount_error)

        val commonCats = listOf("Rent", "Utilities", "Salaries", "Supplies", "Maintenance", "Shipping", "Marketing", "Other")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (expenseToEdit == null) stringResource(R.string.expense_add_title) else stringResource(R.string.expense_edit_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (err != null) Text(err!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                    Text(stringResource(R.string.expenses_category_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(commonCats) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(getExpenseCategoryLabel(cat)) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(stringResource(R.string.expense_amount_required, currency)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = refNum,
                        onValueChange = { refNum = it },
                        label = { Text(stringResource(R.string.expense_ref_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.expense_notes_desc)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            err = amountErrorMsg
                            return@Button
                        }
                        val result = (expenseToEdit ?: Expense(category = category, amount = amount)).copy(
                            category = category,
                            amount = amount,
                            paymentMethod = method,
                            referenceNumber = refNum.trim(),
                            notes = notes.trim()
                        )
                        onSaveExpense(result, { showDialog = false }, {})
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (expenseToDelete != null) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.expense_delete_title),
            message = stringResource(R.string.expense_delete_message, FinancialEngine.formatCurrency(expenseToDelete!!.amount, currency)),
            onConfirm = {
                onDeleteExpense(expenseToDelete!!)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}
