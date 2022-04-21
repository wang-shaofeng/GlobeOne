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
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.model.account.*
import ph.com.globe.model.auth.GetSecurityQuestionsParams
import ph.com.globe.model.auth.GetSecurityQuestionsResponse
import ph.com.globe.model.auth.GetSecurityQuestionsResult
import ph.com.globe.model.auth.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetSecurityQuestionsNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    suspend fun execute(params: GetSecurityQuestionsParams): LfResult<GetSecurityQuestionsResult, GetSecurityQuestionsError> {

        val response = kotlin.runCatching {
            authRetrofit.getSecurityQuestions(params.toQueryMap())
        }.fold(Response<GetSecurityQuestionsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetSecurityQuestionsNetworkCall"
}

private fun NetworkError.toSpecific(): GetSecurityQuestionsError {
    when (this) {
        is NetworkError.Http -> {
            if (this.errorResponse?.error?.code == "50202" && this.errorResponse?.error?.details == "The customer is not allowed to generate security questions at the current moment.")
                return GetSecurityQuestionsError.CannotGetSecurityQuestions
        }
        else -> Unit
    }
    return GetSecurityQuestionsError.General(GeneralError.Other(this))
}
