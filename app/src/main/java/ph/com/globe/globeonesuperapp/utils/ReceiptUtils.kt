/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.payment.payment_successful.RECEIPT
import java.io.*
import java.util.*

fun Fragment.showSnackbar(snackbarViewBinding: ViewBinding) {
    val snackbar = Snackbar.make(requireView(), "", Snackbar.LENGTH_SHORT)
    snackbar.view.setBackgroundColor(Color.TRANSPARENT)
    val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
    snackbarLayout.addView(snackbarViewBinding.root)
    snackbar.show()
}

fun Context.copyToClipboard(textToCopy: String, key: String) {
    val clipboard = ContextCompat.getSystemService(
        this,
        ClipboardManager::class.java
    ) as ClipboardManager

    val clip = ClipData.newPlainText(
        key,
        // Data to be clipped to clipboard
        textToCopy
    )

    clipboard.setPrimaryClip(clip)
}

/**
 * Function that performs screenshot of the provided
 * @param screenshotRootView and saves it's bitmap as a file as
 * @param filename
 *
 * @return 'true' if successful, 'false' otherwise
 */
fun Context.takeScreenshot(screenshotRootView: View, filename: String): Boolean {

    try {
        // Create Bitmap instance
        val bitmapToBeSaved: Bitmap = Bitmap.createBitmap(
            screenshotRootView.width,
            screenshotRootView.height,
            Bitmap.Config.ARGB_8888
        )

        val bitmapDrawerCanvas = Canvas(bitmapToBeSaved)

        // Manually render view to Canvas
        screenshotRootView.draw(bitmapDrawerCanvas)

        // Here we are initialising the format of the date and time to be appended to out image
        val dateTimeFormat: CharSequence = DateFormat.format("yyyy-MM-dd_hh:mm:ss", Date())

        saveImageToStorage(bitmapToBeSaved, "$filename-$dateTimeFormat.png")
    } catch (io: FileNotFoundException) {
        io.printStackTrace()
        return false
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
    return true
}

// Saves the image as media inside the Pictures directory
// so this will be seen within the system's gallery app
fun Context.saveImageToStorage(bitmapToBeSaved: Bitmap, filename: String) {

    // Output stream
    var outputStream: OutputStream? = null
    // File val used for saving an image with a legacy flow for devices running Android OS < Q
    var legacyImage: File? = null

    // For devices running android >= Q
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // getting the contentResolver
        contentResolver?.also { resolver ->

            // Content resolver will process the contentValues
            val contentValues = ContentValues().apply {

                // putting file information in content values
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            // Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            // Opening an OutputStream with the Uri that we got
            outputStream = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        // These for devices running on android < Q
        // The recommended non deprecated Context.getExternalFilesDir(String) is not working
        val imagesDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val saveDirectory = File(imagesDirectory!!.path)
        if (!saveDirectory.exists()) {
            saveDirectory.mkdir()
        }
        legacyImage = File(imagesDirectory, filename)
        outputStream = FileOutputStream(legacyImage)
    }

    outputStream?.use {
        // Finally writing the bitmap to the output stream that we opened
        bitmapToBeSaved.compress(
            Bitmap.CompressFormat.PNG,
            // quality param is ignored for PNG formats as PNG is lossless format
            100,
            it
        )
        it.flush()
        it.close()
    }

    legacyImage?.let { // pre Q
        // explicitly scanning for the saved file to be shown in the gallery
        // required for some devices, optional for most of them
        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(legacyImage.toString()),
            null,
            null
        )
    }
}

fun Fragment.takeScreenshotFlow(viewBinding: ViewBinding) {
    if (requireContext().takeScreenshot(viewBinding.root, RECEIPT)) {
        val successScreenshotSnackbar =
            GlobeSnackbarLayoutBinding
                .inflate(LayoutInflater.from(requireContext()))
        successScreenshotSnackbar.tvGlobeSnackbarTitle.setText(R.string.receipt_downloaded)
        successScreenshotSnackbar.tvGlobeSnackbarDescription.setText(R.string.you_can_find_the_receipt_photo_in_the_gallery)

        showSnackbar(successScreenshotSnackbar)
    }
}

const val COPIED_REFERENCE_NUMBER = "globe_super_app_reference_number"
const val COPIED_VOUCHER_CODE = "globe_super_app_voucher_code"
const val COPIED_ORDER_NUMBER = "globe_super_app_order_number"
