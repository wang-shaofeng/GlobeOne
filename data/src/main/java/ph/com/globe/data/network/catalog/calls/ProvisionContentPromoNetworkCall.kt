/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.catalog.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.catalog.CatalogRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.catalog.ProvisionContentPromoError
import ph.com.globe.model.catalog.ProvisionContentPromoParams
import ph.com.globe.model.catalog.ProvisionContentPromoRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class ProvisionContentPromoNetworkCall @Inject constructor(
    private val catalogRetrofit: CatalogRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: ProvisionContentPromoParams): LfResult<Unit, ProvisionContentPromoError> {

        val headers = tokenRepository.createHeaderWithReferenceId(params.otpReferenceId)
            .successOrNull() // if we don't have the referenceId and we are not logged in, we will return NotLoggedIn error
            ?: return LfResult.failure(ProvisionContentPromoError.General(GeneralError.NotLoggedIn))

        val request = ProvisionContentPromoRequest(
            mobileNumber = params.mobileNumber,
            serviceId = params.serviceId
        )

        val response = kotlin.runCatching {
            catalogRetrofit.provisionContentPromo(
                headers.plus("Source" to "CXS"),
                request
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

    override val logTag = "ProvisionContentPromoNetworkCall"
}

private fun NetworkError.toSpecific() = ProvisionContentPromoError.General(GeneralError.Other(this))
