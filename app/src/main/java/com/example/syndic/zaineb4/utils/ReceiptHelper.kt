package com.example.syndic.zaineb4.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.File

object ReceiptHelper {

    private const val TAG = "ReceiptHelper"

    /**
     * Saves a Bitmap directly into the device's public Downloads directory.
     */
    fun saveBitmapToDownloads(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                    }
                    Log.d(TAG, "Receipt saved via MediaStore: $uri")
                    true
                } else false
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                file.outputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)
                Log.d(TAG, "Receipt saved via File: ${file.absolutePath}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bitmap: ${e.message}", e)
            false
        }
    }

    /**
     * Downloads the receipt image for an expense from Firebase and saves it to the device's Downloads directory.
     */
    fun downloadReceipt(context: Context, expenseId: Long) {
        Toast.makeText(context, "جاري تحميل التوصيل...", Toast.LENGTH_SHORT).show()
        val db = FirebaseFirestore.getInstance()
        db.collection("syndic_receipts").document(expenseId.toString()).get()
            .addOnSuccessListener { snapshot ->
                var base64 = snapshot.getString("image")
                if (!base64.isNullOrBlank()) {
                    try {
                        if (base64.startsWith("data:")) {
                            val commaIndex = base64.indexOf(",")
                            if (commaIndex != -1) {
                                base64 = base64.substring(commaIndex + 1)
                            }
                        }
                        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            val fileName = "Recu_Depense_${expenseId}_${System.currentTimeMillis()}.jpg"
                            val saved = saveBitmapToDownloads(context, bitmap, fileName)
                            if (saved) {
                                Toast.makeText(
                                    context,
                                    "✅ تم حفظ التوصيل في مجلد التحميلات (Downloads)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(context, "❌ فشل حفظ التوصيل", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "❌ صورة التوصيل غير صالحة", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding receipt: ${e.message}", e)
                        Toast.makeText(context, "❌ خطأ في معالجة الصورة", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "⚠️ لا توجد صورة لهذا التوصيل", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching receipt: ${e.message}", e)
                Toast.makeText(context, "❌ فشل تحميل التوصيل من السيرفر", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Shares the receipt bitmap using FileProvider.
     */
    fun shareReceiptBitmap(context: Context, bitmap: Bitmap, expenseId: Long) {
        try {
            val file = File(context.cacheDir, "recu_${expenseId}.jpg")
            file.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Partager le reçu / مشاركة التوصيل"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing receipt: ${e.message}", e)
            Toast.makeText(context, "❌ تعذر مشاركة التوصيل", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Converts a content Uri to compressed Base64 string for Firestore.
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

            var inSampleSize = 1
            val maxDimension = 1200
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize

            val bitmap = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting uri to base64: ${e.message}", e)
            null
        }
    }

    /**
     * Downloads a payment receipt from Firebase and saves it to device's Downloads folder.
     * If no receipt image was uploaded by admin, automatically generates an official PDF receipt!
     */
    fun downloadPaymentReceipt(
        context: Context,
        apartmentId: Long,
        year: Int,
        month: Int,
        apartmentName: String = "",
        buildingName: String = "",
        residentName: String = "",
        amount: Double = 0.0
    ) {
        val key = "${apartmentId}_${year}_${month}"
        Toast.makeText(context, "جاري تحضير وصل الأداء...", Toast.LENGTH_SHORT).show()
        val db = FirebaseFirestore.getInstance()
        db.collection("syndic_payment_receipts").document(key).get()
            .addOnSuccessListener { snapshot ->
                var base64 = snapshot.getString("image")
                if (!base64.isNullOrBlank()) {
                    try {
                        if (base64.startsWith("data:")) {
                            val commaIndex = base64.indexOf(",")
                            if (commaIndex != -1) base64 = base64.substring(commaIndex + 1)
                        }
                        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            val cleanApt = apartmentName.ifBlank { "Apt$apartmentId" }.replace(" ", "_")
                            val fileName = "Recu_Paiement_${year}_M${month}_${cleanApt}_${System.currentTimeMillis()}.jpg"
                            val saved = saveBitmapToDownloads(context, bitmap, fileName)
                            if (saved) {
                                Toast.makeText(
                                    context,
                                    "✅ تم حفظ صورة التوصيل في مجلد التحميلات (Downloads)",
                                    Toast.LENGTH_LONG
                                ).show()
                                sharePaymentReceiptBitmap(context, bitmap, key)
                            } else {
                                Toast.makeText(context, "❌ فشل حفظ التوصيل", Toast.LENGTH_SHORT).show()
                            }
                            return@addOnSuccessListener
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding payment receipt: ${e.message}", e)
                    }
                }

                // If no image attached, generate official PDF receipt!
                generateAndExportPdfReceipt(context, apartmentId, apartmentName, buildingName, residentName, year, month, amount)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching payment receipt from server, generating PDF: ${e.message}", e)
                generateAndExportPdfReceipt(context, apartmentId, apartmentName, buildingName, residentName, year, month, amount)
            }
    }

    private fun generateAndExportPdfReceipt(
        context: Context,
        apartmentId: Long,
        apartmentName: String,
        buildingName: String,
        residentName: String,
        year: Int,
        month: Int,
        amount: Double
    ) {
        val pdfFile = PdfHelper.generatePaymentReceiptPdf(
            context = context,
            apartmentId = apartmentId,
            apartmentName = apartmentName,
            buildingName = buildingName,
            residentName = residentName,
            year = year,
            month = month,
            amount = amount
        )
        if (pdfFile != null) {
            PdfHelper.handlePdfExport(context, pdfFile)
        } else {
            Toast.makeText(context, "❌ تعذر إنشاء وصل الأداء", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares payment receipt bitmap via FileProvider.
     */
    fun sharePaymentReceiptBitmap(context: Context, bitmap: Bitmap, key: String) {
        try {
            val file = File(context.cacheDir, "recu_pay_${key}.jpg")
            file.outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Partager le reçu de paiement"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing payment receipt: ${e.message}", e)
            Toast.makeText(context, "❌ تعذر مشاركة التوصيل", Toast.LENGTH_SHORT).show()
        }
    }
}

