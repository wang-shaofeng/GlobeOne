/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.ValidateSecurityAnswersError
import ph.com.globe.model.account.*
import ph.com.globe.model.auth.ValidateSecurityAnswersParams
import ph.com.globe.model.auth.toValidateSecurityAnswersRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class ValidateSecurityAnswersNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    suspend fun execute(params: ValidateSecurityAnswersParams): LfResult<Unit, ValidateSecurityAnswersError> {

        val response = kotlin.runCatching {
            authRetrofit.validateSecurityAnswers(params.toValidateSecurityAnswersRequest())
        }.fold(Response<Unit?>::toEmptyLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(Unit)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "ValidateSecurityAnswers"
}

private fun NetworkError.toSpecific(): ValidateSecurityAnswersError {
    when (this) {
        is NetworkError.Http -> {
            if (errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "Valid security answers are insufficient.")
                return ValidateSecurityAnswersError.SecurityAnswersInsufficient(
                    incorrectAnswersIds = errorResponse?.moreInfo?.split(",") ?: listOf()
                )
            if (errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "The customer has already reached the max attempt value for validating security answers.")
                return ValidateSecurityAnswersError.MaxAttemptsReached
        }
        else -> Unit
    }
    return ValidateSecurityAnswersError.General(GeneralError.Other(this))
}

