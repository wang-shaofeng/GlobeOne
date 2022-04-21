/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.shop

data class ContactData(
    val contactName: String = "",
    val phoneNumber: String = ""
)

data class PhoneNumber(
    val countryCode: Int,
    val subscriberNumber: String
)

fun String.justPhoneNumber() = buildString {
    this@justPhoneNumber.forEach {
        if (it in '0'..'9') {
            append(it)
        }
    }
}.removePrefix("00")

fun PhoneNumber.formattedForPhilippines(): String {
    val fullNumber = countryCode.toString() + subscriberNumber
    val cleanedNumber = fullNumber.justPhoneNumber().removePrefix("63")
    return "0$cleanedNumber"
}

fun String.formattedForPhilippines(): String {
    var cleanedNumber = this.justPhoneNumber().removePrefix("63")
    if (!cleanedNumber.startsWith("0"))
        cleanedNumber = "0$cleanedNumber"
    return cleanedNumber
}

fun String.phoneNumberStringValid() = length == 11 && startsWith("0") && !startsWith("00")
