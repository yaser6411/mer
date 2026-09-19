package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.MercuryDatabase
import com.example.data.local.dao.CategoryTotal
import com.example.data.local.entity.AppSetting
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MercuryRepository(private val database: MercuryDatabase) {

    private val profileDao = database.businessProfileDao()
    private val categoryDao = database.categoryDao()
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val supplierDao = database.supplierDao()
    private val saleDao = database.saleDao()
    private val purchaseDao = database.purchaseDao()
    private val expenseDao = database.expenseDao()
    private val paymentDao = database.paymentDao()
    private val invoiceDao = database.invoiceDao()
    private val stockMovementDao = database.stockMovementDao()
    private val appSettingDao = database.appSettingDao()

    // Business Profile
    val businessProfile: Flow<BusinessProfile?> = profileDao.getProfile()

    suspend fun getBusinessProfileSync(): BusinessProfile? = withContext(Dispatchers.IO) {
        profileDao.getProfileSync()
    }

    suspend fun updateBusinessProfile(profile: BusinessProfile) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdate(profile)
    }

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        categoryDao.insert(category)
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.delete(category)
    }

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val activeProducts: Flow<List<Product>> = productDao.getAllActiveProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val outOfStockCount: Flow<Int> = productDao.getOutOfStockCount()
    val totalInventoryCostValue: Flow<Double> = productDao.getTotalInventoryCostValue()
    val totalInventoryRetailValue: Flow<Double> = productDao.getTotalInventoryRetailValue()
    val totalProductCount: Flow<Int> = productDao.getActiveProductCount()

    fun getProductById(id: Long): Flow<Product?> = productDao.getProductById(id)
    suspend fun getProductByIdSync(id: Long): Product? = withContext(Dispatchers.IO) {
        productDao.getProductByIdSync(id)
    }

    suspend fun getProductBySku(sku: String): Product? = withContext(Dispatchers.IO) {
        productDao.getProductBySku(sku)
    }

    suspend fun getProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        productDao.getProductByBarcode(barcode)
    }

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun insertProduct(product: Product, initialStockReason: String = "Initial Inventory"): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingSku = if (product.sku.isNotBlank()) productDao.getProductBySku(product.sku) else null
            if (existingSku != null) {
                throw IllegalArgumentException("A product with SKU '${product.sku}' already exists.")
            }
            val id = productDao.insert(product)
            if (product.stock > 0.0) {
                stockMovementDao.insert(
                    StockMovement(
                        productId = id,
                        productName = product.name,
                        type = "INITIAL",
                        quantity = product.stock,
                        previousStock = 0.0,
                        newStock = product.stock,
                        notes = initialStockReason
                    )
                )
            }
            id
        }
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existing = productDao.getProductByIdSync(product.id)
                ?: throw NoSuchElementException("Product not found")
            if (product.sku.isNotBlank() && product.sku != existing.sku) {
                val skuCheck = productDao.getProductBySku(product.sku)
                if (skuCheck != null && skuCheck.id != product.id) {
                    throw IllegalArgumentException("A product with SKU '${product.sku}' already exists.")
                }
            }
            productDao.update(product)
        }
    }

    suspend fun adjustStock(productId: Long, newStock: Double, reason: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val product = productDao.getProductByIdSync(productId)
                ?: throw NoSuchElementException("Product not found")
            val allowNegative = appSettingDao.getSettingSync("allow_negative_stock") == "true"
            if (!allowNegative && newStock < 0.0) {
                throw IllegalArgumentException("Negative stock is not allowed by system policy.")
            }
            val delta = newStock - product.stock
            productDao.updateStock(productId, newStock)
            stockMovementDao.insert(
                StockMovement(
                    productId = productId,
                    productName = product.name,
                    type = "ADJUSTMENT",
                    quantity = delta,
                    previousStock = product.stock,
                    newStock = newStock,
                    notes = reason.ifBlank { "Stock Adjustment" }
                )
            )
        }
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.delete(product)
    }

    // Customers
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val totalReceivables: Flow<Double> = customerDao.getTotalReceivables()

    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)
    suspend fun getCustomerByIdSync(id: Long): Customer? = withContext(Dispatchers.IO) {
        customerDao.getCustomerByIdSync(id)
    }

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun insertCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        customerDao.insert(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.update(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        customerDao.delete(customer)
    }

    // Suppliers
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    val totalPayables: Flow<Double> = supplierDao.getTotalPayables()

    fun getSupplierById(id: Long): Flow<Supplier?> = supplierDao.getSupplierById(id)
    suspend fun getSupplierByIdSync(id: Long): Supplier? = withContext(Dispatchers.IO) {
        supplierDao.getSupplierByIdSync(id)
    }

    fun searchSuppliers(query: String): Flow<List<Supplier>> = supplierDao.searchSuppliers(query)

    suspend fun insertSupplier(supplier: Supplier): Long = withContext(Dispatchers.IO) {
        supplierDao.insert(supplier)
    }

    suspend fun updateSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        supplierDao.update(supplier)
    }

    suspend fun deleteSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        supplierDao.delete(supplier)
    }

    // Sales & Atomic Sale Transaction
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()

    fun getSaleById(id: Long): Flow<Sale?> = saleDao.getSaleById(id)
    suspend fun getSaleByIdSync(id: Long): Sale? = withContext(Dispatchers.IO) {
        saleDao.getSaleByIdSync(id)
    }

    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> = saleDao.getSaleItems(saleId)
    suspend fun getSaleItemsSync(saleId: Long): List<SaleItem> = withContext(Dispatchers.IO) {
        saleDao.getSaleItemsSync(saleId)
    }

    fun getSalesBetweenDates(start: Long, end: Long): Flow<List<Sale>> = saleDao.getSalesBetweenDates(start, end)
    fun getSalesTotalBetweenDates(start: Long, end: Long): Flow<Double> = saleDao.getSalesTotalBetweenDates(start, end)
    fun getSalesCountBetweenDates(start: Long, end: Long): Flow<Int> = saleDao.getSalesCountBetweenDates(start, end)

    suspend fun executeSaleTransaction(
        sale: Sale,
        items: List<SaleItem>,
        paymentAmount: Double,
        paymentMethod: String,
        customerId: Long?
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (items.isEmpty()) {
                throw IllegalArgumentException("Sale must contain at least one item.")
            }

            val allowNegative = appSettingDao.getSettingSync("allow_negative_stock") == "true"

            // 1. Validate all items and stock
            for (item in items) {
                val product = productDao.getProductByIdSync(item.productId)
                    ?: throw NoSuchElementException("Product '${item.productName}' not found.")
                if (!allowNegative && product.stock < item.quantity) {
                    throw IllegalStateException(
                        "Insufficient stock for '${product.name}'. Available: ${product.stock}, Required: ${item.quantity}"
                    )
                }
            }

            // 2. Insert Sale
            val saleId = saleDao.insertSale(sale)

            // 3. Attach saleId to items and insert
            val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
            saleDao.insertSaleItems(itemsWithSaleId)

            // 4. Decrease stock and record stock movements
            for (item in itemsWithSaleId) {
                val product = productDao.getProductByIdSync(item.productId)!!
                val newStock = product.stock - item.quantity
                productDao.updateStock(product.id, newStock)
                stockMovementDao.insert(
                    StockMovement(
                        productId = product.id,
                        productName = product.name,
                        type = "SALE",
                        quantity = -item.quantity,
                        previousStock = product.stock,
                        newStock = newStock,
                        referenceId = saleId,
                        notes = "Sale #${sale.invoiceNumber}"
                    )
                )
            }

            // 5. If payment made, record payment
            if (paymentAmount > 0.0) {
                paymentDao.insert(
                    Payment(
                        type = "SALE_PAYMENT",
                        relatedId = saleId,
                        amount = paymentAmount,
                        date = sale.date,
                        paymentMethod = paymentMethod,
                        notes = "Payment for sale #${sale.invoiceNumber}"
                    )
                )
            }

            // 6. Update customer balance with unpaid balance
            if (customerId != null && sale.balance > 0.0) {
                customerDao.updateBalance(customerId, sale.balance)
            }

            // 7. Create invoice record
            invoiceDao.insert(
                Invoice(
                    saleId = saleId,
                    invoiceNumber = sale.invoiceNumber,
                    issueDate = sale.date,
                    dueDate = sale.date,
                    customerName = sale.customerName,
                    subtotal = sale.subtotal,
                    discount = sale.discount,
                    tax = sale.tax,
                    total = sale.total,
                    paid = sale.amountPaid,
                    balance = sale.balance,
                    status = sale.paymentStatus,
                    notes = sale.notes
                )
            )

            saleId
        }
    }

    // Purchases & Atomic Purchase Transaction
    val allPurchases: Flow<List<Purchase>> = purchaseDao.getAllPurchases()

    fun getPurchaseById(id: Long): Flow<Purchase?> = purchaseDao.getPurchaseById(id)
    suspend fun getPurchaseByIdSync(id: Long): Purchase? = withContext(Dispatchers.IO) {
        purchaseDao.getPurchaseByIdSync(id)
    }

    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItem>> = purchaseDao.getPurchaseItems(purchaseId)
    suspend fun getPurchaseItemsSync(purchaseId: Long): List<PurchaseItem> = withContext(Dispatchers.IO) {
        purchaseDao.getPurchaseItemsSync(purchaseId)
    }

    fun getPurchasesBetweenDates(start: Long, end: Long): Flow<List<Purchase>> = purchaseDao.getPurchasesBetweenDates(start, end)
    fun getPurchasesTotalBetweenDates(start: Long, end: Long): Flow<Double> = purchaseDao.getPurchasesTotalBetweenDates(start, end)

    suspend fun executePurchaseTransaction(
        purchase: Purchase,
        items: List<PurchaseItem>,
        paymentAmount: Double,
        paymentMethod: String,
        supplierId: Long?
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (items.isEmpty()) {
                throw IllegalArgumentException("Purchase must contain at least one item.")
            }

            // 1. Insert Purchase
            val purchaseId = purchaseDao.insertPurchase(purchase)

            // 2. Attach purchaseId to items and insert
            val itemsWithPurchaseId = items.map { it.copy(purchaseId = purchaseId) }
            purchaseDao.insertPurchaseItems(itemsWithPurchaseId)

            // 3. Increase stock and record stock movements
            for (item in itemsWithPurchaseId) {
                val product = productDao.getProductByIdSync(item.productId)
                if (product != null) {
                    val newStock = product.stock + item.quantity
                    // Optionally update purchase price to reflect latest cost
                    val updatedProduct = product.copy(
                        stock = newStock,
                        purchasePrice = if (item.unitCost > 0.0) item.unitCost else product.purchasePrice,
                        updatedDate = System.currentTimeMillis()
                    )
                    productDao.update(updatedProduct)
                    stockMovementDao.insert(
                        StockMovement(
                            productId = product.id,
                            productName = product.name,
                            type = "PURCHASE",
                            quantity = item.quantity,
                            previousStock = product.stock,
                            newStock = newStock,
                            referenceId = purchaseId,
                            notes = "Purchase #${purchase.invoiceNumber}"
                        )
                    )
                }
            }

            // 4. Record Payment if paid
            if (paymentAmount > 0.0) {
                paymentDao.insert(
                    Payment(
                        type = "PURCHASE_PAYMENT",
                        relatedId = purchaseId,
                        amount = paymentAmount,
                        date = purchase.date,
                        paymentMethod = paymentMethod,
                        notes = "Payment for purchase #${purchase.invoiceNumber}"
                    )
                )
            }

            // 5. Update supplier balance with unpaid balance
            if (supplierId != null && purchase.balance > 0.0) {
                supplierDao.updateBalance(supplierId, purchase.balance)
            }

            purchaseId
        }
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesBetweenDates(start: Long, end: Long): Flow<List<Expense>> = expenseDao.getExpensesBetweenDates(start, end)
    fun getExpensesTotalBetweenDates(start: Long, end: Long): Flow<Double> = expenseDao.getExpensesTotalBetweenDates(start, end)
    fun getExpensesGroupedByCategory(start: Long, end: Long): Flow<List<CategoryTotal>> = expenseDao.getExpensesGroupedByCategory(start, end)

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        expenseDao.insert(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.update(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.delete(expense)
    }

    // Invoices
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    fun getInvoiceById(id: Long): Flow<Invoice?> = invoiceDao.getInvoiceById(id)
    suspend fun getInvoiceByIdSync(id: Long): Invoice? = withContext(Dispatchers.IO) {
        invoiceDao.getInvoiceByIdSync(id)
    }

    fun getInvoiceBySaleId(saleId: Long): Flow<Invoice?> = invoiceDao.getInvoiceBySaleId(saleId)

    // Stock Movements
    val allStockMovements: Flow<List<StockMovement>> = stockMovementDao.getAllMovements()
    fun getStockMovementsForProduct(productId: Long): Flow<List<StockMovement>> = stockMovementDao.getMovementsForProduct(productId)

    // App Settings
    fun getSetting(key: String): Flow<String?> = appSettingDao.getSetting(key)
    suspend fun getSettingSync(key: String): String? = withContext(Dispatchers.IO) {
        appSettingDao.getSettingSync(key)
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        appSettingDao.setSetting(AppSetting(key, value))
    }

    // Synchronous aggregates for reporting
    suspend fun getSalesTotalSync(start: Long, end: Long): Double = withContext(Dispatchers.IO) {
        saleDao.getSalesTotalBetweenDatesSync(start, end)
    }

    suspend fun getPurchasesTotalSync(start: Long, end: Long): Double = withContext(Dispatchers.IO) {
        purchaseDao.getPurchasesTotalBetweenDatesSync(start, end)
    }

    suspend fun getExpensesTotalSync(start: Long, end: Long): Double = withContext(Dispatchers.IO) {
        expenseDao.getExpensesTotalBetweenDatesSync(start, end)
    }

    suspend fun getAllSaleItemsSync(): List<SaleItem> = withContext(Dispatchers.IO) {
        saleDao.getAllSaleItemsSync()
    }

    // For generating invoice number
    suspend fun generateNextInvoiceNumber(): String = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfileSync()
        val prefix = profile?.invoicePrefix?.ifBlank { "INV-" } ?: "INV-"
        val count = saleDao.getTotalSalesCount() + 1
        String.format("%s%05d", prefix, count)
    }
}
