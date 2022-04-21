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
import ph.com.globe.errors.account.GetSecurityAnswersError
import ph.com.globe.model.account.*
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetSecurityAnswersNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    suspend fun execute(params: GetSecurityAnswersParams): LfResult<List<SecurityAnswer>, GetSecurityAnswersError> {

        val response = kotlin.runCatching {
            authRetrofit.getSecurityAnswers(params.referenceId)
        }.fold(Response<GetSecurityAnswersResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetSecurityAnswersNetworkCall"
}

private fun NetworkError.toSpecific(): GetSecurityAnswersError {
    return GetSecurityAnswersError.General(GeneralError.Other(this))
}
