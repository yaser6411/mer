package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.BusinessProfileDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.dao.SupplierDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BusinessProfile::class,
        Category::class,
        Product::class,
        Customer::class,
        Supplier::class,
        Sale::class,
        SaleItem::class,
        Purchase::class,
        PurchaseItem::class,
        Expense::class,
        Payment::class,
        Invoice::class,
        StockMovement::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MercuryDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: MercuryDatabase? = null

        fun getDatabase(context: Context): MercuryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MercuryDatabase::class.java,
                    "mercury_business.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default business profile and standard categories in background
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.businessProfileDao().insertOrUpdate(
                                    BusinessProfile(
                                        id = 1L,
                                        name = "MERCURY Business",
                                        currency = "USD",
                                        invoicePrefix = "INV-",
                                        taxRate = 0.0
                                    )
                                )
                                database.appSettingDao().setSetting(
                                    AppSetting("allow_negative_stock", "false")
                                )
                                database.appSettingDao().setSetting(
                                    AppSetting("theme_mode", "SYSTEM")
                                )
                                database.appSettingDao().setSetting(
                                    AppSetting("app_language", "SYSTEM")
                                )
                                // Add starter categories
                                val defaultCategories = listOf("General", "Services", "Retail", "Supplies", "Wholesale")
                                for (cat in defaultCategories) {
                                    database.categoryDao().insert(Category(name = cat))
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
