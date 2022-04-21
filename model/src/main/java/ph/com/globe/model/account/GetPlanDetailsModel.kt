/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.AccountBrandType.Postpaid
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.BRAND_KEY
import ph.com.globe.model.util.brand.SEGMENT_KEY

data class GetPlanDetailsParams(
    val msisdn: String,
    val segment: AccountSegment,
    val referenceId: String? = null
)

fun GetPlanDetailsParams.toQueryMap(): Map<String, String> =
    mapOf(
        msisdn.toHeaderPair(),
        SEGMENT_KEY to segment.toString(),
        BRAND_KEY to Postpaid.toString()
    )

@JsonClass(generateAdapter = true)
data class GetMobilePlanDetailsResponse(
    val result: GetMobilePlanDetailsResult
)

@JsonClass(generateAdapter = true)
data class GetBroadbandPlanDetailsResponse(
    val result: GetBroadbandPlanDetailsResult
)

@JsonClass(generateAdapter = true)
data class GetMobilePlanDetailsResult(
    val cxsAccountId: String,
    val plan: MobilePlanDetails,
    val sim: MobileSim?,
    val configuration: MobileConfiguration?,
    val gadget: MobileGadget?,
    val allowance: List<MobileAllowance>?,
    val freebies: MobileFreebie?,
)

@JsonClass(generateAdapter = true)
data class GetBroadbandPlanDetailsResult(
    val cxsAccountId: String,
    val plan: BroadbandPlanDetails,
)

@JsonClass(generateAdapter = true)
data class BroadbandPlanDetails(
    val accountNumber: String?,
    val landlineNumber: String?,
    val mpid: String,
    val planName: String,
    // val contractExpiryDate: Date?,
    val contractDuration: String?,
    val monthsRemaining: String?,
    val creditLimit: String?,
    val spendingLimit: String?,
    val bandwidth: String?,
    val unitOfMeasure: String?,
    val price: String?,
    val isUnli: String?,
    val isForVolumeBoost: String?,
    val planType: String?,
    val hasVolumeBoost: String?,
    // val nextRenewalDate: Date?,
    val hasDeviceAddons: String?,
    val productCategory: String?
)

@JsonClass(generateAdapter = true)
data class MobilePlanDetails(
    val mobileNumber: String,
    val landlineNumber: String?,
    val landlineNumberZone: String?,
    val billingId: String,
    val planId: String,
    val planName: String,
    val planType: String,
    val creditLimit: String,
    val spendingLimit: String,
)

@JsonClass(generateAdapter = true)
data class MobileSim(
    val brand: String,
    val subBrand: String,
    val serialNumber: String,
)

@JsonClass(generateAdapter = true)
data class MobileConfiguration(
    val mobileData: MobileConfigData?,
    val roaming: MobileConfigRoaming?,
    val voice: MobileConfigVoice?,
    val mms: MobileConfigMMS?,
)

@JsonClass(generateAdapter = true)
data class MobileConfigData(
    val isNonLteDataEnabled: String?,
    val hasLteDataProv: String,
)

@JsonClass(generateAdapter = true)
data class MobileConfigRoaming(
    val hasRoaming: String?,
    val hasInternationalOutgoingVoice: String?,
    val hasInternationalIncomingVoice: String?,
)

@JsonClass(generateAdapter = true)
data class MobileConfigVoice(
    val hasCallerId: String?,
    val hasLocalIncomingVoice: String?,
    val hasLocalOutgoingVoice: String?,
    val canForwardCall: String?,
    val canWaitCall: String?,
    val canHoldCall: String?,
    val hasMultipartyTeleconferencing: String?,
    val has3gVideoCall: String?,
    val hasRingBackTone: String?,
)

@JsonClass(generateAdapter = true)
data class MobileConfigMMS(
    val hasIncomingSms: String?,
    val hasOutgoingSms: String?,
    val hasMmsLteProv: String?,
)

@JsonClass(generateAdapter = true)
data class MobileGadget(
    val lockInStartDate: String?,
    val lockInEndDate: String?,
    val lockInDuration: String?,
    @Json(name = "package") // explicit naming due to package being a keyword in kotlin
    val package_: List<MobileGadgetPackage>?,
)

@JsonClass(generateAdapter = true)
data class MobileGadgetPackage(
    val type: String?,
    val description: String?,
    val devices: List<MobileGadgetPackageDevice>?,
)

@JsonClass(generateAdapter = true)
data class MobileGadgetPackageDevice(
    val name: String?,
    val brand: String?,
    val type: String?,
)

@JsonClass(generateAdapter = true)
data class MobileAllowance(
    val type: String?,
    val planId: String?,
    val planName: String?,
    val unitOfMeasure: String?,
    val quota: String?,
    val utilizedQuota: String?,
    val remainingQuota: String?,
)

@JsonClass(generateAdapter = true)
data class MobileFreebie(
    val billingId: String?,
    val offerId: String?,
    val offerName: String?,
)
