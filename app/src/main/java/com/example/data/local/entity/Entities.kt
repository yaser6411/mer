package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "business_profiles")
data class BusinessProfile(
    @PrimaryKey val id: Long = 1L,
    val name: String = "MERCURY Business",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val currency: String = "USD",
    val taxNumber: String = "",
    val taxRate: Double = 0.0,
    val invoicePrefix: String = "INV-",
    val logoUri: String? = null,
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"]),
        Index(value = ["categoryId"]),
        Index(value = ["supplierId"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val categoryId: Long? = null,
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stock: Double = 0.0,
    val minStock: Double = 0.0,
    val unit: String = "pcs",
    val supplierId: Long? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    indices = [Index(value = ["phone"]), Index(value = ["name"])]
)
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val balance: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["phone"]), Index(value = ["name"])]
)
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val balance: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["date"])
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String,
    val date: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val amountPaid: Double = 0.0,
    val balance: Double = 0.0,
    val paymentStatus: String = "PAID", // PAID, PARTIALLY_PAID, UNPAID
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val purchasePrice: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double
)

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["date"])
    ]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String = "",
    val date: Long = System.currentTimeMillis(),
    val supplierId: Long? = null,
    val supplierName: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val amountPaid: Double = 0.0,
    val balance: Double = 0.0,
    val paymentStatus: String = "PAID", // PAID, PARTIALLY_PAID, UNPAID
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitCost: Double,
    val total: Double
)

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["category"]),
        Index(value = ["date"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String, // Rent, Salaries, Transportation, Utilities, Marketing, Maintenance, Supplies, Taxes, Other
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val referenceNumber: String = "",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["type"]),
        Index(value = ["relatedId"]),
        Index(value = ["date"])
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // SALE_PAYMENT, PURCHASE_PAYMENT
    val relatedId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash", // Cash, Card, Bank Transfer, Cheque, Other
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["saleId"]),
        Index(value = ["issueDate"])
    ]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val saleId: Long,
    val invoiceNumber: String,
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paid: Double = 0.0,
    val balance: Double = 0.0,
    val status: String = "PAID",
    val notes: String = ""
)

@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["type"]),
        Index(value = ["timestamp"])
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val productName: String = "",
    val type: String, // PURCHASE, SALE, ADJUSTMENT, RETURN, INITIAL
    val quantity: Double,
    val previousStock: Double,
    val newStock: Double,
    val referenceId: Long? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
