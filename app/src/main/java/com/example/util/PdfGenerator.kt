package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.ToBuyItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateShoppingListPdf(
        context: Context,
        items: List<ToBuyItemEntity>,
        userName: String,
        currency: String = "₹"
    ): File? {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (in points)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(27, 94, 56) // #1B5E38 Forest Green
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(100, 100, 100)
                textSize = 11f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(30, 30, 30)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val tableHeaderBgPaint = Paint().apply {
                color = Color.rgb(230, 244, 234)
                style = Paint.Style.FILL
            }

            val textPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 10f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(220, 220, 220)
                strokeWidth = 1f
            }

            var y = 45f

            // Title
            canvas.drawText("Kitchen Pantry & Spices - Market Shopping Slip", 40f, y, titlePaint)
            y += 18f
            canvas.drawText("বাজার ফর্দ ও নিত্যপ্রয়োজনীয় তালিকা", 40f, y, subtitlePaint)
            y += 16f
            canvas.drawText("Generated on: $dateStr  |  Prepared by: $userName", 40f, y, subtitlePaint)
            y += 20f

            // Table Header
            canvas.drawRect(40f, y, 555f, y + 24f, tableHeaderBgPaint)
            val headerY = y + 16f
            canvas.drawText("#", 50f, headerY, headerPaint)
            canvas.drawText("Item Name", 80f, headerY, headerPaint)
            canvas.drawText("Category", 230f, headerY, headerPaint)
            canvas.drawText("Quantity", 330f, headerY, headerPaint)
            canvas.drawText("Rate", 420f, headerY, headerPaint)
            canvas.drawText("Est. Price", 485f, headerY, headerPaint)
            y += 28f

            var totalEst = 0.0

            // Table Rows
            items.forEachIndexed { index, item ->
                if (y > 780f) return@forEachIndexed

                val itemName = "${item.nameEn} (${item.nameBn})"
                val truncatedName = if (itemName.length > 25) itemName.take(23) + ".." else itemName
                val qtyStr = "${item.quantity} ${item.unit}"
                val rateStr = if (item.rate > 0) "$currency${item.rate}" else "-"
                val estPrice = if (item.estimatedPrice > 0) item.estimatedPrice else (item.rate * item.quantity)
                val priceStr = if (estPrice > 0) "$currency${estPrice.toInt()}" else "-"
                totalEst += estPrice

                val boxPaint = Paint().apply {
                    color = Color.rgb(180, 180, 180)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                }
                canvas.drawRect(48f, y + 2f, 58f, y + 12f, boxPaint)

                canvas.drawText("${index + 1}", 64f, y + 10f, textPaint)
                canvas.drawText(truncatedName, 80f, y + 10f, textPaint)
                canvas.drawText(item.category, 230f, y + 10f, textPaint)
                canvas.drawText(qtyStr, 330f, y + 10f, textPaint)
                canvas.drawText(rateStr, 420f, y + 10f, textPaint)
                canvas.drawText(priceStr, 485f, y + 10f, textPaint)

                y += 18f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 4f
            }

            y += 10f
            val summaryPaint = Paint().apply {
                color = Color.rgb(27, 94, 56)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("Total Items: ${items.size}", 40f, y, summaryPaint)
            if (totalEst > 0) {
                canvas.drawText("Total Estimated Cost: $currency${totalEst.toInt()}", 350f, y, summaryPaint)
            }

            y += 35f
            canvas.drawText("Household Companion Sync enabled. Mark off items upon purchase.", 40f, y, subtitlePaint)

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "Shopping_List_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Throwable) {
            // Fallback for headless environments or devices without native PDF canvas
            try {
                val fallbackFile = File(context.cacheDir, "Shopping_List_${System.currentTimeMillis()}.txt")
                val content = buildString {
                    appendLine("==============================================")
                    appendLine("KITCHEN PANTRY & SPICES - SHOPPING LIST")
                    appendLine("Date: $dateStr | Prepared by: $userName")
                    appendLine("==============================================")
                    items.forEachIndexed { i, item ->
                        val est = if (item.estimatedPrice > 0) item.estimatedPrice else (item.rate * item.quantity)
                        appendLine("[ ] ${i + 1}. ${item.nameEn} (${item.nameBn}) - ${item.quantity} ${item.unit} | Rate: $currency${item.rate} | Est: $currency$est")
                    }
                    appendLine("==============================================")
                    appendLine("Household Companion Sync Enabled")
                }
                fallbackFile.writeText(content)
                fallbackFile
            } catch (ex: Exception) {
                null
            }
        }
    }

    fun sharePdf(context: Context, file: File) {
        val mimeType = if (file.name.endsWith(".pdf")) "application/pdf" else "text/plain"
        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.kitchenpantry.rqnvw.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Kitchen Market Shopping List (বাজার ফর্দ)")
            putExtra(Intent.EXTRA_TEXT, "Here is the finalized kitchen market shopping list.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Shopping List"))
    }
}
