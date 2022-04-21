/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util

import java.text.DecimalFormat
import kotlin.math.floor

fun String.convertKiloToGigaBytes() =
    (this.toLongOrNull() ?: 0) / (KB * KB)

fun String.convertGigaToKiloBytes() =
    (this.toLongOrNull() ?: 0) * (KB * KB)

fun Int.convertKiloBytesToMegaOrGiga() =
    this / if (this >= KB * KB) (KB * KB) else KB

fun String.getMegaOrGigaStringFromKiloBytes() =
    if (this.toInt() >= KB * KB) GB_STRING else MB_STRING

fun Int.getMegaOrGigaStringFromKiloBytes() =
    if (this >= KB * KB) GB_STRING else MB_STRING

// If data amount is zero, no matter of the original unit, the unit of the result should be GB
fun String.getMegaOrGigaStringFromKiloBytesAndZero() =
    if (this.toInt() >= KB * KB || this.toInt() == 0) GB_STRING else MB_STRING

fun String.megaBytesToDataUnitsFormatted(): String {
    val mb = toDouble()
    val gb = mb / KB
    return if (mb >= KB) {
        String.format("%.1f $GB_STRING", gb)
    } else {
        String.format("%.0f $MB_STRING", mb)
    }
}

fun Int.convertKiloBytesToFormattedAmount(): String {
    return when {
        this >= KB_IN_GB -> {
            val df = DecimalFormat("#.##")
            df.format(this.toFloat() / KB_IN_GB)
        }
        this in KB_IN_MB until KB_IN_GB -> {
            floor(this.toFloat() / KB_IN_MB).toInt().toString()
        }
        else -> "0"
    }
}
