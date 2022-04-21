/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.balance

import ph.com.globe.globeonesuperapp.utils.ui.formatOptionValue
import java.math.BigDecimal
import java.text.DecimalFormat

fun Double.toPezosFormattedDisplayBalance(): String {
    val balanceFormatted = this.toFormattedDisplayBalance()
    return "$PEZOS_BALANCE_PREFIX$balanceFormatted"
}

fun Float.toPezosFormattedDisplayBalance(): String =
    (this.toDouble()).toPezosFormattedDisplayBalance()

fun Double.toFormattedDisplayBalance(): String = DecimalFormat("#,##0.00").format(this)
fun Float.toFormattedDisplayBalance(): String = (this.toDouble()).toFormattedDisplayBalance()

fun String.toRewardsExpiringAmountFormat() =
    this.toDoubleOrNull()?.toFormattedDisplayBalance() ?: ""

private const val PEZOS_BALANCE_PREFIX = "P"

/**
 *  current value is not greater than 10000
 */
fun Double?.toFormattedDisplayPrice(): String {
    this ?: return "0.00"

    val floatStrings = toString().split(".")
    if (floatStrings.isEmpty()) {
        return "0.00"
    }

    val firstIntValue = floatStrings[0].toIntOrNull()
    firstIntValue ?: return "0.00"
    val firstIntString = firstIntValue.formatOptionValue()

    if (floatStrings.size == 1) {
        return "$firstIntString.00"
    }

    if (floatStrings.size > 2) {
        return "0.00"
    }

    val second = when (floatStrings[1].length) {
        1 -> floatStrings[1] + "0"
        2 -> floatStrings[1]
        else -> floatStrings[1].substring(0..1)
    }

    return ("$firstIntString.$second")
}

fun calculateDiscountPrice(price: Double, discount: Double): Double {
    val value = BigDecimal(price)
    value.setScale(2)
    return value.multiply(BigDecimal.valueOf(discount)).toDouble()
}
