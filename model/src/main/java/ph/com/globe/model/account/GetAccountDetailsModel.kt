/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.BRAND_KEY
import ph.com.globe.model.util.brand.SEGMENT_KEY
import ph.com.globe.model.util.convertToAccountStatus

data class GetAccountDetailsParams(
    val msisdn: String,
    val segment: AccountSegment,
    val referenceId: String? = null,
    val verificationType: String? = null
)

fun GetAccountDetailsParams.toQueryMap(): Map<String, String> =
    mapOf(
        msisdn.toHeaderPair(),
        SEGMENT_KEY to segment.toString(),
        BRAND_KEY to AccountBrandType.Postpaid.toString()
    )

@JsonClass(generateAdapter = true)
data class GetAccountDetailsResponse(
    val result: GetAccountDetailsResult
)

@JsonClass(generateAdapter = true)
data class GetAccountDetailsResult(
    val accountType: String,
    val accountTypeDescription: String?,
    val accountNumber: String,
    val mobileNumber: String?,
    val accountTitle: String?,
    val landlineNumber: String?,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val motherMaidenName: String?,
    val birthday: String?,
    val gender: String?,
    val alternativeMobileNumber: String?,
    val email: String?,
    val status: String,
    val statusDescription: String?,
    val statusReasonCode: String?,
    val statusReasonDescription: String?,
    val brand: String?,
    val source: String?,
    val customerId: String?,
    val customerAccountType: String?,
    val planDetails: PlanDetails?,
    val billingDetails: BillingDetails?,
)

@JsonClass(generateAdapter = true)
data class PlanDetails(
    val href: String
)

@JsonClass(generateAdapter = true)
data class BillingDetails(
    val href: String
)

fun String?.extractStatus(): AccountStatus? {
    return when (this) {
        ACTIVE_STATUS_DESCRIPTION -> {
            AccountStatus.Active
        }
        DISCONNECTED_STATUS_DESCRIPTION_1,
        DISCONNECTED_STATUS_DESCRIPTION_2,
        DISCONNECTED_STATUS_DESCRIPTION_3 -> {
            AccountStatus.Disconnected
        }
        INACTIVE_STATUS_DESCRIPTION_1,
        INACTIVE_STATUS_DESCRIPTION_2,
        INACTIVE_STATUS_DESCRIPTION_3 -> {
            AccountStatus.Inactive
        }
        else -> {
            // Search for the keywords
            val description = this ?: ""
            description.convertToAccountStatus()
        }
    }
}

private const val ACTIVE_STATUS_DESCRIPTION = "Active"

private const val DISCONNECTED_STATUS_DESCRIPTION_1 =
    "Collection Suspended/Temporarily Disconnected"
private const val DISCONNECTED_STATUS_DESCRIPTION_2 =
    "Suspended (Voluntary)/Temporarily Disconnected"
private const val DISCONNECTED_STATUS_DESCRIPTION_3 =
    "Collection suspended over voluntary suspend/Temporarily Disconnected"

private const val INACTIVE_STATUS_DESCRIPTION_1 = "Cancelled/Inactive"
private const val INACTIVE_STATUS_DESCRIPTION_2 = "Collection Cancelled/Inactive"
private const val INACTIVE_STATUS_DESCRIPTION_3 = "Terminated due to Change of Ownership/Inactive"
