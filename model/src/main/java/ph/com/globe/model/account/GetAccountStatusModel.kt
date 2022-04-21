/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.*

data class GetAccountStatusParams(
    val msisdn: String,
    val segment: AccountSegment
)

fun GetAccountStatusParams.toQueryMap(): Map<String, String> = mapOf(
    msisdn.toHeaderPair(), SEGMENT_KEY to segment.toString()
)

@JsonClass(generateAdapter = true)
data class GetAccountStatusResponse(
    val result: GetAccountStatusResult
)

@JsonClass(generateAdapter = true)
data class GetAccountStatusResult(
    @StringAsAccountBrand
    val brand: AccountBrand,
    @StringAsAccountBrandType
    val brandType: AccountBrandType,
    val alternativeMobileNumber: String?,
    val accountNumber: String?,
    val email: String?,
    val status: String?,
    val statusDescription: String?
)

sealed class NumberType {
    object MobileNumber : NumberType() {
        override fun toString(): String {
            return MOBILE_NUMBER_STRING
        }
    }

    object AccountNumber : NumberType() {
        override fun toString(): String {
            return ACCOUNT_NUMBER_STRING
        }
    }

    object LandlineNumber : NumberType() {
        override fun toString(): String {
            return LANDLINE_NUMBER_STRING
        }
    }
}

fun String.toHeaderPair(): Pair<String, String> = when (toNumberType()) {
    NumberType.MobileNumber -> "mobileNumber" to this
    NumberType.AccountNumber -> "accountNumber" to this
    NumberType.LandlineNumber -> "landlineNumber" to this
}

fun String.toNumberType(): NumberType =
    when {
        length == 11 && startsWith("09") -> NumberType.MobileNumber
        length == 10 && (startsWith("02") || startsWith("0")) -> NumberType.LandlineNumber
        else -> NumberType.AccountNumber
    }

fun String.isInvalidFormat(): Boolean =
    // if the number is AccountNumber but the length is wrong we can assume that the number is invalid
    // we are doing this extra step only on user input to check if the number is actually valid
    toNumberType() is NumberType.AccountNumber && length !in 9..10

fun NumberType.toSubscribeType(): Int =
    when (this) {
        is NumberType.AccountNumber, NumberType.LandlineNumber -> SUBSCRIBE_TYPE_ACCOUNT_NUMBER
        is NumberType.MobileNumber -> SUBSCRIBE_TYPE_MOBILE_NUMBER
    }

fun String.isMobileNumber(): Boolean =
    this.toNumberType() is NumberType.MobileNumber

fun String.isAccountNumber(): Boolean =
    this.toNumberType() is NumberType.AccountNumber

fun String.isLandlineNumber(): Boolean =
    this.toNumberType() is NumberType.LandlineNumber

const val MOBILE_NUMBER_STRING = "Mobile number"
const val ACCOUNT_NUMBER_STRING = "Account number"
const val LANDLINE_NUMBER_STRING = "Landline number"

const val SUBSCRIBE_TYPE_MOBILE_NUMBER = 1
const val SUBSCRIBE_TYPE_ACCOUNT_NUMBER = 2
