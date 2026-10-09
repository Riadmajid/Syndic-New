package com.example.syndic.zaineb4.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.example.syndic.zaineb4.data.AppData
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    private const val PAGE_WIDTH = 595   // A4 width in points (72dpi)
    private const val PAGE_HEIGHT = 842  // A4 height in points
    private const val MARGIN = 36f
    private const val LINE_HEIGHT = 20f

    private val colorPrimary   = Color.rgb(30, 60, 114)   // #1E3C72
    private val colorSuccess   = Color.rgb(39, 174, 96)    // #27AE60
    private val colorDanger    = Color.rgb(231, 76, 60)    // #E74C3C
    private val colorHeader    = Color.rgb(44, 62, 80)     // #2C3E50
    private val colorRowEven   = Color.rgb(248, 249, 250)
    private val colorRowOdd    = Color.WHITE
    private val colorSeparator = Color.rgb(220, 224, 230)

    fun exportBilanToPdf(appData: AppData, year: Int, month: Int? = null, context: Context): File? {
        val doc = PdfDocument()
        try {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthNamesAr = listOf("يناير", "فبراير", "مارس", "أبريل", "ماي", "يونيو", "يوليوز", "غشت", "شتنبر", "أكتوبر", "نونبر", "دجنبر")

            val monthTag = if (month != null) "_M${month}" else "_Complet"
            val fileName = "Syndic_Bilan_${year}${monthTag}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)

            // 1. Filter expenses
            val filteredExpenses = appData.expenses.filter { exp ->
                val parts = exp.date.split("-")
                if (parts.size >= 2) {
                    val ey = parts[0].toIntOrNull()
                    val em = parts[1].toIntOrNull()
                    if (ey != null && em != null && ey == year) {
                        return@filter (month == null || em == month)
                    }
                    return@filter false
                }
                false
            }
            val totalExpenses = filteredExpenses.sumOf { it.amount }

            // 2. Compute payments
            var totalIncomes = 0.0
            val paymentRows = mutableListOf<Triple<String, String, Double>>() // building, apt, amount

            if (month == null) {
                appData.apartments.forEach { apt ->
                    val bld = appData.buildings.find { it.id == apt.buildingId }
                    var aptTotal = 0.0
                    for (m in 1..12) {
                        aptTotal += appData.payments["${apt.id}_${year}_${m}"] ?: 0.0
                    }
                    totalIncomes += aptTotal
                    paymentRows.add(Triple(bld?.name ?: "N/A", apt.name, aptTotal))
                }
            } else {
                appData.apartments.forEach { apt ->
                    val bld = appData.buildings.find { it.id == apt.buildingId }
                    val amount = appData.payments["${apt.id}_${year}_${month}"] ?: 0.0
                    totalIncomes += amount
                    paymentRows.add(Triple(bld?.name ?: "N/A", apt.name, amount))
                }
            }

            val monthLabel = if (month == null) "Année Complète / جميع الشهور"
            else "${months.getOrElse(month - 1) { "M$month" }} (${monthNamesAr.getOrElse(month - 1) { "" }})"

            // 3. Draw Document
            var pageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            var page = doc.startPage(pageInfo)
            var canvas = page.canvas
            var y = MARGIN

            // Header banner
            y = drawHeader(canvas, year, monthLabel, y)

            // Section 1: Dépenses
            y = drawSectionTitle(canvas, "📋  المصاريف / Dépenses", y)
            y = drawExpenseTableHeader(canvas, y)

            if (filteredExpenses.isEmpty()) {
                y = drawCenteredText(canvas, "Aucune dépense pour cette période (لا توجد مصاريف)", y, Color.GRAY)
            } else {
                filteredExpenses.forEachIndexed { i, exp ->
                    if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 3) {
                        drawFooter(canvas, pageNum)
                        doc.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                        page = doc.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN + 10f
                        y = drawExpenseTableHeader(canvas, y)
                    }
                    val bg = if (i % 2 == 0) colorRowEven else colorRowOdd
                    y = drawExpenseRow(canvas, exp.date, exp.desc, exp.amount, exp.hasReceipt, y, bg)
                }
                y = drawTotalRow(canvas, "Total Dépenses / مجموع المصاريف", totalExpenses, y)
            }

            y += LINE_HEIGHT

            // Section 2: Paiements
            if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 6) {
                drawFooter(canvas, pageNum)
                doc.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 10f
            }

            y = drawSectionTitle(canvas, "💰  الأداءات / Paiements", y)
            y = drawPaymentTableHeader(canvas, y)

            if (paymentRows.isEmpty()) {
                y = drawCenteredText(canvas, "Aucun appartement enregistré (لا توجد شقق)", y, Color.GRAY)
            } else {
                paymentRows.forEachIndexed { i, (bld, apt, amount) ->
                    if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 3) {
                        drawFooter(canvas, pageNum)
                        doc.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                        page = doc.startPage(pageInfo)
                        canvas = page.canvas
                        y = MARGIN + 10f
                        y = drawPaymentTableHeader(canvas, y)
                    }
                    val bg = if (i % 2 == 0) colorRowEven else colorRowOdd
                    val status = if (amount > 0) "✓ Payé" else "✗ Impayé"
                    val statusColor = if (amount > 0) colorSuccess else colorDanger
                    y = drawPaymentRow(canvas, bld, apt, amount, status, statusColor, y, bg)
                }
                y = drawTotalRow(canvas, "Total Paiements / مجموع الأداءات", totalIncomes, y)
            }

            y += LINE_HEIGHT * 1.5f

            // Section 3: Summary
            if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 8) {
                drawFooter(canvas, pageNum)
                doc.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 10f
            }

            y = drawSummary(canvas, year, monthLabel, totalIncomes, totalExpenses, y)

            drawFooter(canvas, pageNum)
            doc.finishPage(page)

            file.outputStream().use { doc.writeTo(it) }
            return file

        } catch (e: Exception) {
            Log.e("PdfHelper", "Error generating PDF: ${e.message}", e)
            return null
        } finally {
            doc.close()
        }
    }

    private fun drawHeader(canvas: Canvas, year: Int, monthLabel: String, startY: Float): Float {
        // Banner background
        val bannerPaint = Paint().apply { color = colorPrimary }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 95f, bannerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Bilan Financier - السنديك ZAINEB 4", MARGIN, 36f, titlePaint)

        val subPaint = Paint().apply {
            color = Color.rgb(205, 225, 255)
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("Année: $year   |   Période: $monthLabel", MARGIN, 60f, subPaint)

        val datePaint = Paint().apply {
            color = Color.rgb(180, 200, 240)
            textSize = 10f
            isAntiAlias = true
        }
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Date d'export: $now", MARGIN, 80f, datePaint)

        return 115f
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint().apply {
            color = colorPrimary
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        // Small indicator box
        val barPaint = Paint().apply { color = colorPrimary }
        canvas.drawRect(MARGIN, y + 2f, MARGIN + 4f, y + 16f, barPaint)
        canvas.drawText(title, MARGIN + 10f, y + 14f, paint)

        val linePaint = Paint().apply { color = colorSeparator; strokeWidth = 0.5f }
        canvas.drawLine(MARGIN, y + 20f, PAGE_WIDTH - MARGIN, y + 20f, linePaint)
        return y + 28f
    }

    private fun drawExpenseTableHeader(canvas: Canvas, y: Float): Float {
        val headerBg = Paint().apply { color = colorHeader }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT + 2f, headerBg)

        val paint = Paint().apply { color = Color.WHITE; textSize = 9.5f; isFakeBoldText = true; isAntiAlias = true }
        val x = MARGIN + 6f
        canvas.drawText("Date (التاريخ)", x, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("Description (البيان)", x + 75f, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("Montant (المبلغ)", x + 270f, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("Reçu (توصيل)", x + 395f, y + LINE_HEIGHT - 3f, paint)
        return y + LINE_HEIGHT + 2f
    }

    private fun drawExpenseRow(canvas: Canvas, date: String, desc: String, amount: Double, hasReceipt: Boolean, y: Float, bg: Int): Float {
        val bgPaint = Paint().apply { color = bg }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT, bgPaint)

        val paint = Paint().apply { color = colorHeader; textSize = 9f; isAntiAlias = true }
        val amtPaint = Paint().apply { color = Color.rgb(192, 57, 43); textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
        val x = MARGIN + 6f
        canvas.drawText(date, x, y + LINE_HEIGHT - 5f, paint)
        val shortDesc = if (desc.length > 36) desc.take(36) + "…" else desc
        canvas.drawText(shortDesc, x + 75f, y + LINE_HEIGHT - 5f, paint)
        canvas.drawText("%.2f DH".format(amount), x + 270f, y + LINE_HEIGHT - 5f, amtPaint)
        canvas.drawText(if (hasReceipt) "Oui (نعم)" else "Non (لا)", x + 395f, y + LINE_HEIGHT - 5f, paint)
        return y + LINE_HEIGHT
    }

    private fun drawPaymentTableHeader(canvas: Canvas, y: Float): Float {
        val headerBg = Paint().apply { color = colorHeader }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT + 2f, headerBg)

        val paint = Paint().apply { color = Color.WHITE; textSize = 9.5f; isFakeBoldText = true; isAntiAlias = true }
        val x = MARGIN + 6f
        canvas.drawText("Immeuble (العمارة)", x, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("Appartement (الشقة)", x + 125f, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("Montant (المبلغ)", x + 265f, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("Statut (الحالة)", x + 395f, y + LINE_HEIGHT - 3f, paint)
        return y + LINE_HEIGHT + 2f
    }

    private fun drawPaymentRow(canvas: Canvas, bld: String, apt: String, amount: Double, status: String, statusColor: Int, y: Float, bg: Int): Float {
        val bgPaint = Paint().apply { color = bg }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT, bgPaint)

        val paint = Paint().apply { color = colorHeader; textSize = 9f; isAntiAlias = true }
        val amtPaint = Paint().apply { color = Color.rgb(41, 128, 185); textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
        val statusPaint = Paint().apply { color = statusColor; textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
        val x = MARGIN + 6f
        canvas.drawText(bld, x, y + LINE_HEIGHT - 5f, paint)
        canvas.drawText(apt, x + 125f, y + LINE_HEIGHT - 5f, paint)
        canvas.drawText("%.2f DH".format(amount), x + 265f, y + LINE_HEIGHT - 5f, amtPaint)
        canvas.drawText(status, x + 395f, y + LINE_HEIGHT - 5f, statusPaint)
        return y + LINE_HEIGHT
    }

    private fun drawTotalRow(canvas: Canvas, label: String, amount: Double, y: Float): Float {
        val bgPaint = Paint().apply { color = Color.rgb(232, 240, 254) }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT + 2f, bgPaint)
        val paint = Paint().apply { color = colorPrimary; textSize = 10f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText(label, MARGIN + 8f, y + LINE_HEIGHT - 3f, paint)
        canvas.drawText("%.2f DH".format(amount), PAGE_WIDTH - MARGIN - 120f, y + LINE_HEIGHT - 3f, paint)
        return y + LINE_HEIGHT + 4f
    }

    private fun drawSummary(canvas: Canvas, year: Int, monthLabel: String, incomes: Double, expenses: Double, y: Float): Float {
        var cy = y
        cy = drawSectionTitle(canvas, "📊  الملخص المالي / Résumé Financier", cy)

        val net = incomes - expenses

        data class SummaryRow(val label: String, val value: String, val color: Int)
        val rows = listOf(
            SummaryRow("Année / السنة", year.toString(), colorHeader),
            SummaryRow("Période / الفترة", monthLabel, colorHeader),
            SummaryRow("Total Recettes / مجموع المداخيل", "%.2f DH".format(incomes), colorSuccess),
            SummaryRow("Total Dépenses / مجموع المصاريف", "%.2f DH".format(expenses), colorDanger),
            SummaryRow("Solde Net / الرصيد الصافي", "%.2f DH".format(net), if (net >= 0) colorSuccess else colorDanger)
        )

        rows.forEachIndexed { i, r ->
            val bg = if (i % 2 == 0) colorRowEven else colorRowOdd
            val bgPaint = Paint().apply { color = bg }
            canvas.drawRect(MARGIN, cy, PAGE_WIDTH - MARGIN, cy + LINE_HEIGHT + 3f, bgPaint)

            val labelPaint = Paint().apply { color = colorHeader; textSize = 10f; isAntiAlias = true }
            val valPaint = Paint().apply { color = r.color; textSize = 10.5f; isFakeBoldText = true; isAntiAlias = true }

            canvas.drawText(r.label, MARGIN + 8f, cy + LINE_HEIGHT - 2f, labelPaint)
            canvas.drawText(r.value, PAGE_WIDTH / 2f + 30f, cy + LINE_HEIGHT - 2f, valPaint)
            cy += LINE_HEIGHT + 3f
        }
        return cy
    }

    private fun drawCenteredText(canvas: Canvas, text: String, y: Float, color: Int): Float {
        val paint = Paint().apply { this.color = color; textSize = 10f; isAntiAlias = true; textAlign = Paint.Align.CENTER }
        canvas.drawText(text, PAGE_WIDTH / 2f, y + LINE_HEIGHT, paint)
        return y + LINE_HEIGHT + 8f
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int) {
        val y = PAGE_HEIGHT - 18f
        val linePaint = Paint().apply { color = colorSeparator; strokeWidth = 0.5f }
        canvas.drawLine(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y - 8f, linePaint)
        val paint = Paint().apply { color = Color.GRAY; textSize = 8.5f; isAntiAlias = true }
        canvas.drawText("Syndic ZAINEB 4  •  Document Officiel", MARGIN, y, paint)
        val rightPaint = Paint().apply { color = Color.GRAY; textSize = 8.5f; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        canvas.drawText("Page $pageNum", PAGE_WIDTH - MARGIN, y, rightPaint)
    }

    /**
     * Saves the generated PDF directly into the device's Downloads directory
     * so it's permanently stored on the phone.
     */
    fun savePdfToDownloads(context: Context, pdfFile: File): Boolean {
        return try {
            val fileName = pdfFile.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { output ->
                        pdfFile.inputStream().use { input -> input.copyTo(output) }
                    }
                    Log.d("PdfHelper", "Saved to Downloads via MediaStore: $uri")
                    true
                } else false
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val destFile = File(downloadsDir, fileName)
                pdfFile.copyTo(destFile, overwrite = true)
                Log.d("PdfHelper", "Saved to Downloads via File: ${destFile.absolutePath}")
                true
            }
        } catch (e: Exception) {
            Log.e("PdfHelper", "Failed to save PDF to Downloads: ${e.message}", e)
            false
        }
    }

    /**
     * Generates an official payment receipt PDF for a resident's payment of a specific month.
     */
    fun generatePaymentReceiptPdf(
        context: Context,
        apartmentId: Long,
        apartmentName: String,
        buildingName: String,
        residentName: String,
        year: Int,
        month: Int,
        amount: Double,
        paymentDate: Long = System.currentTimeMillis()
    ): File? {
        val doc = PdfDocument()
        return try {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthNamesAr = listOf("يناير", "فبراير", "مارس", "أبريل", "ماي", "يونيو", "يوليوز", "غشت", "شتنبر", "أكتوبر", "نونبر", "دجنبر")

            val cleanApt = apartmentName.ifBlank { "Apt$apartmentId" }.replace(" ", "_").replace("/", "_")
            val fileName = "Recu_Paiement_${year}_M${month}_${cleanApt}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)

            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            // Header Banner
            val bannerPaint = Paint().apply { color = colorPrimary }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 130f, bannerPaint)

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("إقامة زينب 4  •  RÉSIDENCE ZAINEB 4", PAGE_WIDTH / 2f, 50f, titlePaint)

            val subPaint = Paint().apply {
                color = Color.rgb(200, 220, 255)
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("وصل أداء واجبات السنديك  •  REÇU DE PAIEMENT DU SYNDIC", PAGE_WIDTH / 2f, 82f, subPaint)

            val docNumPaint = Paint().apply {
                color = Color.rgb(180, 205, 240)
                textSize = 10f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val receiptNum = "REC-${year}${if (month < 10) "0$month" else "$month"}-${apartmentId % 100000}"
            canvas.drawText("N° $receiptNum", PAGE_WIDTH / 2f, 108f, docNumPaint)

            // Info Card Box
            val cardY = 155f
            val cardHeight = 250f
            val cardRect = android.graphics.RectF(MARGIN, cardY, PAGE_WIDTH - MARGIN, cardY + cardHeight)
            val cardBg = Paint().apply { color = Color.rgb(248, 250, 252) }
            val cardStroke = Paint().apply {
                color = colorSeparator
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(cardRect, 12f, 12f, cardBg)
            canvas.drawRoundRect(cardRect, 12f, 12f, cardStroke)

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(paymentDate))
            val labelPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 11f
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val valueArPaint = Paint().apply {
                color = colorPrimary
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
            }

            var lineY = cardY + 34f
            val leftX = MARGIN + 20f
            val rightX = PAGE_WIDTH - MARGIN - 20f

            fun drawInfoRow(labelFr: String, labelAr: String, value: String) {
                canvas.drawText("$labelFr :", leftX, lineY, labelPaint)
                canvas.drawText(value, leftX + 115f, lineY, valuePaint)
                canvas.drawText(labelAr, rightX, lineY, valueArPaint)
                lineY += 34f
            }

            drawInfoRow("Date", "تاريخ الأداء", dateStr)
            drawInfoRow("Immeuble", "العمارة", buildingName.ifBlank { "ZAINEB 4" })
            drawInfoRow("Appartement", "الشقة", apartmentName.ifBlank { "Apt $apartmentId" })
            drawInfoRow("Résident", "الساكن(ة)", residentName.ifBlank { "المحترم(ة)" })
            val mNameFr = months.getOrElse(month - 1) { "M$month" }
            val mNameAr = monthNamesAr.getOrElse(month - 1) { "" }
            drawInfoRow("Période", "الفترة المؤداة", "$mNameFr $year  ($mNameAr $year)")
            drawInfoRow("Montant", "المبلغ المؤدى", "${amount.toInt()} DH  (${String.format(Locale.US, "%.2f", amount)} Dirhams)")

            // Status Badge
            val badgeY = cardY + cardHeight + 25f
            val badgeRect = android.graphics.RectF(MARGIN + 20f, badgeY, PAGE_WIDTH - MARGIN - 20f, badgeY + 50f)
            val badgeBg = Paint().apply { color = Color.rgb(220, 252, 231) }
            val badgeStroke = Paint().apply {
                color = colorSuccess
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(badgeRect, 8f, 8f, badgeBg)
            canvas.drawRoundRect(badgeRect, 8f, 8f, badgeStroke)

            val badgeTextPaint = Paint().apply {
                color = colorSuccess
                textSize = 15f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("✓  مـؤدى بالكامل  /  RÉGLÉ AVEC SUCCÈS", PAGE_WIDTH / 2f, badgeY + 32f, badgeTextPaint)

            // Cachet & Signature Box
            val signY = badgeY + 80f
            val signRect = android.graphics.RectF(PAGE_WIDTH - MARGIN - 200f, signY, PAGE_WIDTH - MARGIN, signY + 95f)
            val signBox = Paint().apply { color = Color.rgb(241, 245, 249) }
            canvas.drawRoundRect(signRect, 8f, 8f, signBox)
            val signTitle = Paint().apply {
                color = colorPrimary
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("إدارة السنديك / Syndic", signRect.centerX(), signY + 24f, signTitle)
            val signSub = Paint().apply {
                color = Color.GRAY
                textSize = 9f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("إقامة زينب 4", signRect.centerX(), signY + 44f, signSub)
            canvas.drawText("[ Cachet & Signature ]", signRect.centerX(), signY + 75f, signSub)

            // Bottom Notices
            val noticePaint = Paint().apply {
                color = Color.GRAY
                textSize = 9f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "هذا الوصل وثيقة رسمية تؤكد أداء واجبات السنديك للشهر المشار إليه أعلاه",
                PAGE_WIDTH / 2f,
                PAGE_HEIGHT - 60f,
                noticePaint
            )
            canvas.drawText(
                "Ce document officiel certifie le paiement des charges de copropriété pour la période indiquée.",
                PAGE_WIDTH / 2f,
                PAGE_HEIGHT - 45f,
                noticePaint
            )

            // Footer
            val footerPaint = Paint().apply { color = colorSeparator; strokeWidth = 0.5f }
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 35f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 35f, footerPaint)
            val brandPaint = Paint().apply { color = Color.GRAY; textSize = 8.5f; isAntiAlias = true }
            canvas.drawText("Syndic ZAINEB 4  •  Application Mobile", MARGIN, PAGE_HEIGHT - 20f, brandPaint)

            doc.finishPage(page)
            doc.writeTo(file.outputStream())
            file
        } catch (e: Exception) {
            Log.e("PdfHelper", "Error generating payment receipt PDF: ${e.message}", e)
            null
        } finally {
            doc.close()
        }
    }

    /**
     * Complete export workflow: saves the file permanently into the phone's Downloads folder,
     * informs the user via Toast, and offers the share / open chooser.
     */
    fun handlePdfExport(context: Context, file: File) {
        val saved = savePdfToDownloads(context, file)
        if (saved) {
            Toast.makeText(
                context,
                "✅ تم حفظ التقرير في مجلد التحميلات (Downloads)",
                Toast.LENGTH_LONG
            ).show()
        }

        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Partager / فتح التقرير"))
        } catch (e: Exception) {
            Log.e("PdfHelper", "Error opening chooser: ${e.message}", e)
        }
    }
}

