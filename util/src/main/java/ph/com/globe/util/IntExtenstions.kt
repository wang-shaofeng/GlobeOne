/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.util

fun Int.toOrdinal() = this.toString() + when (this) {
    1, 21, 31 -> "st"
    2, 22 -> "nd"
    3, 23 -> "rd"
    in 4..20, in 24..30 -> "th"
    else -> ""
}
