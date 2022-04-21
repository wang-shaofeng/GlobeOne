/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.util

fun String.nonEmptyOrNull() = this.trim().takeIf { it.isNotEmpty() }

fun String.subStringWithChecks(startIndex: Int, endIndex: Int): String {
    val start = if (startIndex > this.length) this.length
    else kotlin.math.max(startIndex,0)
    val end = if (endIndex > this.length) this.length
    else endIndex

    return this.substring(start, end)
}
