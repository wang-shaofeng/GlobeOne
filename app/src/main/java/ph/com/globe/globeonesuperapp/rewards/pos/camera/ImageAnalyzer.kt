/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


class ImageAnalyzer(
    private val listener: ImageListener = {}
) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 300) {

            image.image?.let {
                val byteBuffer = it.planes.toByteBuffer(image.width, image.height)

                // convert byteBuffer to byteArray
                val yuv420ByteArray = byteBuffer.toByteArray()

                val nv21ByteArray = yuv420ByteArray.toNV21ByteArray(image.width, image.height)

                val bitmap = nv21ByteArray?.nv21ByteArrayToBitmap()

                val rotatedBitmap = bitmap?.rotate(image.imageInfo.rotationDegrees)
                // if bitmap is not null, we call callback with that bitmap
                rotatedBitmap?.let { listener(rotatedBitmap) }
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
        image.close()
    }
}

typealias ImageListener = (image: Bitmap) -> Unit
