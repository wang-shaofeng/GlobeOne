/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.utils

fun String.numericalToTextMonth() =
    when (this) {
        "01" -> "January"
        "02" -> "February"
        "03" -> "March"
        "04" -> "April"
        "05" -> "May"
        "06" -> "June"
        "07" -> "July"
        "08" -> "August"
        "09" -> "September"
        "10" -> "October"
        "11" -> "November"
        "12" -> "December"
        else -> ""
    }

fun String.numericalToShortTextMonth() =
    when (this) {
        "01" -> "Jan."
        "02" -> "Feb."
        "03" -> "Mar."
        "04" -> "Apr."
        "05" -> "May"
        "06" -> "Jun."
        "07" -> "Jul."
        "08" -> "Aug."
        "09" -> "Sept."
        "10" -> "Oct."
        "11" -> "Nov."
        "12" -> "Dec."
        else -> ""
    }

fun String.convertDateToGroupDataFormat(short: Boolean = false): String {
    if (this.isEmpty()) {
        return ""
    }
    return try {
        "${
            with(this.substring(5, 7)) {
                if (short)
                    numericalToShortTextMonth()
                else
                    numericalToTextMonth()
            }
        } ${this.substring(8, 10)}, ${this.substring(0, 4)}"
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun String.isNoExpiry(): Boolean =
    equals(END_DATE_OF_NO_EXPIRY_PREFIX.convertDateToGroupDataFormat())

const val END_DATE_OF_NO_EXPIRY_PREFIX = "1970-01-01T08:00:00"
