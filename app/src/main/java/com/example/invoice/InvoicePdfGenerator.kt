package com.example.invoice

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.local.entity.BusinessProfile
import com.example.data.local.entity.Invoice
import com.example.data.local.entity.SaleItem
import com.example.domain.FinancialEngine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        items: List<SaleItem>,
        profile: BusinessProfile?
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard point width
        val pageHeight = 842 // A4 standard point height
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val currency = profile?.currency ?: "USD"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Header Background Banner
        paint.color = Color.parseColor("#0F172A") // Deep slate
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

        // Accent top bar
        paint.color = Color.parseColor("#38BDF8") // Cyan
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, paint)

        // Business Name
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        val businessName = profile?.name?.ifBlank { "MERCURY BUSINESS" } ?: "MERCURY BUSINESS"
        canvas.drawText(businessName, 36f, 48f, paint)

        // Business Contact subtext
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#94A3B8")
        val contactInfo = buildString {
            profile?.phone?.let { if (it.isNotBlank()) append("Tel: $it  |  ") }
            profile?.email?.let { if (it.isNotBlank()) append("$it  |  ") }
            profile?.address?.let { if (it.isNotBlank()) append(it) }
        }
        canvas.drawText(contactInfo, 36f, 68f, paint)

        if (!profile?.taxNumber.isNullOrBlank()) {
            canvas.drawText("Tax No: ${profile?.taxNumber}", 36f, 84f, paint)
        }

        // "INVOICE" Title right aligned
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("INVOICE", pageWidth - 140f, 48f, paint)

        // Invoice Number & Date right aligned
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#38BDF8")
        canvas.drawText("#${invoice.invoiceNumber}", pageWidth - 140f, 68f, paint)
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("Date: ${dateFormat.format(Date(invoice.issueDate))}", pageWidth - 140f, 84f, paint)

        // Customer Details Section
        var currentY = 145f
        paint.color = Color.parseColor("#475569")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("BILLED TO:", 36f, currentY, paint)

        currentY += 16f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 13f
        paint.isFakeBoldText = true
        val custName = invoice.customerName.ifBlank { "General Walk-in Customer" }
        canvas.drawText(custName, 36f, currentY, paint)

        paint.isFakeBoldText = false
        paint.textSize = 10f
        paint.color = Color.parseColor("#64748B")
        if (invoice.customerPhone.isNotBlank()) {
            currentY += 14f
            canvas.drawText("Phone: ${invoice.customerPhone}", 36f, currentY, paint)
        }
        if (invoice.customerAddress.isNotBlank()) {
            currentY += 14f
            canvas.drawText("Address: ${invoice.customerAddress}", 36f, currentY, paint)
        }

        // Status Badge (PAID, PARTIALLY_PAID, UNPAID)
        val badgeX = pageWidth - 140f
        val badgeY = 140f
        val badgeRect = RectF(badgeX, badgeY, badgeX + 104f, badgeY + 26f)
        val status = invoice.status.uppercase()
        val (bgColor, textColor) = when (status) {
            "PAID" -> Color.parseColor("#DCFCE7") to Color.parseColor("#15803D")
            "PARTIALLY_PAID" -> Color.parseColor("#FEF3C7") to Color.parseColor("#B45309")
            else -> Color.parseColor("#FEE2E2") to Color.parseColor("#B91C1C")
        }
        paint.color = bgColor
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)
        paint.color = textColor
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText(status.replace("_", " "), badgeX + 12f, badgeY + 17f, paint)

        // Item Table Header
        currentY += 30f
        val tableTop = currentY
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(36f, tableTop, pageWidth - 36f, tableTop + 24f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Item / Description", 46f, tableTop + 16f, paint)
        canvas.drawText("Qty", 320f, tableTop + 16f, paint)
        canvas.drawText("Price", 390f, tableTop + 16f, paint)
        canvas.drawText("Total", pageWidth - 100f, tableTop + 16f, paint)

        // Table Rows
        currentY = tableTop + 24f
        paint.isFakeBoldText = false
        paint.textSize = 10f

        for (item in items) {
            currentY += 20f
            paint.color = Color.parseColor("#1E293B")
            // Truncate name if too long
            val displayName = if (item.productName.length > 34) item.productName.take(32) + "..." else item.productName
            canvas.drawText(displayName, 46f, currentY, paint)

            paint.color = Color.parseColor("#475569")
            canvas.drawText(FinancialEngine.formatQuantity(item.quantity), 320f, currentY, paint)
            canvas.drawText(FinancialEngine.formatCurrency(item.unitPrice, currency), 390f, currentY, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.isFakeBoldText = true
            canvas.drawText(FinancialEngine.formatCurrency(item.total, currency), pageWidth - 100f, currentY, paint)
            paint.isFakeBoldText = false

            // Light horizontal rule
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(36f, currentY + 6f, pageWidth - 36f, currentY + 6f, paint)
        }

        // Totals Box
        currentY += 30f
        val totalsLeft = pageWidth - 240f

        fun drawTotalLine(label: String, amountStr: String, isBold: Boolean = false, colorHex: String = "#1E293B") {
            paint.color = Color.parseColor(colorHex)
            paint.isFakeBoldText = isBold
            paint.textSize = if (isBold) 12f else 10f
            canvas.drawText(label, totalsLeft, currentY, paint)
            canvas.drawText(amountStr, pageWidth - 100f, currentY, paint)
            currentY += 18f
        }

        drawTotalLine("Subtotal:", FinancialEngine.formatCurrency(invoice.subtotal, currency))
        if (invoice.discount > 0.0) {
            drawTotalLine("Discount:", "- " + FinancialEngine.formatCurrency(invoice.discount, currency), colorHex = "#DC2626")
        }
        if (invoice.tax > 0.0) {
            drawTotalLine("Tax:", FinancialEngine.formatCurrency(invoice.tax, currency))
        }

        // Divider
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(totalsLeft, currentY - 4f, pageWidth - 36f, currentY - 4f, paint)

        drawTotalLine("Grand Total:", FinancialEngine.formatCurrency(invoice.total, currency), isBold = true, colorHex = "#0F172A")
        drawTotalLine("Amount Paid:", FinancialEngine.formatCurrency(invoice.paid, currency), colorHex = "#16A34A")
        drawTotalLine("Balance Due:", FinancialEngine.formatCurrency(invoice.balance, currency), isBold = true, colorHex = if (invoice.balance > 0.0) "#DC2626" else "#475569")

        // Notes section at bottom
        if (invoice.notes.isNotBlank()) {
            currentY = maxOf(currentY + 20f, 680f)
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 9f
            paint.isFakeBoldText = true
            canvas.drawText("NOTES / TERMS:", 36f, currentY, paint)
            paint.isFakeBoldText = false
            currentY += 14f
            canvas.drawText(invoice.notes, 36f, currentY, paint)
        }

        // Footer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 9f
        paint.isFakeBoldText = false
        val footerText = "Thank you for your business! Generated by MERCURY Business Manager."
        canvas.drawText(footerText, 36f, pageHeight - 30f, paint)

        pdfDocument.finishPage(page)

        // Save to cache directory
        val invoiceDir = File(context.cacheDir, "invoices")
        if (!invoiceDir.exists()) invoiceDir.mkdirs()
        val file = File(invoiceDir, "Invoice_${invoice.invoiceNumber}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        return file
    }

    fun shareInvoicePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice: ${file.nameWithoutExtension}")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the invoice.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Invoice PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun printInvoice(context: Context, file: File, jobName: String = "Mercury_Invoice") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) return

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val pdi = android.print.PrintDocumentInfo.Builder(jobName)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(pdi, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val input = file.inputStream()
                    val output = FileOutputStream(destination?.fileDescriptor)
                    input.copyTo(output)
                    input.close()
                    output.close()
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }
}
