package ph.com.globe.globeonesuperapp.utils

import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.model.account.*
import ph.com.globe.model.billings.domain_models.BILLING_DETAILS_DATE_PATTERN
import ph.com.globe.model.billings.domain_models.BillingsDetails
import ph.com.globe.model.shop.ContentSubscriptionUIModel
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toOrdinal
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH

/**
 * This file includes useful extension functions and some formatters for
 * [GetPostpaidActivePromoSubscriptionResponse] since its structure is complicated.
 * */

// region Plan usages on account details
internal fun GetPostpaidActivePromoSubscriptionResponse.getPlanUsages(): List<AccountDetailsUsageUIModel>? {
    return activePromoSubscriptions.callAndText?.firstOrNull()?.let {
        val planUsages: MutableList<AccountDetailsUsageUIModel> = mutableListOf()

        if (it.hasCallsValue()) {
            val remaining = it.callsRemaining?.extractCallsFromFormattedString() ?: 0
            val total = it.callsTotal?.extractCallsFromFormattedString() ?: 0
            planUsages.add(
                AccountDetailsUsageUIModel(
                    remaining,
                    total,
                    remainingPercentage = percentage(remaining, total),
                    usageType = PlanUsageType.CallsUsage
                )
            )
        }

        if (it.hasTextsValue()) {
            val remaining = it.smsRemaining?.extractTextsFromFormattedString() ?: 0
            val total = it.smsTotal?.extractTextsFromFormattedString() ?: 0
            planUsages.add(
                AccountDetailsUsageUIModel(
                    remaining,
                    total,
                    remainingPercentage = percentage(remaining, total),
                    usageType = PlanUsageType.TextsUsage
                )
            )
        }

        // 'Add on' usages (temporary commented)
        /*activePromoSubscriptions.addOn.firstOrNull()?.let {
            it.callUsage?.let { calls ->
                var remaining = 0
                var total = 0
                var expirationDate = 0L

                for (bucket in calls) {
                    if (bucket.skelligWallet in listOf(VOB, VAB, VGB)) {
                        remaining += bucket.volumeRemaining?.toInt() ?: 0
                        total += bucket.totalAllocated?.toInt() ?: 0
                    }
                    if (bucket.expiryDate > expirationDate)
                        expirationDate = bucket.expiryDate
                }

                planUsages.add(
                    AccountDetailsUsageUIModel(
                        remaining,
                        total,
                        remainingPercentage = percentage(remaining, total),
                        expirationDate = getExpirationDateFormatted(expirationDate),
                        usageType = PlanUsageType.CallsUsage,
                        addOnUsage = true
                    )
                )
            }
            it.textUsage?.let { texts ->
                var remaining = 0
                var total = 0
                var expirationDate = 0L

                for (bucket in texts) {
                    if (bucket.skelligWallet in listOf(SOB, SAB, SGB)) {
                        remaining += bucket.volumeRemaining?.toInt() ?: 0
                        total += bucket.totalAllocated?.toInt() ?: 0
                    }
                    if (bucket.expiryDate > expirationDate)
                        expirationDate = bucket.expiryDate
                }

                planUsages.add(
                    AccountDetailsUsageUIModel(
                        remaining,
                        total,
                        remainingPercentage = percentage(remaining, total),
                        expirationDate = getExpirationDateFormatted(expirationDate),
                        usageType = PlanUsageType.TextsUsage,
                        addOnUsage = true
                    )
                )
            }
        }*/

        planUsages

    }
}

internal fun GetPostpaidActivePromoSubscriptionResponse.getContentSubscriptions(): List<ContentSubscriptionUIModel> {
    return activePromoSubscriptions.lifeStyle?.map {
        ContentSubscriptionUIModel(
            promoName = it.offerName ?: "",
            expiryDate = it.expiryDate ?: "",
            isActivated = true
        )
    } ?: emptyList()
}

internal fun GetPostpaidActivePromoSubscriptionResponse.getOfferDescription(): String {
    return activePromoSubscriptions.callAndText?.firstOrNull()?.offerDescription ?: ""
}

internal fun BillingsDetails.getRefreshDate(): String {
    return cutOffDate?.let { cutOffDate ->

        // Possible case to have 'cutOffDate' as "5" (means day of month) or usual formatted date
        val cutOffDay = cutOffDate.toIntOrNull()
            ?: cutOffDate.substring(8, 10).toIntOrNull() ?: 0

        val calendar = Calendar.getInstance()
        val currentDayOfMonth = calendar.get(DAY_OF_MONTH)

        when {
            currentDayOfMonth > cutOffDay -> {
                calendar.set(DAY_OF_MONTH, cutOffDay + 1)
                calendar.set(MONTH, calendar.get(MONTH) + 1)
            }
            currentDayOfMonth < cutOffDay -> {
                calendar.set(DAY_OF_MONTH, cutOffDay + 1)
            }
        }

        SimpleDateFormat(BILLING_DETAILS_DATE_PATTERN, Locale.US)
            .format(calendar.time).convertDateToGroupDataFormat()
    } ?: ""
}

internal fun BillingsDetails.getCutOffDay(): String {
    return cutOffDate?.let { cutOffDate ->

        // Possible case to have 'cutOffDate' as "5" (means day of month) or usual formatted date
        val cutOffDay = cutOffDate.toIntOrNull()
            ?: cutOffDate.toDateOrNull()?.let { date ->
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.get(Calendar.DAY_OF_MONTH)
            }

        cutOffDay?.toOrdinal() ?: ""
    } ?: ""
}
// endregion


// region Plan usages on dashboard
internal fun UsageUIModel.applyPostpaidActiveSubscription(
    activeSubscriptionResponse: GetPostpaidActivePromoSubscriptionResponse
): UsageUIModel {

    activeSubscriptionResponse.activePromoSubscriptions.callAndText?.firstOrNull()?.let {

        if (it.hasCallsValue()) {
            callsRemaining += it.callsRemaining?.extractCallsFromFormattedString() ?: 0
            callsTotal += it.callsTotal?.extractCallsFromFormattedString() ?: 0
        } else {
            callSubscriptionsIncluded = it.callsIncluded()
        }

        if (it.hasTextsValue()) {
            textRemaining += it.smsRemaining?.extractTextsFromFormattedString() ?: 0
            textTotal += it.smsTotal?.extractTextsFromFormattedString() ?: 0
        } else {
            textSubscriptionsIncluded = it.textsIncluded()
        }

        hasCallsSubscriptions = it.hasCallsValue() || it.callsIncluded()
        hasTextSubscriptions = it.hasTextsValue() || it.textsIncluded()
    }

    usageFetched = true
    isLoading = false

    return this
}
// endregion


// Extract remaining / total values from formatted string
private fun String.extractCallsFromFormattedString(): Int =
    if (endsWith("minutes")) split(" ")[0].toFloatOrNull()?.toInt() ?: 0
    else 0

private fun String.extractTextsFromFormattedString(): Int =
    replace(".0", "").filter { it.isDigit() }.toIntOrNull() ?: 0


// Check for existing subscriptions by remaining / total values
private fun PostpaidSubscriptionJson.hasCallsValue(): Boolean =
    callsRemaining?.isNotEmpty() == true && callsTotal?.isNotEmpty() == true

private fun PostpaidSubscriptionJson.hasTextsValue(): Boolean =
    smsRemaining?.isNotEmpty() == true && smsTotal?.isNotEmpty() == true


// Check for existing subscriptions by offer description
private fun PostpaidSubscriptionJson.callsIncluded(): Boolean =
    offerDescription?.contains("Calls", ignoreCase = true) ?: false

private fun PostpaidSubscriptionJson.textsIncluded(): Boolean =
    offerDescription?.contains("Texts", ignoreCase = true) ?: false
