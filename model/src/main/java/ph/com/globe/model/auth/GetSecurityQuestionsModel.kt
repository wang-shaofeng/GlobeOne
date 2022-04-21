/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.toHeaderPair
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.BRAND_KEY
import ph.com.globe.model.util.brand.SEGMENT_KEY
import java.io.Serializable

data class GetSecurityQuestionsParams(
    val msisdn: String,
    val segment: AccountSegment,
    val brand: AccountBrandType,
    val categoryIdentifier: String
)

fun GetSecurityQuestionsParams.toQueryMap(): Map<String, String> = mapOf(
    msisdn.toHeaderPair(),
    SEGMENT_KEY to segment.toString(),
    "categoryIdentifier" to categoryIdentifier,
    BRAND_KEY to brand.toString()
)

@JsonClass(generateAdapter = true)
data class GetSecurityQuestionsResponse(
    val result: GetSecurityQuestionsResult
)

@JsonClass(generateAdapter = true)
data class GetSecurityQuestionsResult(
    val referenceId: String,
    val securityQuestions: List<SecurityQuestion>,
)

@JsonClass(generateAdapter = true)
data class SecurityQuestion(
    val questionId: String,
    val question: String,
    val fieldType: String?,
    val placeholderText: String?,
) : Serializable
