package com.example.syndic.zaineb4.utils

import android.content.Context
import android.util.Log
import com.example.syndic.zaineb4.data.AppData
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableWorkbook
import java.io.File
import java.util.Calendar

object ExcelHelper {
    fun exportBilanToExcel(appData: AppData, year: Int, month: Int? = null, context: Context): File? {
        var workbook: WritableWorkbook? = null
        try {
            val monthTag = if (month != null) "_M${month}" else "_Complet"
            val fileName = "Syndic_Bilan_${year}${monthTag}_${System.currentTimeMillis()}.xls"
            val file = File(context.cacheDir, fileName)
            workbook = Workbook.createWorkbook(file)

            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthNamesAr = listOf("يناير", "فبراير", "مارس", "أبريل", "ماي", "يونيو", "يوليوز", "غشت", "شتنبر", "أكتوبر", "نونبر", "دجنبر")

            // 1. Filter Expenses
            val filteredExpenses = appData.expenses.filter { exp ->
                val parts = exp.date.split("-")
                if (parts.size >= 2) {
                    val ey = parts[0].toIntOrNull()
                    val em = parts[1].toIntOrNull()
                    if (ey != null && em != null) {
                        if (ey == year) {
                            return@filter (month == null || em == month)
                        }
                        return@filter false
                    }
                }
                true
            }
            val totalExpenses = filteredExpenses.sumOf { it.amount }

            // Sheet: Dépenses (المصاريف)
            val sheetExp = workbook.createSheet("المصاريف (Dépenses)", 0)
            sheetExp.addCell(Label(0, 0, "التاريخ (Date)"))
            sheetExp.addCell(Label(1, 0, "البيان (Description)"))
            sheetExp.addCell(Label(2, 0, "المبلغ (Montant)"))
            sheetExp.addCell(Label(3, 0, "بالتوصيل (Avec Reçu)"))

            if (filteredExpenses.isEmpty()) {
                sheetExp.addCell(Label(0, 1, "Aucune dépense pour cette période (لا توجد مصاريف)"))
            } else {
                filteredExpenses.forEachIndexed { index, exp ->
                    val row = index + 1
                    sheetExp.addCell(Label(0, row, exp.date))
                    sheetExp.addCell(Label(1, row, exp.desc))
                    sheetExp.addCell(Label(2, row, exp.amount.toString()))
                    sheetExp.addCell(Label(3, row, if (exp.hasReceipt) "Oui (نعم)" else "Non (لا)"))
                }
                val expTotalRow = filteredExpenses.size + 1
                sheetExp.addCell(Label(0, expTotalRow, "المجموع (Total)"))
                sheetExp.addCell(Label(2, expTotalRow, totalExpenses.toString()))
            }

            // 2. Payments
            var totalIncomes = 0.0

            if (month == null) {
                // All Months Table
                val sheetPay = workbook.createSheet("الأداءات (Paiements)", 1)
                sheetPay.addCell(Label(0, 0, "العمارة (Immeuble)"))
                sheetPay.addCell(Label(1, 0, "الشقة (Appartement)"))
                months.forEachIndexed { i, m ->
                    sheetPay.addCell(Label(2 + i, 0, m))
                }
                sheetPay.addCell(Label(14, 0, "المجموع (Total)"))

                var payRow = 1
                if (appData.apartments.isEmpty()) {
                    sheetPay.addCell(Label(0, 1, "Aucun appartement (لا توجد شقق)"))
                } else {
                    appData.apartments.forEach { apt ->
                        val bld = appData.buildings.find { it.id == apt.buildingId }
                        sheetPay.addCell(Label(0, payRow, bld?.name ?: "N/A"))
                        sheetPay.addCell(Label(1, payRow, apt.name))

                        var aptTotal = 0.0
                        for (m in 1..12) {
                            val key = "${apt.id}_${year}_${m}"
                            val amountDouble = appData.payments[key] ?: 0.0
                            sheetPay.addCell(Label(1 + m, payRow, if (amountDouble > 0) amountDouble.toString() else "0"))
                            aptTotal += amountDouble
                        }
                        sheetPay.addCell(Label(14, payRow, aptTotal.toString()))
                        totalIncomes += aptTotal
                        payRow++
                    }
                    sheetPay.addCell(Label(0, payRow, "المجموع العام (Total Général)"))
                    sheetPay.addCell(Label(14, payRow, totalIncomes.toString()))
                }
            } else {
                // Specific Month Table
                val mLabel = "${months.getOrElse(month - 1) { "M$month" }} (${monthNamesAr.getOrElse(month - 1) { "" }})"
                val sheetPay = workbook.createSheet("الأداءات ($mLabel)", 1)
                sheetPay.addCell(Label(0, 0, "العمارة (Immeuble)"))
                sheetPay.addCell(Label(1, 0, "الشقة (Appartement)"))
                sheetPay.addCell(Label(2, 0, "المبلغ المؤدى (Montant $mLabel)"))
                sheetPay.addCell(Label(3, 0, "الحالة (Statut)"))

                var payRow = 1
                if (appData.apartments.isEmpty()) {
                    sheetPay.addCell(Label(0, 1, "Aucun appartement (لا توجد شقق)"))
                } else {
                    appData.apartments.forEach { apt ->
                        val bld = appData.buildings.find { it.id == apt.buildingId }
                        sheetPay.addCell(Label(0, payRow, bld?.name ?: "N/A"))
                        sheetPay.addCell(Label(1, payRow, apt.name))

                        val key = "${apt.id}_${year}_${month}"
                        val amountDouble = appData.payments[key] ?: 0.0
                        sheetPay.addCell(Label(2, payRow, if (amountDouble > 0) amountDouble.toString() else "0"))
                        sheetPay.addCell(Label(3, payRow, if (amountDouble > 0) "Payé (خالص)" else "Non payé (غير مؤدى)"))
                        totalIncomes += amountDouble
                        payRow++
                    }
                    sheetPay.addCell(Label(0, payRow, "المجموع (Total)"))
                    sheetPay.addCell(Label(2, payRow, totalIncomes.toString()))
                }

                // Additional Sheet: Full Year 12 Months
                val sheetAnnual = workbook.createSheet("الجدول السنوي للأداءات $year", 2)
                sheetAnnual.addCell(Label(0, 0, "العمارة (Immeuble)"))
                sheetAnnual.addCell(Label(1, 0, "الشقة (Appartement)"))
                months.forEachIndexed { i, m ->
                    sheetAnnual.addCell(Label(2 + i, 0, m))
                }
                sheetAnnual.addCell(Label(14, 0, "المجموع (Total)"))

                var annRow = 1
                appData.apartments.forEach { apt ->
                    val bld = appData.buildings.find { it.id == apt.buildingId }
                    sheetAnnual.addCell(Label(0, annRow, bld?.name ?: "N/A"))
                    sheetAnnual.addCell(Label(1, annRow, apt.name))

                    var aptTotal = 0.0
                    for (m in 1..12) {
                        val key = "${apt.id}_${year}_${m}"
                        val amountDouble = appData.payments[key] ?: 0.0
                        sheetAnnual.addCell(Label(1 + m, annRow, if (amountDouble > 0) amountDouble.toString() else "0"))
                        aptTotal += amountDouble
                    }
                    sheetAnnual.addCell(Label(14, annRow, aptTotal.toString()))
                    annRow++
                }
            }

            // 3. Summary Sheet
            val sheetRes = workbook.createSheet("الملخص المالي (Résumé)", workbook.numberOfSheets)
            sheetRes.addCell(Label(0, 0, "البند (Rubrique)"))
            sheetRes.addCell(Label(1, 0, "القيمة (Valeur)"))

            val monthText = if (month == null) "جميع الشهور (Tous les mois)"
            else "${months.getOrElse(month - 1) { "M$month" }} - ${monthNamesAr.getOrElse(month - 1) { "" }}"

            sheetRes.addCell(Label(0, 1, "السنة (Année)"))
            sheetRes.addCell(Label(1, 1, year.toString()))

            sheetRes.addCell(Label(0, 2, "الشهر (Mois)"))
            sheetRes.addCell(Label(1, 2, monthText))

            sheetRes.addCell(Label(0, 3, "مجموع المداخيل (Total Recettes)"))
            sheetRes.addCell(Label(1, 3, totalIncomes.toString()))

            sheetRes.addCell(Label(0, 4, "مجموع المصاريف (Total Dépenses)"))
            sheetRes.addCell(Label(1, 4, totalExpenses.toString()))

            sheetRes.addCell(Label(0, 5, "الرصيد الصافي (Solde Net)"))
            sheetRes.addCell(Label(1, 5, (totalIncomes - totalExpenses).toString()))

            workbook.write()
            return file
        } catch (e: Exception) {
            Log.e("ExcelHelper", "Error generating excel file: ${e.message}")
            return null
        } finally {
            try {
                workbook?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
