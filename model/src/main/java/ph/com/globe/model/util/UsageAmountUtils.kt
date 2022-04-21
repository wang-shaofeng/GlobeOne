/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util

import ph.com.globe.model.account.percentage

/**
 * This class responsible for any type of usage consumption formatting (data, calls and texts).
 * For instance, if you need to show data usage, you can call [getDataUsageAmount]
 * function to convert kilobytes into normal displaying format (like "15.5 GB" or "250 MB")
 * via [convertKiloBytesToFormattedAmount] and [getMegaOrGigaStringFromKiloBytes] functions.
 *
 * In this way, you'll have a [UsageAmount] model with formatted remaining/total values and its units.
 * */
data class UsageAmount(
    val remaining: String,
    val total: String,
    val remainingUnit: String,
    val totalUnit: String,
    val percentage: Int
)

/**
 * UI model for displaying usages on Dashboard (data, calls and texts).
 * Includes [UsageAmount] model to setup remaining/total values, its units and percentage.
 *
 * @param subscriptionsIncluded used only for calls and texts specific UI state
 * */
data class UsageAmountUIModel(
    val usageAmount: UsageAmount,
    val isUnlimited: Boolean = false,
    val hasSubscriptions: Boolean = false,
    val subscriptionsIncluded: Boolean = false
) {

    fun getRemainingAmount() = if (!isUnlimited && hasSubscriptions)
        usageAmount.getRemainingPartFormatted() else ""

    fun getTotalAmount() = if (!isUnlimited && hasSubscriptions)
        usageAmount.getTotalPartFormatted() else ""
}

fun getDataUsageAmount(remaining: Int, total: Int) = UsageAmount(
    remaining = remaining.convertKiloBytesToFormattedAmount(),
    total = total.convertKiloBytesToFormattedAmount(),
    remainingUnit = remaining.getMegaOrGigaStringFromKiloBytes(),
    totalUnit = total.getMegaOrGigaStringFromKiloBytes(),
    percentage = percentage(remaining, total)
)

fun getCallsUsageAmount(remaining: Int, total: Int) = UsageAmount(
    remaining = remaining.toString(),
    total = total.toString(),
    remainingUnit = CALLS_UNIT,
    totalUnit = CALLS_UNIT,
    percentage = percentage(remaining, total)
)

fun getTextsUsageAmount(remaining: Int, total: Int) = UsageAmount(
    remaining = remaining.toString(),
    total = total.toString(),
    remainingUnit = TEXTS_UNIT,
    totalUnit = TEXTS_UNIT,
    percentage = percentage(remaining, total)
)

const val CALLS_UNIT = "mins"
const val TEXTS_UNIT = "texts"

// Useful extensions to display usage amount
fun UsageAmount.getRemainingPartFormatted(): String = "$remaining $remainingUnit"

fun UsageAmount.getTotalPartFormatted(): String = "$total $totalUnit"

fun UsageAmount.toFormattedConsumption(): String {
    return if (remainingUnit != totalUnit) {
        "$remaining $remainingUnit / $total $totalUnit"
    } else {
        "$remaining / $total $totalUnit"
    }
}
