/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountSegment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@JsonClass(generateAdapter = true)
data class GetPrepaidPromoActiveSubscriptionRequest(
    val serviceNumber: String,
    val forceRefresh: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class GetPrepaidPromoActiveSubscriptionResponse(
    val activePromoSubscriptions: PromoActiveSubscriptionJson?
)

@JsonClass(generateAdapter = true)
data class PromoActiveSubscriptionJson(
    val callUsage: List<UsageItemJson>?,
    val textUsage: List<UsageItemJson>?,
    val lifeStyle: List<UsageItemJson>?
)

@JsonClass(generateAdapter = true)
data class UsageItemJson(
    val skelligWallet: String,
    val promoType: String,
    val totalAllocated: Double?,
    val volumeRemaining: Double?,
    val unit: String,
    val expiryDate: Long
)

data class AccountDetailsUsageUIModel(
    val remaining: Int = 0,
    val total: Int = 0,
    val unlimited: Boolean = false,
    val remainingPercentage: Int = 0,
    val bucketId: String = "",
    val expirationDate: String = "",
    val usageType: PlanUsageType? = null,
    val addOnUsage: Boolean = false
)

sealed class PlanUsageType {
    object CallsUsage : PlanUsageType()
    object TextsUsage : PlanUsageType()
}

fun UsageItemJson.toAccountDetailsUsagesModel(): AccountDetailsUsageUIModel {
    val remaining = volumeRemaining?.toInt() ?: -1
    val allocated = totalAllocated?.toInt() ?: -1
    val unlimited = remaining == REMAINING_UNLIMITED

    return AccountDetailsUsageUIModel(
        remaining,
        allocated,
        unlimited,
        remainingPercentage = if (unlimited) 100 else percentage(remaining, allocated),
        skelligWallet,
        expirationDate = getExpirationDateFormatted(expiryDate)
    )
}

fun getExpirationDateFormatted(expirationDate: Long): String {
    return SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.US).format(expirationDate)
}

fun percentage(part: Int, whole: Int): Int {
    val percent = part.toFloat() * 100 / whole
    return if (!percent.isNaN()) percent.roundToInt()
    else 0
}

fun UsageUIModel.applyActiveSubscription(
    activeSubscription: GetPrepaidPromoActiveSubscriptionResponse
): UsageUIModel {
    with(this) {
        activeSubscription.activePromoSubscriptions?.textUsage?.let { texts ->
            for (bucket in texts) {
                val bucketTotal = bucket.totalAllocated?.toInt() ?: -1
                val bucketRemaining = bucket.volumeRemaining?.toInt() ?: -1
                if (bucketRemaining == REMAINING_UNLIMITED)
                    areTextsUnlimited = true
                else {
                    if (bucket.skelligWallet in listOf("SOB", "SAB", "SGB")) {
                        textTotal += bucketTotal
                        textRemaining += bucketRemaining
                    }
                }
            }
        }

        activeSubscription.activePromoSubscriptions?.callUsage?.let { calls ->
            for (bucket in calls) {
                val bucketTotal = bucket.totalAllocated?.toInt() ?: -1
                val bucketRemaining = bucket.volumeRemaining?.toInt() ?: -1
                if (bucketRemaining == REMAINING_UNLIMITED)
                    areCallsUnlimited = true
                else {
                    if (bucket.skelligWallet in listOf("VOB", "VAB", "VGB")) {
                        callsTotal += bucketTotal
                        callsRemaining += bucketRemaining
                    }
                }
            }
        }

        hasCallsSubscriptions = callsRemaining > 0 || areCallsUnlimited
        hasTextSubscriptions = textRemaining > 0 || areTextsUnlimited

        noSubscriptions = !areTextsUnlimited
                && !areCallsUnlimited
                && !isDataUnlimited
                && !hasCallsSubscriptions
                && !hasTextSubscriptions
                && !hasDataSubscriptions

        isLoading = false

        return this
    }
}

data class UsageUIModel(
    val enrolledAccount: EnrolledAccount,
    var dataRemaining: Int = 0,
    var dataTotal: Int = 0,
    var isDataUnlimited: Boolean = false,
    var hasDataSubscriptions: Boolean = false,

    var textRemaining: Int = 0,
    var textTotal: Int = 0,
    var areTextsUnlimited: Boolean = false,
    var hasTextSubscriptions: Boolean = false,
    var textSubscriptionsIncluded: Boolean = false,

    var callsRemaining: Int = 0,
    var callsTotal: Int = 0,
    var areCallsUnlimited: Boolean = false,
    var hasCallsSubscriptions: Boolean = false,
    var callSubscriptionsIncluded: Boolean = false,

    var accountName: String = "",
    var primaryMsisdn: String = "",
    var balance: Float? = null,
    var brand: AccountBrand? = null,
    var segment: AccountSegment? = null,

    var hasGift: Boolean = false,
    var isLoading: Boolean = true,
    var error: UsageError = UsageError(),
    var noSubscriptions: Boolean = false,
    var accountStatus: AccountStatus? = null,
    var platinumAccount: Boolean = false,

    var postpaidPaymentStatus: PostpaidPaymentStatus? = null,

    var balanceFetched: Boolean = false,
    var usageFetched: Boolean = false,
    var giftFetched: Boolean = false,
    var brandFetched: Boolean = false
)

data class UsageError(
    var errorAccountDetails: Boolean = false,
    var errorGroupService: Boolean = false,
    var errorGroupUsage: Boolean = false,
    var errorGroupMemberUsage: Boolean = false,
    var errorPromoSubscriptionUsage: Boolean = false,
    var errorActivePromoSubscription: Boolean = false,
    var errorAccessType: Boolean = false,
    var errorUsageConsumptionReports: Boolean = false,
) {
    fun isError() =
        errorAccountDetails || errorGroupService || errorGroupUsage || errorGroupMemberUsage
                || errorPromoSubscriptionUsage || errorActivePromoSubscription || errorAccessType || errorUsageConsumptionReports
}

fun UsageUIModel.setLoadingFlags() {
    isLoading = true
}

fun UsageUIModel.setPlatinumAccountFlags() {
    platinumAccount = true
    isLoading = false
    giftFetched = true
    balanceFetched = true
    usageFetched = true
}

fun EnrolledAccount.toLoadingUsageUIModel() = UsageUIModel(
    enrolledAccount = this,
    accountName = accountAlias,
    primaryMsisdn = primaryMsisdn,
    segment = segment,
    isLoading = true
)

fun UsageUIModel.isReadyForUiUpdate() =
    this.balanceFetched && this.usageFetched && this.brandFetched && this.giftFetched

/**
 * Function to disable navigation from dashboard to account details when:
 * 1. Loading still executes for postpaid broadband (account details not fetched).
 * */
fun UsageUIModel.isClickable() =
    !(enrolledAccount.isPostpaidBroadband() && isLoading)

sealed class PostpaidPaymentStatus {
    object AllSet : PostpaidPaymentStatus()
    object BillDueSoon : PostpaidPaymentStatus()
    object BillOverdue : PostpaidPaymentStatus()
}

const val REMAINING_UNLIMITED = -1
const val DEFA_BUCKET_ID = "DEFA"
