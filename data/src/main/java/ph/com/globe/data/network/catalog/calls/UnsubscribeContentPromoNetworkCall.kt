/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.catalog.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.catalog.CatalogRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.catalog.UnsubscribeContentPromoError
import ph.com.globe.model.catalog.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class UnsubscribeContentPromoNetworkCall @Inject constructor(
    private val catalogRetrofit: CatalogRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: UnsubscribeContentPromoParams): LfResult<Unit, UnsubscribeContentPromoError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(UnsubscribeContentPromoError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            catalogRetrofit.unsubscribeContentPromo(
                headers.plus("Source" to "CXS"),
                UnsubscribeContentPromoRequest(
                    params.mobileNumber,
                    params.serviceId
                )
            )
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

    override val logTag = "UnsubscribeContentPromoNetworkCall"
}

private fun NetworkError.toSpecific() = UnsubscribeContentPromoError.General(GeneralError.Other(this))
