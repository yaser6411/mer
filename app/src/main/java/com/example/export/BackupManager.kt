package com.example.export

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.MercuryDatabase
import com.example.data.local.entity.BusinessProfile
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Invoice
import com.example.data.local.entity.Payment
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.Supplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

object BackupManager {

    const val BACKUP_SCHEMA_VERSION = 1
    const val APP_VERSION = "1.0.0"

    suspend fun exportFullJsonBackup(database: MercuryDatabase, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("schemaVersion", BACKUP_SCHEMA_VERSION)
        root.put("appVersion", APP_VERSION)
        root.put("timestamp", System.currentTimeMillis())

        // Business Profile
        val profile = database.businessProfileDao().getProfileSync()
        if (profile != null) {
            val profJson = JSONObject().apply {
                put("name", profile.name)
                put("ownerName", profile.ownerName)
                put("phone", profile.phone)
                put("email", profile.email)
                put("address", profile.address)
                put("currency", profile.currency)
                put("taxNumber", profile.taxNumber)
                put("taxRate", profile.taxRate)
                put("invoicePrefix", profile.invoicePrefix)
            }
            root.put("businessProfile", profJson)
        }

        // Categories
        val categories = database.categoryDao().getAllCategories().first()
        val catArray = JSONArray()
        for (cat in categories) {
            catArray.put(JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("description", cat.description)
            })
        }
        root.put("categories", catArray)

        // Products
        val products = database.productDao().getAllProducts().first()
        val prodArray = JSONArray()
        for (prod in products) {
            prodArray.put(JSONObject().apply {
                put("id", prod.id)
                put("name", prod.name)
                put("sku", prod.sku)
                put("barcode", prod.barcode)
                put("categoryId", prod.categoryId ?: JSONObject.NULL)
                put("purchasePrice", prod.purchasePrice)
                put("sellingPrice", prod.sellingPrice)
                put("stock", prod.stock)
                put("minStock", prod.minStock)
                put("unit", prod.unit)
                put("notes", prod.notes)
                put("isActive", prod.isActive)
            })
        }
        root.put("products", prodArray)

        // Customers
        val customers = database.customerDao().getAllCustomers().first()
        val custArray = JSONArray()
        for (c in customers) {
            custArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("email", c.email)
                put("address", c.address)
                put("notes", c.notes)
                put("balance", c.balance)
            })
        }
        root.put("customers", custArray)

        // Suppliers
        val suppliers = database.supplierDao().getAllSuppliers().first()
        val suppArray = JSONArray()
        for (s in suppliers) {
            suppArray.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("phone", s.phone)
                put("email", s.email)
                put("address", s.address)
                put("notes", s.notes)
                put("balance", s.balance)
            })
        }
        root.put("suppliers", suppArray)

        // Sales
        val sales = database.saleDao().getAllSales().first()
        val salesArray = JSONArray()
        for (s in sales) {
            salesArray.put(JSONObject().apply {
                put("id", s.id)
                put("invoiceNumber", s.invoiceNumber)
                put("date", s.date)
                put("customerId", s.customerId ?: JSONObject.NULL)
                put("customerName", s.customerName)
                put("subtotal", s.subtotal)
                put("discount", s.discount)
                put("tax", s.tax)
                put("total", s.total)
                put("amountPaid", s.amountPaid)
                put("balance", s.balance)
                put("paymentStatus", s.paymentStatus)
                put("paymentMethod", s.paymentMethod)
                put("notes", s.notes)
            })
        }
        root.put("sales", salesArray)

        // Sale Items
        val saleItems = database.saleDao().getAllSaleItemsSync()
        val saleItemsArray = JSONArray()
        for (si in saleItems) {
            saleItemsArray.put(JSONObject().apply {
                put("id", si.id)
                put("saleId", si.saleId)
                put("productId", si.productId)
                put("productName", si.productName)
                put("quantity", si.quantity)
                put("unitPrice", si.unitPrice)
                put("purchasePrice", si.purchasePrice)
                put("discount", si.discount)
                put("total", si.total)
            })
        }
        root.put("saleItems", saleItemsArray)

        // Purchases
        val purchases = database.purchaseDao().getAllPurchases().first()
        val purchArray = JSONArray()
        for (p in purchases) {
            purchArray.put(JSONObject().apply {
                put("id", p.id)
                put("invoiceNumber", p.invoiceNumber)
                put("date", p.date)
                put("supplierId", p.supplierId ?: JSONObject.NULL)
                put("supplierName", p.supplierName)
                put("subtotal", p.subtotal)
                put("discount", p.discount)
                put("tax", p.tax)
                put("total", p.total)
                put("amountPaid", p.amountPaid)
                put("balance", p.balance)
                put("paymentStatus", p.paymentStatus)
                put("paymentMethod", p.paymentMethod)
                put("notes", p.notes)
            })
        }
        root.put("purchases", purchArray)

        // Purchase Items
        val purchaseItems = database.purchaseDao().getAllPurchaseItemsSync()
        val purchaseItemsArray = JSONArray()
        for (pi in purchaseItems) {
            purchaseItemsArray.put(JSONObject().apply {
                put("id", pi.id)
                put("purchaseId", pi.purchaseId)
                put("productId", pi.productId)
                put("productName", pi.productName)
                put("quantity", pi.quantity)
                put("unitCost", pi.unitCost)
                put("total", pi.total)
            })
        }
        root.put("purchaseItems", purchaseItemsArray)

        // Invoices
        val invoices = database.invoiceDao().getAllInvoicesSync()
        val invoiceArray = JSONArray()
        for (inv in invoices) {
            invoiceArray.put(JSONObject().apply {
                put("id", inv.id)
                put("saleId", inv.saleId)
                put("invoiceNumber", inv.invoiceNumber)
                put("issueDate", inv.issueDate)
                put("dueDate", inv.dueDate)
                put("customerName", inv.customerName)
                put("customerPhone", inv.customerPhone)
                put("customerAddress", inv.customerAddress)
                put("subtotal", inv.subtotal)
                put("discount", inv.discount)
                put("tax", inv.tax)
                put("total", inv.total)
                put("paid", inv.paid)
                put("balance", inv.balance)
                put("status", inv.status)
                put("notes", inv.notes)
            })
        }
        root.put("invoices", invoiceArray)

        // Payments
        val payments = database.paymentDao().getAllPaymentsSync()
        val paymentsArray = JSONArray()
        for (pm in payments) {
            paymentsArray.put(JSONObject().apply {
                put("id", pm.id)
                put("type", pm.type)
                put("relatedId", pm.relatedId)
                put("amount", pm.amount)
                put("date", pm.date)
                put("paymentMethod", pm.paymentMethod)
                put("notes", pm.notes)
            })
        }
        root.put("payments", paymentsArray)

        // Stock Movements
        val movements = database.stockMovementDao().getAllMovementsSync()
        val movementsArray = JSONArray()
        for (sm in movements) {
            movementsArray.put(JSONObject().apply {
                put("id", sm.id)
                put("productId", sm.productId)
                put("productName", sm.productName)
                put("type", sm.type)
                put("quantity", sm.quantity)
                put("previousStock", sm.previousStock)
                put("newStock", sm.newStock)
                put("referenceId", sm.referenceId ?: JSONObject.NULL)
                put("notes", sm.notes)
                put("timestamp", sm.timestamp)
            })
        }
        root.put("stockMovements", movementsArray)

        // Expenses
        val expenses = database.expenseDao().getAllExpenses().first()
        val expArray = JSONArray()
        for (e in expenses) {
            expArray.put(JSONObject().apply {
                put("id", e.id)
                put("category", e.category)
                put("amount", e.amount)
                put("date", e.date)
                put("paymentMethod", e.paymentMethod)
                put("referenceNumber", e.referenceNumber)
                put("notes", e.notes)
            })
        }
        root.put("expenses", expArray)

        outputStream.write(root.toString(2).toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    suspend fun restoreFullJsonBackup(database: MercuryDatabase, inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonString)

        val schemaVersion = root.optInt("schemaVersion", -1)
        if (schemaVersion <= 0) {
            throw IllegalArgumentException("Invalid backup file: missing or invalid schemaVersion.")
        }

        database.withTransaction {
            // Restore Profile
            if (root.has("businessProfile")) {
                val p = root.getJSONObject("businessProfile")
                val current = database.businessProfileDao().getProfileSync() ?: BusinessProfile()
                database.businessProfileDao().insertOrUpdate(
                    current.copy(
                        name = p.optString("name", current.name),
                        ownerName = p.optString("ownerName", current.ownerName),
                        phone = p.optString("phone", current.phone),
                        email = p.optString("email", current.email),
                        address = p.optString("address", current.address),
                        currency = p.optString("currency", current.currency),
                        taxNumber = p.optString("taxNumber", current.taxNumber),
                        taxRate = p.optDouble("taxRate", current.taxRate),
                        invoicePrefix = p.optString("invoicePrefix", current.invoicePrefix)
                    )
                )
            }

            // Restore Categories
            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                for (i in 0 until catArray.length()) {
                    val c = catArray.getJSONObject(i)
                    database.categoryDao().insert(
                        Category(
                            name = c.getString("name"),
                            description = c.optString("description", "")
                        )
                    )
                }
            }

            // Restore Customers
            if (root.has("customers")) {
                val custArray = root.getJSONArray("customers")
                for (i in 0 until custArray.length()) {
                    val c = custArray.getJSONObject(i)
                    database.customerDao().insert(
                        Customer(
                            name = c.getString("name"),
                            phone = c.optString("phone", ""),
                            email = c.optString("email", ""),
                            address = c.optString("address", ""),
                            notes = c.optString("notes", ""),
                            balance = c.optDouble("balance", 0.0)
                        )
                    )
                }
            }

            // Restore Suppliers
            if (root.has("suppliers")) {
                val suppArray = root.getJSONArray("suppliers")
                for (i in 0 until suppArray.length()) {
                    val s = suppArray.getJSONObject(i)
                    database.supplierDao().insert(
                        Supplier(
                            name = s.getString("name"),
                            phone = s.optString("phone", ""),
                            email = s.optString("email", ""),
                            address = s.optString("address", ""),
                            notes = s.optString("notes", ""),
                            balance = s.optDouble("balance", 0.0)
                        )
                    )
                }
            }

            // Restore Products
            if (root.has("products")) {
                val prodArray = root.getJSONArray("products")
                for (i in 0 until prodArray.length()) {
                    val p = prodArray.getJSONObject(i)
                    val sku = p.optString("sku", "")
                    val existing = if (sku.isNotBlank()) database.productDao().getProductBySku(sku) else null
                    if (existing == null) {
                        database.productDao().insert(
                            Product(
                                name = p.getString("name"),
                                sku = sku,
                                barcode = p.optString("barcode", ""),
                                categoryId = if (p.isNull("categoryId")) null else p.optLong("categoryId"),
                                purchasePrice = p.optDouble("purchasePrice", 0.0),
                                sellingPrice = p.optDouble("sellingPrice", 0.0),
                                stock = p.optDouble("stock", 0.0),
                                minStock = p.optDouble("minStock", 0.0),
                                unit = p.optString("unit", "pcs"),
                                notes = p.optString("notes", ""),
                                isActive = p.optBoolean("isActive", true)
                            )
                        )
                    }
                }
            }

            // Restore Expenses
            if (root.has("expenses")) {
                val expArray = root.getJSONArray("expenses")
                for (i in 0 until expArray.length()) {
                    val e = expArray.getJSONObject(i)
                    database.expenseDao().insert(
                        Expense(
                            category = e.getString("category"),
                            amount = e.getDouble("amount"),
                            date = e.optLong("date", System.currentTimeMillis()),
                            paymentMethod = e.optString("paymentMethod", "Cash"),
                            referenceNumber = e.optString("referenceNumber", ""),
                            notes = e.optString("notes", "")
                        )
                    )
                }
            }

            // Restore Sales
            if (root.has("sales")) {
                val salesArray = root.getJSONArray("sales")
                val restoredSales = mutableListOf<Sale>()
                for (i in 0 until salesArray.length()) {
                    val s = salesArray.getJSONObject(i)
                    restoredSales.add(
                        Sale(
                            id = s.optLong("id", 0),
                            invoiceNumber = s.getString("invoiceNumber"),
                            date = s.optLong("date", System.currentTimeMillis()),
                            customerId = if (s.isNull("customerId")) null else s.optLong("customerId"),
                            customerName = s.optString("customerName", ""),
                            subtotal = s.optDouble("subtotal", 0.0),
                            discount = s.optDouble("discount", 0.0),
                            tax = s.optDouble("tax", 0.0),
                            total = s.optDouble("total", 0.0),
                            amountPaid = s.optDouble("amountPaid", 0.0),
                            balance = s.optDouble("balance", 0.0),
                            paymentStatus = s.optString("paymentStatus", "PAID"),
                            paymentMethod = s.optString("paymentMethod", "Cash"),
                            notes = s.optString("notes", "")
                        )
                    )
                }
                if (restoredSales.isNotEmpty()) {
                    database.saleDao().insertSales(restoredSales)
                }
            }

            // Restore Sale Items
            if (root.has("saleItems")) {
                val saleItemsArray = root.getJSONArray("saleItems")
                val restoredItems = mutableListOf<SaleItem>()
                for (i in 0 until saleItemsArray.length()) {
                    val si = saleItemsArray.getJSONObject(i)
                    restoredItems.add(
                        SaleItem(
                            id = si.optLong("id", 0),
                            saleId = si.getLong("saleId"),
                            productId = si.getLong("productId"),
                            productName = si.optString("productName", ""),
                            quantity = si.optDouble("quantity", 1.0),
                            unitPrice = si.optDouble("unitPrice", 0.0),
                            purchasePrice = si.optDouble("purchasePrice", 0.0),
                            discount = si.optDouble("discount", 0.0),
                            total = si.optDouble("total", 0.0)
                        )
                    )
                }
                if (restoredItems.isNotEmpty()) {
                    database.saleDao().insertSaleItems(restoredItems)
                }
            }

            // Restore Purchases
            if (root.has("purchases")) {
                val purchArray = root.getJSONArray("purchases")
                val restoredPurchases = mutableListOf<Purchase>()
                for (i in 0 until purchArray.length()) {
                    val p = purchArray.getJSONObject(i)
                    restoredPurchases.add(
                        Purchase(
                            id = p.optLong("id", 0),
                            invoiceNumber = p.getString("invoiceNumber"),
                            date = p.optLong("date", System.currentTimeMillis()),
                            supplierId = if (p.isNull("supplierId")) null else p.optLong("supplierId"),
                            supplierName = p.optString("supplierName", ""),
                            subtotal = p.optDouble("subtotal", 0.0),
                            discount = p.optDouble("discount", 0.0),
                            tax = p.optDouble("tax", 0.0),
                            total = p.optDouble("total", 0.0),
                            amountPaid = p.optDouble("amountPaid", 0.0),
                            balance = p.optDouble("balance", 0.0),
                            paymentStatus = p.optString("paymentStatus", "PAID"),
                            paymentMethod = p.optString("paymentMethod", "Cash"),
                            notes = p.optString("notes", "")
                        )
                    )
                }
                if (restoredPurchases.isNotEmpty()) {
                    database.purchaseDao().insertPurchases(restoredPurchases)
                }
            }

            // Restore Purchase Items
            if (root.has("purchaseItems")) {
                val purchItemsArray = root.getJSONArray("purchaseItems")
                val restoredPurchItems = mutableListOf<PurchaseItem>()
                for (i in 0 until purchItemsArray.length()) {
                    val pi = purchItemsArray.getJSONObject(i)
                    restoredPurchItems.add(
                        PurchaseItem(
                            id = pi.optLong("id", 0),
                            purchaseId = pi.getLong("purchaseId"),
                            productId = pi.getLong("productId"),
                            productName = pi.optString("productName", ""),
                            quantity = pi.optDouble("quantity", 1.0),
                            unitCost = pi.optDouble("unitCost", 0.0),
                            total = pi.optDouble("total", 0.0)
                        )
                    )
                }
                if (restoredPurchItems.isNotEmpty()) {
                    database.purchaseDao().insertPurchaseItems(restoredPurchItems)
                }
            }

            // Restore Invoices
            if (root.has("invoices")) {
                val invoiceArray = root.getJSONArray("invoices")
                val restoredInvoices = mutableListOf<Invoice>()
                for (i in 0 until invoiceArray.length()) {
                    val inv = invoiceArray.getJSONObject(i)
                    restoredInvoices.add(
                        Invoice(
                            id = inv.optLong("id", 0),
                            saleId = inv.optLong("saleId", 0),
                            invoiceNumber = inv.getString("invoiceNumber"),
                            issueDate = inv.optLong("issueDate", System.currentTimeMillis()),
                            dueDate = inv.optLong("dueDate", System.currentTimeMillis()),
                            customerName = inv.optString("customerName", ""),
                            customerPhone = inv.optString("customerPhone", ""),
                            customerAddress = inv.optString("customerAddress", ""),
                            subtotal = inv.optDouble("subtotal", 0.0),
                            discount = inv.optDouble("discount", 0.0),
                            tax = inv.optDouble("tax", 0.0),
                            total = inv.optDouble("total", 0.0),
                            paid = inv.optDouble("paid", 0.0),
                            balance = inv.optDouble("balance", 0.0),
                            status = inv.optString("status", "PAID"),
                            notes = inv.optString("notes", "")
                        )
                    )
                }
                if (restoredInvoices.isNotEmpty()) {
                    database.invoiceDao().insertInvoices(restoredInvoices)
                }
            }

            // Restore Payments
            if (root.has("payments")) {
                val paymentsArray = root.getJSONArray("payments")
                val restoredPayments = mutableListOf<Payment>()
                for (i in 0 until paymentsArray.length()) {
                    val pm = paymentsArray.getJSONObject(i)
                    restoredPayments.add(
                        Payment(
                            id = pm.optLong("id", 0),
                            type = pm.getString("type"),
                            relatedId = pm.getLong("relatedId"),
                            amount = pm.getDouble("amount"),
                            date = pm.optLong("date", System.currentTimeMillis()),
                            paymentMethod = pm.optString("paymentMethod", "Cash"),
                            notes = pm.optString("notes", "")
                        )
                    )
                }
                if (restoredPayments.isNotEmpty()) {
                    database.paymentDao().insertPayments(restoredPayments)
                }
            }

            // Restore Stock Movements
            if (root.has("stockMovements")) {
                val movArray = root.getJSONArray("stockMovements")
                val restoredMovements = mutableListOf<StockMovement>()
                for (i in 0 until movArray.length()) {
                    val sm = movArray.getJSONObject(i)
                    restoredMovements.add(
                        StockMovement(
                            id = sm.optLong("id", 0),
                            productId = sm.getLong("productId"),
                            productName = sm.optString("productName", ""),
                            type = sm.getString("type"),
                            quantity = sm.getDouble("quantity"),
                            previousStock = sm.getDouble("previousStock"),
                            newStock = sm.getDouble("newStock"),
                            referenceId = if (sm.isNull("referenceId")) null else sm.optLong("referenceId"),
                            notes = sm.optString("notes", ""),
                            timestamp = sm.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (restoredMovements.isNotEmpty()) {
                    database.stockMovementDao().insertMovements(restoredMovements)
                }
            }
        }
        true
    }

    // CSV Exports
    suspend fun exportProductsCsv(database: MercuryDatabase, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val products = database.productDao().getAllProducts().first()
        val writer = outputStream.bufferedWriter()
        writer.appendLine("Name,SKU,Barcode,PurchasePrice,SellingPrice,Stock,MinStock,Unit,Notes,IsActive")
        for (p in products) {
            writer.appendLine(
                "\"${escapeCsv(p.name)}\",\"${escapeCsv(p.sku)}\",\"${escapeCsv(p.barcode)}\",${p.purchasePrice},${p.sellingPrice},${p.stock},${p.minStock},\"${escapeCsv(p.unit)}\",\"${escapeCsv(p.notes)}\",${p.isActive}"
            )
        }
        writer.flush()
    }

    suspend fun exportCustomersCsv(database: MercuryDatabase, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val customers = database.customerDao().getAllCustomers().first()
        val writer = outputStream.bufferedWriter()
        writer.appendLine("Name,Phone,Email,Address,Balance,Notes")
        for (c in customers) {
            writer.appendLine(
                "\"${escapeCsv(c.name)}\",\"${escapeCsv(c.phone)}\",\"${escapeCsv(c.email)}\",\"${escapeCsv(c.address)}\",${c.balance},\"${escapeCsv(c.notes)}\""
            )
        }
        writer.flush()
    }

    suspend fun exportSuppliersCsv(database: MercuryDatabase, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val suppliers = database.supplierDao().getAllSuppliers().first()
        val writer = outputStream.bufferedWriter()
        writer.appendLine("Name,Phone,Email,Address,Balance,Notes")
        for (s in suppliers) {
            writer.appendLine(
                "\"${escapeCsv(s.name)}\",\"${escapeCsv(s.phone)}\",\"${escapeCsv(s.email)}\",\"${escapeCsv(s.address)}\",${s.balance},\"${escapeCsv(s.notes)}\""
            )
        }
        writer.flush()
    }

    suspend fun exportExpensesCsv(database: MercuryDatabase, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val expenses = database.expenseDao().getAllExpenses().first()
        val writer = outputStream.bufferedWriter()
        writer.appendLine("Category,Amount,Date,PaymentMethod,ReferenceNumber,Notes")
        for (e in expenses) {
            writer.appendLine(
                "\"${escapeCsv(e.category)}\",${e.amount},${e.date},\"${escapeCsv(e.paymentMethod)}\",\"${escapeCsv(e.referenceNumber)}\",\"${escapeCsv(e.notes)}\""
            )
        }
        writer.flush()
    }

    // CSV Imports
    suspend fun importProductsCsv(database: MercuryDatabase, inputStream: InputStream): Int = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var count = 0
        val lines = reader.readLines()
        if (lines.isEmpty()) return@withContext 0

        database.withTransaction {
            for (line in lines.drop(1)) {
                if (line.isBlank()) continue
                val parts = parseCsvLine(line)
                if (parts.isNotEmpty()) {
                    val name = parts.getOrNull(0) ?: continue
                    if (name.isBlank()) continue
                    val sku = parts.getOrNull(1) ?: ""
                    val barcode = parts.getOrNull(2) ?: ""
                    val purchasePrice = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                    val sellingPrice = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0
                    val stock = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                    val minStock = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0
                    val unit = parts.getOrNull(7)?.ifBlank { "pcs" } ?: "pcs"
                    val notes = parts.getOrNull(8) ?: ""
                    val isActive = parts.getOrNull(9)?.toBooleanStrictOrNull() ?: true

                    val existingSku = if (sku.isNotBlank()) database.productDao().getProductBySku(sku) else null
                    if (existingSku == null) {
                        database.productDao().insert(
                            Product(
                                name = name,
                                sku = sku,
                                barcode = barcode,
                                purchasePrice = purchasePrice,
                                sellingPrice = sellingPrice,
                                stock = stock,
                                minStock = minStock,
                                unit = unit,
                                notes = notes,
                                isActive = isActive
                            )
                        )
                        count++
                    }
                }
            }
        }
        count
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ")
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
