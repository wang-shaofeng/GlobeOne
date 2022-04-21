/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.util

import org.joda.time.format.ISODateTimeFormat
import ph.com.globe.model.shop.domain_models.Validity
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 *  This file contains functions for parsing date form strings and vice-versa
 */

@Suppress("NewApi", "SimpleDateFormat")
fun String.checkValidity(greaterThanAndroidO: Boolean): Boolean {
    val validityDate = this.toDateOrNull()
    val datePattern = "yyyy-MM-dd'T'hh:mm:ss"

    if (greaterThanAndroidO) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(datePattern)
        val myDate: String = current.format(formatter)

        myDate.toDateOrNull()?.let { currentDate ->
            return currentDate.before(validityDate) || currentDate == validityDate
        }
    } else {
        val formatter = SimpleDateFormat(datePattern)
        val myDate: String = formatter.format(Date())

        myDate.toDateOrNull()?.let { currentDate ->
            return currentDate.before(validityDate) || currentDate == validityDate
        }
    }
    return false
}

fun String.toDateOrNull(): Date? =
    try {
        ISODateTimeFormat.dateTimeParser().parseDateTime(this).toDate()
    } catch (e: Exception) {
        null
    }

fun String.toDateWithTimeZoneOrNull(
    timeZone: TimeZone = TimeZone.getTimeZone("GMT")
) =
    listOf(
        PROMO_EXPIRY_DATE_PATTERN,
        LOAD_EXPIRY_DATE_PATTERN,
        REWARDS_EXPIRY_DATE_PATTERN
    ).map { pattern ->
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.timeZone = timeZone
            sdf.parse(this)
        } catch (e: Exception) {
            null
        }
    }.filterNotNull().firstOrNull()


fun Long?.toFormattedStringOrNull(globeDateFormat: GlobeDateFormat) =
    this?.let { SimpleDateFormat(globeDateFormat.format, Locale.getDefault()).format(this) }

fun Long?.toFormattedStringOrEmpty(globeDateFormat: GlobeDateFormat) =
    this.toFormattedStringOrNull(globeDateFormat) ?: ""

fun Date?.toFormattedStringOrNull(globeDateFormat: GlobeDateFormat) =
    this?.time?.toFormattedStringOrNull(globeDateFormat)

fun Date?.toFormattedStringOrEmpty(globeDateFormat: GlobeDateFormat) =
    this?.toFormattedStringOrNull(globeDateFormat) ?: ""

// to be revised
fun String.convertToTimeFormat(): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, this.toFloat().toInt())
    calendar.set(Calendar.MILLISECOND, 0)

    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val mins = calendar.get(Calendar.MINUTE)
    val secs = calendar.get(Calendar.SECOND)

    return if (mins < 1) {
        "$secs sec"
    } else if (hours < 1) {
        "$mins min $secs sec"
    } else {
        "$hours hour $mins min $secs sec"
    }
}

sealed class GlobeDateFormat {

    abstract val format: String

    /**
     * MMM dd, yyyy
     */
    object Default : GlobeDateFormat() {
        override val format = "MMM dd, yyyy"
    }

    /**
     * month abbr custom date
     * MMM. dd, yyyy
     */
    object MonthAbbrCustomDate : GlobeDateFormat() {
        override val format = "MMM. dd, yyyy"
    }

    /**
     * month full name custom date
     * MMMM dd, yyyy
     */
    object MonthFullNameCustomDate : GlobeDateFormat() {
        override val format = "MMMM dd, yyyy"
    }

    /**
     * MMMM dd, yyyy h:mm a
     */
    object GroupDataFormat : GlobeDateFormat() {
        override val format = "MMMM dd, yyyy h:mm a"
    }

    /**
     * MMM dd
     */
    object RewardPointsExpiry : GlobeDateFormat() {
        override val format = "MMM dd"
    }

    /**
     * yyyy-MM-dd
     */
    object ProfileApi : GlobeDateFormat() {
        override val format = "yyyy-MM-dd"
    }

    /**
     * yyyy-MM-dd
     */
    object ISO8601 : GlobeDateFormat() {
        override val format = "yyyy-MM-dd"
    }

    /**
     * M/d/yyyy
     */
    object PrepaidTransaction : GlobeDateFormat() {
        override val format = "M/d/yyyy"
    }

    /**
     * MM/dd/yyyy
     */
    object SecurityQuestionApi : GlobeDateFormat() {
        override val format = "MM/dd/yyyy"
    }

    /**
     * dd/MM
     */
    object HidingSpinwheel : GlobeDateFormat() {
        override val format = "dd/MM"
    }

    /**
     * MMMM dd, yyyy
     */
    object Voucher : GlobeDateFormat() {
        override val format = "MMMM dd, yyyy"
    }

    object ORDownload : GlobeDateFormat() {
        override val format = "yyyyMMdd"
    }
}

fun Validity?.toMillis(): Long =
    this?.let {
        days * SECONDS_IN_ONE_DAY * MILLISECONDS_IN_A_SECOND +
                hours * SECONDS_IN_ONE_HOUR * MILLISECONDS_IN_A_SECOND
    } ?: 0L

fun getValidityFromTimestamp(timestamp: Long): Validity {
    val days = timestamp / SECONDS_IN_ONE_DAY
    val hours = (timestamp % SECONDS_IN_ONE_DAY) / SECONDS_IN_ONE_HOUR
    return Validity(days.toInt(), hours.toInt())
}

private fun Validity.equalTo(validity: Validity): Boolean {
    return this.days == validity.days && this.hours == validity.hours
}

fun Validity.isNoExpiry(): Boolean {
    return this.equalTo(getValidityFromTimestamp(NO_EXPIRY_TIMESTAMP))
}

fun Validity?.toEndDate(): String =
    Date(System.currentTimeMillis() + this.toMillis()).toFormattedStringOrEmpty(GlobeDateFormat.Default)

const val PROMO_EXPIRY_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
const val LOAD_EXPIRY_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.S"
const val REWARDS_EXPIRY_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"

const val MILLISECONDS_IN_A_SECOND = 1000L
const val SECONDS_IN_ONE_DAY = 86400
const val SECONDS_IN_ONE_HOUR = 60 * 60
const val NO_EXPIRY_TIMESTAMP = 4294967296L

