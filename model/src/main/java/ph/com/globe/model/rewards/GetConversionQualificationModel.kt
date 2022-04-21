/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountSegment

data class GetConversionQualificationParams(
    val mobileNumber: String,
    val brand: AccountBrand
)

@JsonClass(generateAdapter = true)
data class GetConversionQualificationModel(
    val result: GetConversionQualificationResult
)

@JsonClass(generateAdapter = true)
data class GetConversionQualificationResult(
    val qualifications: List<Qualification>?,
    val subscriber: Subscriber,
    val rate: Rate?,
    val targetCurrencyStatus: String
)

@JsonClass(generateAdapter = true)
data class Qualification(
    val qualificationId: String,
    val brand: String,
    val promoName: String,
    val wallet: String,
    val min: Int,
    val max: Int,
    val dataRemaining: Int
)

@JsonClass(generateAdapter = true)
data class Subscriber(
    val brand: String,
    val customerType: String,
    val customerSubType: String
)

@JsonClass(generateAdapter = true)
data class Rate(
    val sourceRate: Int,
    val targetRate: Int,
    val sourceCurrency: String,
    val targetCurrency: String
)

data class QualificationDetails(
    val enrolledAccount: EnrolledAccount,

    val accountName: String,
    val number: String,
    val brand: AccountBrand? = null,
    val segment: AccountSegment? = null,
    val promoName: String = "",
    val min: Int = 0,
    val max: Int = 0,
    val dataRemaining: Int = 0,
    val exchangeRate: Int = 0,
    val qualificationId: String = "",
    val rateId: String = "",
    val error: String = ""
)

