/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.*
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.StringAsAccountBrandType
import ph.com.globe.model.util.brand.StringAsAccountSegment
import java.io.Serializable

data class ValidateSecurityAnswersParams(
    val referenceId: String,
    val categoryIdentifier: List<String>,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val msisdn: String,
    val securityAnswers: List<SecurityAnswers>
)

@JsonClass(generateAdapter = true)
data class ValidateSecurityAnswersRequest(
    val referenceId: String,
    val categoryIdentifier: List<String>,
    @StringAsAccountBrandType
    val brand: AccountBrandType,
    @StringAsAccountSegment
    val segment: AccountSegment,
    val mobileNumber: String?,
    val accountNumber: String?,
    val landlineNumber: String?,
    val securityQuestions: List<SecurityAnswers>,
)

@JsonClass(generateAdapter = true)
data class SecurityAnswers(
    val questionId: String,
    val answer: String,
) : Serializable

fun createSecurityAnswers(
    answers: List<String?>,
    questionIds: List<String?>
): List<SecurityAnswers> =
    answers.zip(questionIds) { answer, questionId ->
        if (!answer.isNullOrEmpty() && !questionId.isNullOrEmpty()) SecurityAnswers(
            questionId,
            answer
        ) else null
    }.filterNotNull()

fun ValidateSecurityAnswersParams.toValidateSecurityAnswersRequest(): ValidateSecurityAnswersRequest =
    ValidateSecurityAnswersRequest(
        accountNumber = msisdn.takeIf { it.isAccountNumber() },
        landlineNumber = msisdn.takeIf { it.isLandlineNumber() },
        mobileNumber = msisdn.takeIf { it.isMobileNumber() },
        categoryIdentifier = categoryIdentifier,
        brand = brandType,
        segment = segment,
        referenceId = referenceId,
        securityQuestions = securityAnswers
    )
