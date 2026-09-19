package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MercuryApplication
import com.example.data.local.entity.BusinessProfile
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Invoice
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.Supplier
import com.example.domain.FinancialEngine
import com.example.domain.model.UiState
import com.example.domain.usecase.GetDashboardSummaryUseCase
import com.example.domain.usecase.ProcessPurchaseUseCase
import com.example.domain.usecase.ProcessSaleUseCase
import com.example.util.AppLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val unitPrice: Double = product.sellingPrice,
    val discount: Double = 0.0
) {
    val total: Double get() = FinancialEngine.calculateItemTotal(quantity, unitPrice, discount)
}

data class PurchaseCartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val unitCost: Double = product.purchasePrice
) {
    val total: Double get() = FinancialEngine.roundTwoDecimals(quantity * unitCost)
}

data class DashboardSummary(
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayProfit: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val monthlyProfit: Double = 0.0,
    val inventoryValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val customerBalances: Double = 0.0,
    val supplierBalances: Double = 0.0
)

enum class ScreenDestination {
    DASHBOARD,
    PRODUCTS,
    SALES_POS,
    SALES_HISTORY,
    PURCHASES,
    CUSTOMERS,
    SUPPLIERS,
    EXPENSES,
    INVOICES,
    REPORTS,
    BACKUP_RESTORE,
    SETTINGS
}

class MercuryViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as MercuryApplication).container
    private val repository = container.repository
    private val localePreferencesManager = container.localePreferencesManager
    private val getDashboardSummaryUseCase = container.getDashboardSummaryUseCase
    private val processSaleUseCase = container.processSaleUseCase
    private val processPurchaseUseCase = container.processPurchaseUseCase

    // Current Screen Navigation
    private val _currentScreen = MutableStateFlow(ScreenDestination.DASHBOARD)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    // Feedback Notifications
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Dashboard Executive KPIs via Domain Use Case (Sealed UI State)
    val dashboardSummaryState: StateFlow<UiState<DashboardSummary>> = getDashboardSummaryUseCase()
        .map { summary ->
            UiState.Success(summary) as UiState<DashboardSummary>
        }
        .catch { e ->
            AppLogger.e("Error loading dashboard summary", e)
            emit(UiState.Error(e.message ?: "Failed to load dashboard metrics", e))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val dashboardSummary: StateFlow<DashboardSummary> = getDashboardSummaryUseCase()
        .catch { e ->
            AppLogger.e("Error emitting dashboard summary", e)
            emit(DashboardSummary())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Business Profile
    val businessProfile: StateFlow<BusinessProfile?> = repository.businessProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Products & Inventory
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockCount: StateFlow<Int> = repository.lowStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount: StateFlow<Int> = repository.outOfStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalInventoryCostValue: StateFlow<Double> = repository.totalInventoryCostValue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalInventoryRetailValue: StateFlow<Double> = repository.totalInventoryRetailValue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalProductCount: StateFlow<Int> = repository.totalProductCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Categories
    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customers
    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReceivables: StateFlow<Double> = repository.totalReceivables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Suppliers
    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPayables: StateFlow<Double> = repository.totalPayables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Sales & Invoices
    val allSales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Purchases
    val allPurchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses
    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock Movements
    val allStockMovements: StateFlow<List<StockMovement>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    val allowNegativeStock: StateFlow<Boolean> = repository.getSetting("allow_negative_stock")
        .map { it == "true" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = repository.getSetting("theme_mode")
        .map { it ?: "SYSTEM" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val appLanguage: StateFlow<String> = localePreferencesManager.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _cartDiscount = MutableStateFlow(0.0)
    val cartDiscount: StateFlow<Double> = _cartDiscount.asStateFlow()

    private val _cartTaxRate = MutableStateFlow(0.0)
    val cartTaxRate: StateFlow<Double> = _cartTaxRate.asStateFlow()

    private val _cartNotes = MutableStateFlow("")
    val cartNotes: StateFlow<String> = _cartNotes.asStateFlow()

    // Purchase Cart State
    private val _purchaseCartItems = MutableStateFlow<List<PurchaseCartItem>>(emptyList())
    val purchaseCartItems: StateFlow<List<PurchaseCartItem>> = _purchaseCartItems.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<Supplier?>(null)
    val selectedSupplier: StateFlow<Supplier?> = _selectedSupplier.asStateFlow()

    init {
        // Sync default tax rate from business profile if set
        viewModelScope.launch {
            repository.businessProfile.collect { profile ->
                if (profile != null && _cartTaxRate.value == 0.0 && profile.taxRate > 0.0) {
                    _cartTaxRate.value = profile.taxRate
                }
            }
        }
    }

    // POS Actions
    fun addToCart(product: Product, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: Long, quantity: Double) {
        if (quantity <= 0.0) {
            removeFromCart(productId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = quantity)
            _cartItems.value = current
        }
    }

    fun updateCartItemDiscount(productId: Long, discount: Double) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(discount = maxOf(0.0, discount))
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _cartDiscount.value = 0.0
        _cartNotes.value = ""
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setCartDiscount(discount: Double) {
        _cartDiscount.value = maxOf(0.0, discount)
    }

    fun setCartTaxRate(rate: Double) {
        _cartTaxRate.value = maxOf(0.0, rate)
    }

    fun setCartNotes(notes: String) {
        _cartNotes.value = notes
    }

    fun completeSale(
        amountPaid: Double,
        paymentMethod: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val items = _cartItems.value
                if (items.isEmpty()) {
                    onError("Cart is empty.")
                    return@launch
                }

                val subtotal = FinancialEngine.calculateSubtotal(items.map { it.total })
                val discount = _cartDiscount.value
                val tax = FinancialEngine.calculateTax(maxOf(0.0, subtotal - discount), _cartTaxRate.value)
                val total = FinancialEngine.calculateTotal(subtotal, discount, tax)
                val balance = FinancialEngine.calculateBalance(total, amountPaid)
                val paymentStatus = FinancialEngine.determinePaymentStatus(total, amountPaid).name
                val invoiceNumber = repository.generateNextInvoiceNumber()

                val customer = _selectedCustomer.value
                val sale = Sale(
                    invoiceNumber = invoiceNumber,
                    date = System.currentTimeMillis(),
                    customerId = customer?.id,
                    customerName = customer?.name ?: "Walk-in Customer",
                    subtotal = subtotal,
                    discount = discount,
                    tax = tax,
                    total = total,
                    amountPaid = amountPaid,
                    balance = balance,
                    paymentStatus = paymentStatus,
                    paymentMethod = paymentMethod,
                    notes = _cartNotes.value
                )

                val saleItems = items.map {
                    SaleItem(
                        saleId = 0L,
                        productId = it.product.id,
                        productName = it.product.name,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        purchasePrice = it.product.purchasePrice,
                        discount = it.discount,
                        total = it.total
                    )
                }

                val saleId = repository.executeSaleTransaction(
                    sale = sale,
                    items = saleItems,
                    paymentAmount = amountPaid,
                    paymentMethod = paymentMethod,
                    customerId = customer?.id
                )

                clearCart()
                _userMessage.emit("Sale completed successfully! Invoice #$invoiceNumber")
                onSuccess(saleId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to complete sale.")
            }
        }
    }

    // Purchase Cart Actions
    fun addToPurchaseCart(product: Product, quantity: Double = 1.0, unitCost: Double = product.purchasePrice) {
        val current = _purchaseCartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity, unitCost = unitCost)
        } else {
            current.add(PurchaseCartItem(product = product, quantity = quantity, unitCost = unitCost))
        }
        _purchaseCartItems.value = current
    }

    fun updatePurchaseCartQuantity(productId: Long, quantity: Double) {
        if (quantity <= 0.0) {
            removeFromPurchaseCart(productId)
            return
        }
        val current = _purchaseCartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = quantity)
            _purchaseCartItems.value = current
        }
    }

    fun removeFromPurchaseCart(productId: Long) {
        _purchaseCartItems.value = _purchaseCartItems.value.filter { it.product.id != productId }
    }

    fun clearPurchaseCart() {
        _purchaseCartItems.value = emptyList()
        _selectedSupplier.value = null
    }

    fun selectSupplier(supplier: Supplier?) {
        _selectedSupplier.value = supplier
    }

    fun completePurchase(
        invoiceNumber: String,
        amountPaid: Double,
        paymentMethod: String,
        notes: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val items = _purchaseCartItems.value
                if (items.isEmpty()) {
                    onError("Purchase cart is empty.")
                    return@launch
                }

                val subtotal = FinancialEngine.calculateSubtotal(items.map { it.total })
                val total = subtotal
                val balance = FinancialEngine.calculateBalance(total, amountPaid)
                val paymentStatus = FinancialEngine.determinePaymentStatus(total, amountPaid).name

                val supplier = _selectedSupplier.value
                val purchase = Purchase(
                    invoiceNumber = invoiceNumber.ifBlank { "PO-${System.currentTimeMillis() % 100000}" },
                    date = System.currentTimeMillis(),
                    supplierId = supplier?.id,
                    supplierName = supplier?.name ?: "Direct Supplier",
                    subtotal = subtotal,
                    discount = 0.0,
                    tax = 0.0,
                    total = total,
                    amountPaid = amountPaid,
                    balance = balance,
                    paymentStatus = paymentStatus,
                    paymentMethod = paymentMethod,
                    notes = notes
                )

                val purchaseItems = items.map {
                    PurchaseItem(
                        purchaseId = 0L,
                        productId = it.product.id,
                        productName = it.product.name,
                        quantity = it.quantity,
                        unitCost = it.unitCost,
                        total = it.total
                    )
                }

                val purchaseId = repository.executePurchaseTransaction(
                    purchase = purchase,
                    items = purchaseItems,
                    paymentAmount = amountPaid,
                    paymentMethod = paymentMethod,
                    supplierId = supplier?.id
                )

                clearPurchaseCart()
                _userMessage.emit("Purchase recorded successfully!")
                onSuccess(purchaseId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to record purchase.")
            }
        }
    }

    // Product CRUD
    fun saveProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (product.name.isBlank()) {
                    onError("Product name is required.")
                    return@launch
                }
                if (product.id == 0L) {
                    repository.insertProduct(product)
                    _userMessage.emit("Product created successfully.")
                } else {
                    repository.updateProduct(product)
                    _userMessage.emit("Product updated successfully.")
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error saving product.")
            }
        }
    }

    fun adjustStock(productId: Long, newStock: Double, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.adjustStock(productId, newStock, reason)
                _userMessage.emit("Stock adjusted successfully.")
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to adjust stock.")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _userMessage.emit("Product deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete product: ${e.message}")
            }
        }
    }

    // Customer CRUD
    fun saveCustomer(customer: Customer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (customer.name.isBlank()) {
                    onError("Customer name is required.")
                    return@launch
                }
                if (customer.id == 0L) {
                    repository.insertCustomer(customer)
                    _userMessage.emit("Customer added successfully.")
                } else {
                    repository.updateCustomer(customer)
                    _userMessage.emit("Customer updated successfully.")
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error saving customer.")
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customer)
                _userMessage.emit("Customer deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete customer: ${e.message}")
            }
        }
    }

    // Supplier CRUD
    fun saveSupplier(supplier: Supplier, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (supplier.name.isBlank()) {
                    onError("Supplier name is required.")
                    return@launch
                }
                if (supplier.id == 0L) {
                    repository.insertSupplier(supplier)
                    _userMessage.emit("Supplier added successfully.")
                } else {
                    repository.updateSupplier(supplier)
                    _userMessage.emit("Supplier updated successfully.")
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error saving supplier.")
            }
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplier)
                _userMessage.emit("Supplier deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete supplier: ${e.message}")
            }
        }
    }

    // Expense CRUD
    fun saveExpense(expense: Expense, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (expense.amount <= 0.0) {
                    onError("Expense amount must be greater than 0.")
                    return@launch
                }
                if (expense.id == 0L) {
                    repository.insertExpense(expense)
                    _userMessage.emit("Expense recorded.")
                } else {
                    repository.updateExpense(expense)
                    _userMessage.emit("Expense updated.")
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error saving expense.")
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
                _userMessage.emit("Expense deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete expense: ${e.message}")
            }
        }
    }

    // Category CRUD
    fun addCategory(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                if (name.isNotBlank()) {
                    repository.insertCategory(Category(name = name.trim(), description = description))
                    _userMessage.emit("Category added.")
                }
            } catch (e: Exception) {
                _userMessage.emit("Failed to add category: ${e.message}")
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                _userMessage.emit("Category deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to delete category: ${e.message}")
            }
        }
    }

    // Settings
    fun updateBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            try {
                repository.updateBusinessProfile(profile)
                _userMessage.emit("Business profile updated.")
            } catch (e: Exception) {
                _userMessage.emit("Failed to update profile: ${e.message}")
            }
        }
    }

    fun setAllowNegativeStock(allow: Boolean) {
        viewModelScope.launch {
            repository.setSetting("allow_negative_stock", if (allow) "true" else "false")
            _userMessage.emit("Policy updated: Allow negative stock = $allow")
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setSetting("theme_mode", mode)
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            localePreferencesManager.setAppLanguage(lang)
            repository.setSetting("app_language", lang)
        }
    }

    suspend fun getSaleItems(saleId: Long): List<SaleItem> {
        return repository.getSaleItemsSync(saleId)
    }

    suspend fun getStockMovements(productId: Long): List<StockMovement> {
        return repository.getStockMovementsForProduct(productId).first()
    }
}
