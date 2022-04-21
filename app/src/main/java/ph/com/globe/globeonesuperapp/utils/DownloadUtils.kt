package ph.com.globe.globeonesuperapp.utils

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun Fragment.saveBillToDownloads(decodedPdf: ByteArray, filename: String) {
    var outputStream: OutputStream? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        requireContext().contentResolver?.also { resolver ->

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            outputStream = pdfUri?.let { resolver.openOutputStream(it) }
        }

    } else {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(downloadsDir, filename)
        outputStream = FileOutputStream(pdfFile)
    }

    outputStream?.use {
        it.write(decodedPdf)
        it.flush()
        it.close()
    }
}

fun Fragment.saveBillToCache(decodedPdf: ByteArray) {

    val pdfFile = File(requireContext().cacheDir, "cached_bill.pdf")
    pdfFile.delete()
    pdfFile.createNewFile()

    FileOutputStream(pdfFile).use {
        it.write(decodedPdf)
        it.flush()
        it.close()
    }
}
