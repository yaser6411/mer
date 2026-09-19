package com.example.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.MercuryApplication
import com.example.export.BackupManager
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = (context.applicationContext as MercuryApplication).database

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // SAF Launchers
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        BackupManager.exportFullJsonBackup(db, os)
                    }
                    statusMessage = context.getString(R.string.backup_success_json_export)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_export, e.message ?: "")
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val restoreJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BackupManager.restoreFullJsonBackup(db, inputStream)
                    }
                    statusMessage = context.getString(R.string.backup_success_json_restore)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_restore, e.message ?: "")
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    // CSV Launchers
    val exportProductsCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { BackupManager.exportProductsCsv(db, it) }
                    statusMessage = context.getString(R.string.backup_success_products_csv_export)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_export, e.message ?: "")
                }
            }
        }
    }

    val importProductsCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val count = context.contentResolver.openInputStream(uri)?.use { BackupManager.importProductsCsv(db, it) } ?: 0
                    statusMessage = context.getString(R.string.backup_success_products_import, count)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_import, e.message ?: "")
                }
            }
        }
    }

    val exportCustomersCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { BackupManager.exportCustomersCsv(db, it) }
                    statusMessage = context.getString(R.string.backup_success_customers_csv_export)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_export, e.message ?: "")
                }
            }
        }
    }

    val exportSuppliersCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { BackupManager.exportSuppliersCsv(db, it) }
                    statusMessage = context.getString(R.string.backup_success_suppliers_csv_export)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_export, e.message ?: "")
                }
            }
        }
    }

    val exportExpensesCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { BackupManager.exportExpensesCsv(db, it) }
                    statusMessage = context.getString(R.string.backup_success_expenses_csv_export)
                } catch (e: Exception) {
                    statusMessage = context.getString(R.string.backup_error_export, e.message ?: "")
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("backup_restore_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(stringResource(R.string.backup_full_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.backup_full_desc), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val timestamp = System.currentTimeMillis()
                            exportJsonLauncher.launch("mercury_backup_$timestamp.json")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.backup_export_json_btn))
                    }

                    OutlinedButton(
                        onClick = {
                            restoreJsonLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.backup_restore_json_btn))
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.backup_csv_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.backup_csv_desc), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { exportProductsCsvLauncher.launch("mercury_products.csv") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.backup_export_products), fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { importProductsCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.backup_import_products), fontSize = 12.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { exportCustomersCsvLauncher.launch("mercury_customers.csv") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.backup_export_customers), fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { exportSuppliersCsvLauncher.launch("mercury_suppliers.csv") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.backup_export_suppliers), fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { exportExpensesCsvLauncher.launch("mercury_expenses.csv") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.backup_export_expenses), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (statusMessage != null) {
        AlertDialog(
            onDismissRequest = { statusMessage = null },
            title = { Text(stringResource(R.string.backup_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(statusMessage!!) },
            confirmButton = {
                Button(onClick = { statusMessage = null }) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}
