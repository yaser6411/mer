package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.backup.BackupRestoreScreen
import com.example.ui.screens.contacts.CustomersScreen
import com.example.ui.screens.contacts.SuppliersScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.invoices.InvoicesScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.purchases.PurchasesScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.SalesPosScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.viewmodel.MercuryViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.launch

data class NavigationItem(
    val destination: ScreenDestination,
    val titleRes: Int,
    val icon: ImageVector
)

val MainDrawerItems = listOf(
    NavigationItem(ScreenDestination.DASHBOARD, R.string.nav_dashboard, Icons.Default.Dashboard),
    NavigationItem(ScreenDestination.SALES_POS, R.string.nav_pos, Icons.Default.PointOfSale),
    NavigationItem(ScreenDestination.PRODUCTS, R.string.nav_products, Icons.Default.Inventory),
    NavigationItem(ScreenDestination.PURCHASES, R.string.nav_purchases, Icons.Default.ShoppingCart),
    NavigationItem(ScreenDestination.INVOICES, R.string.nav_invoices, Icons.AutoMirrored.Filled.ReceiptLong),
    NavigationItem(ScreenDestination.CUSTOMERS, R.string.nav_customers, Icons.Default.People),
    NavigationItem(ScreenDestination.SUPPLIERS, R.string.nav_suppliers, Icons.Default.Business),
    NavigationItem(ScreenDestination.EXPENSES, R.string.nav_expenses, Icons.Default.MoneyOff),
    NavigationItem(ScreenDestination.REPORTS, R.string.nav_reports, Icons.Default.Assessment),
    NavigationItem(ScreenDestination.BACKUP_RESTORE, R.string.nav_backup, Icons.Default.Backup),
    NavigationItem(ScreenDestination.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MercuryViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to ViewModel events
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // State Collection
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currency ?: "USD"
    val products by viewModel.allProducts.collectAsState()
    val activeProducts by viewModel.activeProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val sales by viewModel.allSales.collectAsState()
    val purchases by viewModel.allPurchases.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val inventoryCostValue by viewModel.totalInventoryCostValue.collectAsState()
    val customerReceivables by viewModel.totalReceivables.collectAsState()
    val supplierPayables by viewModel.totalPayables.collectAsState()
    val allowNegativeStock by viewModel.allowNegativeStock.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    // POS Cart
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val cartDiscount by viewModel.cartDiscount.collectAsState()
    val cartTaxRate by viewModel.cartTaxRate.collectAsState()

    // Purchase Cart
    val purchaseCartItems by viewModel.purchaseCartItems.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                // Drawer Brand Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_mercury_logo),
                            contentDescription = "MERCURY Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MERCURY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = stringResource(R.string.drawer_tagline),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = profile?.name ?: stringResource(R.string.drawer_default_store),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Items
                MainDrawerItems.forEach { item ->
                    val isSelected = currentScreen == item.destination
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.titleRes), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        selected = isSelected,
                        onClick = {
                            viewModel.navigateTo(item.destination)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("nav_drawer_item_${item.destination.name}"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val titleRes = MainDrawerItems.find { it.destination == currentScreen }?.titleRes ?: R.string.app_name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentScreen == ScreenDestination.DASHBOARD) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_mercury_logo),
                                    contentDescription = "MERCURY Logo",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(stringResource(titleRes), fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("nav_drawer_open_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.nav_open_menu))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("top_app_bar")
                )
            },
            bottomBar = {
                // Bottom Navigation Bar with primary fast access destinations
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val bottomItems = listOf(
                        NavigationItem(ScreenDestination.DASHBOARD, R.string.nav_dashboard, Icons.Default.Dashboard),
                        NavigationItem(ScreenDestination.SALES_POS, R.string.nav_pos, Icons.Default.PointOfSale),
                        NavigationItem(ScreenDestination.PRODUCTS, R.string.nav_products, Icons.Default.Inventory),
                        NavigationItem(ScreenDestination.INVOICES, R.string.nav_invoices, Icons.AutoMirrored.Filled.ReceiptLong)
                    )

                    bottomItems.forEach { item ->
                        val selected = currentScreen == item.destination
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                            label = { Text(stringResource(item.titleRes), fontSize = 11.sp) },
                            selected = selected,
                            onClick = { viewModel.navigateTo(item.destination) },
                            modifier = Modifier.testTag("bottom_nav_${item.destination.name}")
                        )
                    }

                    // "More" button to open drawer
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = stringResource(R.string.nav_more)) },
                        label = { Text(stringResource(R.string.nav_more), fontSize = 11.sp) },
                        selected = currentScreen !in bottomItems.map { it.destination },
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.testTag("bottom_nav_more")
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    ScreenDestination.DASHBOARD -> DashboardScreen(
                        profile = profile,
                        sales = sales,
                        purchases = purchases,
                        expenses = expenses,
                        lowStockProducts = lowStockProducts,
                        inventoryCostValue = inventoryCostValue,
                        customerReceivables = customerReceivables,
                        supplierPayables = supplierPayables,
                        onNavigate = { viewModel.navigateTo(it) }
                    )

                    ScreenDestination.PRODUCTS -> ProductsScreen(
                        products = products,
                        categories = categories,
                        currency = currency,
                        onSaveProduct = { product, onSuccess, onError ->
                            viewModel.saveProduct(product, onSuccess, onError)
                        },
                        onAdjustStock = { id, qty, reason, onSuccess, onError ->
                            viewModel.adjustStock(id, qty, reason, onSuccess, onError)
                        },
                        onDeleteProduct = { viewModel.deleteProduct(it) },
                        onFetchMovements = { viewModel.getStockMovements(it) }
                    )

                    ScreenDestination.SALES_POS -> SalesPosScreen(
                        products = activeProducts,
                        customers = customers,
                        cartItems = cartItems,
                        selectedCustomer = selectedCustomer,
                        cartDiscount = cartDiscount,
                        cartTaxRate = cartTaxRate,
                        currency = currency,
                        onAddToCart = { viewModel.addToCart(it) },
                        onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                        onRemoveFromCart = { viewModel.removeFromCart(it) },
                        onClearCart = { viewModel.clearCart() },
                        onSelectCustomer = { viewModel.selectCustomer(it) },
                        onSetDiscount = { viewModel.setCartDiscount(it) },
                        onSetTaxRate = { viewModel.setCartTaxRate(it) },
                        onCompleteSale = { paid, method, onSuccess, onError ->
                            viewModel.completeSale(paid, method, onSuccess, onError)
                        }
                    )

                    ScreenDestination.SALES_HISTORY, ScreenDestination.INVOICES -> InvoicesScreen(
                        invoices = invoices,
                        profile = profile,
                        currency = currency,
                        onFetchSaleItems = { viewModel.getSaleItems(it) }
                    )

                    ScreenDestination.PURCHASES -> PurchasesScreen(
                        products = products,
                        suppliers = suppliers,
                        purchases = purchases,
                        cartItems = purchaseCartItems,
                        selectedSupplier = selectedSupplier,
                        currency = currency,
                        onAddToCart = { prod, qty, cost -> viewModel.addToPurchaseCart(prod, qty, cost) },
                        onUpdateQuantity = { id, qty -> viewModel.updatePurchaseCartQuantity(id, qty) },
                        onRemoveFromCart = { viewModel.removeFromPurchaseCart(it) },
                        onClearCart = { viewModel.clearPurchaseCart() },
                        onSelectSupplier = { viewModel.selectSupplier(it) },
                        onCompletePurchase = { inv, paid, method, notes, onSuccess, onError ->
                            viewModel.completePurchase(inv, paid, method, notes, onSuccess, onError)
                        }
                    )

                    ScreenDestination.CUSTOMERS -> CustomersScreen(
                        customers = customers,
                        currency = currency,
                        onSaveCustomer = { customer, onSuccess, onError ->
                            viewModel.saveCustomer(customer, onSuccess, onError)
                        },
                        onDeleteCustomer = { viewModel.deleteCustomer(it) }
                    )

                    ScreenDestination.SUPPLIERS -> SuppliersScreen(
                        suppliers = suppliers,
                        currency = currency,
                        onSaveSupplier = { supplier, onSuccess, onError ->
                            viewModel.saveSupplier(supplier, onSuccess, onError)
                        },
                        onDeleteSupplier = { viewModel.deleteSupplier(it) }
                    )

                    ScreenDestination.EXPENSES -> ExpensesScreen(
                        expenses = expenses,
                        currency = currency,
                        onSaveExpense = { expense, onSuccess, onError ->
                            viewModel.saveExpense(expense, onSuccess, onError)
                        },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )

                    ScreenDestination.REPORTS -> ReportsScreen(
                        sales = sales,
                        expenses = expenses,
                        products = products,
                        customerReceivables = customerReceivables,
                        supplierPayables = supplierPayables,
                        currency = currency
                    )

                    ScreenDestination.BACKUP_RESTORE -> BackupRestoreScreen()

                    ScreenDestination.SETTINGS -> SettingsScreen(
                        profile = profile,
                        allowNegativeStock = allowNegativeStock,
                        themeMode = themeMode,
                        appLanguage = appLanguage,
                        onUpdateProfile = { viewModel.updateBusinessProfile(it) },
                        onSetAllowNegativeStock = { viewModel.setAllowNegativeStock(it) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetAppLanguage = { viewModel.setAppLanguage(it) }
                    )
                }
            }
        }
    }
}
