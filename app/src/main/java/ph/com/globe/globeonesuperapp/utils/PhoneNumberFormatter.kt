/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import ph.com.globe.model.account.NumberType
import ph.com.globe.model.account.toNumberType
import ph.com.globe.model.shop.formattedForPhilippines

const val PHILIPPINES_COUNTRY_CODE_NUMBER_STRING = "63"
const val PHILIPPINES_COUNTRY_CODE_NUMBER_INT = 63
const val PHILIPPINES_LOCAL_NUMBER_SIZE = 9
private const val MOBILE_NUMBER_PREFIX = "0"

fun String.formatPhoneNumber() = substring(0, 4) + " " + substring(4, 7) + " " + substring(7)

fun String.formatPhoneNumberOtp() =
    "+63" + substring(0, 3).replace("*", "•") + " " + substring(3, 6).replace(
        "*",
        "•"
    ) + " " + substring(6)

fun String.convertToClassicNumberFormat(): String {
    return if (this.startsWith(MOBILE_NUMBER_PREFIX)) {
        this.substringAfter(MOBILE_NUMBER_PREFIX)
    } else {
        this
    }
}

/**
 * Only used for the landline number display.
 * The landline number length must be 9, else
 * won't show the landline number.
 * Add prefix '0' to the landline number, if it
 * starts with "02", format to (02)[last 8 digits],
 * else format to (0[first 2 digits])[last 7 digits].
 */
fun String.formatLandlineNumber(): String {
    if (this.length != PHILIPPINES_LOCAL_NUMBER_SIZE) {
        return ""
    }
    return (MOBILE_NUMBER_PREFIX + this).convertToUserFriendlyLandlineNumber()
}

fun String.convertToPrefixNumberFormat(): String {
    return if (!this.startsWith(MOBILE_NUMBER_PREFIX)) {
        MOBILE_NUMBER_PREFIX + this
    } else {
        this
    }
}

fun String.convertToUserFriendlyLandlineNumber(): String = takeIf { it.length >= 10 }?.let {
    val number = if (it.startsWith("02")) {
        it.replaceFirst("02", "(02) ")
    } else {
        it.replaceFirst("${it[0]}${it[1]}${it[2]}", "(${it[0]}${it[1]}${it[2]}) ")
    }

    number.substring(0, number.length - 4) + " " + number.substring(
        number.length - 4,
        number.length
    )
} ?: ""

fun String.toDisplayUINumberFormat(): String =
    when (toNumberType()) {
        // if we are having the AccountNumber no formatting is required
        is NumberType.AccountNumber -> this
        // if the msisdn is LandlineNumber we should format is to user friendly format
        is NumberType.LandlineNumber -> this.convertToUserFriendlyLandlineNumber()
        // if the msisdn is MobileNumber we should format is to user friendly format
        is NumberType.MobileNumber -> formattedForPhilippines().formatPhoneNumber()
    }

fun String.justPhoneNumber() = buildString {
    this@justPhoneNumber.forEach {
        if (it in '0'..'9') {
            append(it)
        }
    }
}.removePrefix("00")
