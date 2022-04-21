/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package android.print

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.WebView
import java.io.File

fun WebView.saveHtmlAsPdf(fileName: String) {

    val itemUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)
        if (!file.exists())
            file.createNewFile()
        Uri.fromFile(file)
    } else {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        }
        context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    }

    itemUri?.let {
        createPrintDocumentAdapter("Receipt").apply {
            onLayout(
                null,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.NA_GOVT_LETTER)
                    .setResolution(
                        PrintAttributes.Resolution(
                            "RESOLUTION_ID",
                            "RESOLUTION_ID",
                            100,
                            100
                        )
                    )
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build(),
                null,
                object : PrintDocumentAdapter.LayoutResultCallback() {},
                null
            )
            onWrite(
                arrayOf(PageRange.ALL_PAGES),
                context.contentResolver.openFileDescriptor(itemUri, "w"),
                null,
                object : PrintDocumentAdapter.WriteResultCallback() {
                    override fun onWriteFinished(pages: Array<PageRange>) {}
                })
        }
    }
}
