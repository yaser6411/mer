package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profiles WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: BusinessProfile)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<Category>

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSync(id: Long): Product?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE stock <= minStock AND isActive = 1 ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE stock <= minStock AND isActive = 1")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE stock <= 0 AND isActive = 1")
    fun getOutOfStockCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(stock * purchasePrice), 0.0) FROM products WHERE isActive = 1")
    fun getTotalInventoryCostValue(): Flow<Double>

    @Query("SELECT COALESCE(SUM(stock * sellingPrice), 0.0) FROM products WHERE isActive = 1")
    fun getTotalInventoryRetailValue(): Flow<Double>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getActiveProductCount(): Flow<Int>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsSync(): List<Product>

    @Update
    suspend fun update(product: Product)

    @Query("UPDATE products SET stock = :newStock, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double, updatedDate: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(product: Product)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerByIdSync(id: Long): Customer?

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM customers")
    fun getTotalReceivables(): Flow<Double>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomersSync(): List<Customer>

    @Update
    suspend fun update(customer: Customer)

    @Query("UPDATE customers SET balance = balance + :delta, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double, updatedDate: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    fun getSupplierById(id: Long): Flow<Supplier?>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierByIdSync(id: Long): Supplier?

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM suppliers")
    fun getTotalPayables(): Flow<Double>

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSuppliers(query: String): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<Supplier>)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    suspend fun getAllSuppliersSync(): List<Supplier>

    @Update
    suspend fun update(supplier: Supplier)

    @Query("UPDATE suppliers SET balance = balance + :delta, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double, updatedDate: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(supplier: Supplier)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    fun getSaleById(id: Long): Flow<Sale?>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleByIdSync(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getSalesBetweenDates(startTime: Long, endTime: Long): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales WHERE date >= :startTime AND date <= :endTime")
    fun getSalesTotalBetweenDates(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales WHERE date >= :startTime AND date <= :endTime")
    suspend fun getSalesTotalBetweenDatesSync(startTime: Long, endTime: Long): Double

    @Query("SELECT COUNT(*) FROM sales WHERE date >= :startTime AND date <= :endTime")
    fun getSalesCountBetweenDates(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsSync(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItemsSync(): List<SaleItem>

    @Query("SELECT * FROM sales ORDER BY date DESC")
    suspend fun getAllSalesSync(): List<Sale>

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getTotalSalesCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    fun getPurchaseById(id: Long): Flow<Purchase?>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseByIdSync(id: Long): Purchase?

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getPurchasesBetweenDates(startTime: Long, endTime: Long): Flow<List<Purchase>>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM purchases WHERE date >= :startTime AND date <= :endTime")
    fun getPurchasesTotalBetweenDates(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM purchases WHERE date >= :startTime AND date <= :endTime")
    suspend fun getPurchasesTotalBetweenDatesSync(startTime: Long, endTime: Long): Double

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItem>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItemsSync(purchaseId: Long): List<PurchaseItem>

    @Query("SELECT * FROM purchases ORDER BY date DESC")
    suspend fun getAllPurchasesSync(): List<Purchase>

    @Query("SELECT * FROM purchase_items")
    suspend fun getAllPurchaseItemsSync(): List<PurchaseItem>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<Purchase>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Delete
    suspend fun deletePurchase(purchase: Purchase)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseById(id: Long): Flow<Expense?>

    @Query("SELECT * FROM expenses WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getExpensesBetweenDates(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date >= :startTime AND date <= :endTime")
    fun getExpensesTotalBetweenDates(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date >= :startTime AND date <= :endTime")
    suspend fun getExpensesTotalBetweenDatesSync(startTime: Long, endTime: Long): Double

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date >= :startTime AND date <= :endTime GROUP BY category")
    fun getExpensesGroupedByCategory(startTime: Long, endTime: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesSync(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE type = :type AND relatedId = :relatedId ORDER BY date DESC")
    fun getPaymentsForRelated(type: String, relatedId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)

    @Query("SELECT * FROM payments ORDER BY date DESC")
    suspend fun getAllPaymentsSync(): List<Payment>

    @Delete
    suspend fun delete(payment: Payment)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    fun getInvoiceById(id: Long): Flow<Invoice?>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceByIdSync(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE saleId = :saleId LIMIT 1")
    fun getInvoiceBySaleId(saleId: Long): Flow<Invoice?>

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<Invoice>)

    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    suspend fun getAllInvoicesSync(): List<Invoice>

    @Update
    suspend fun update(invoice: Invoice)

    @Delete
    suspend fun delete(invoice: Invoice)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovement>)

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    suspend fun getAllMovementsSync(): List<StockMovement>
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingSync(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSetting>>
}
