/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rewards.RewardsRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rewards.AddDataConversionError
import ph.com.globe.model.rewards.AddDataConversionRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import javax.inject.Inject

class AddDataConversionNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(requestBody: AddDataConversionRequest): LfResult<String, AddDataConversionError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(AddDataConversionError.General(GeneralError.NotLoggedIn))
        }

        var conversionId = ""

        val response = kotlin.runCatching {
            rewardsRetrofit.addDataConversion(headers, requestBody)
        }.fold({
            it.headers().get("Location")?.let { locationHeader ->
                conversionId = locationHeader.substringAfterLast("/")
            }

            it.toLfSdkResult()
        }, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(conversionId)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "AddDataConversionNetworkCall"
}

private fun NetworkError.toSpecific(): AddDataConversionError {
    return AddDataConversionError.General(GeneralError.Other(this))
}
