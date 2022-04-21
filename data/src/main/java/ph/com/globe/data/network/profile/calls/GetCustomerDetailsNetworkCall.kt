/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.createHeaderWithReferenceId
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.GetCustomerDetailsError
import ph.com.globe.model.profile.response_models.CustomerDetails
import ph.com.globe.model.profile.response_models.GetCustomerDetailsParams
import ph.com.globe.model.profile.response_models.GetCustomerDetailsResponse
import ph.com.globe.model.profile.response_models.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetCustomerDetailsNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetCustomerDetailsParams): LfResult<CustomerDetails, GetCustomerDetailsError> {

        val headers = tokenRepository.createHeaderWithReferenceId(params.otpReferenceId)
            .successOrNull() // if we don't have the referenceId and we are not logged in, we will return NotLoggedIn error
            ?: return LfResult.failure(GetCustomerDetailsError.General(GeneralError.NotLoggedIn))

        val response = kotlin.runCatching {
            profileRetrofit.getCustomerDetails(
                headers,
                params.toQueryMap()
            )
        }.fold(Response<GetCustomerDetailsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetCustomerDetailsNetworkCall"
}

private fun NetworkError.toSpecific() = GetCustomerDetailsError.General(GeneralError.Other(this))
