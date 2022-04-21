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
import ph.com.globe.data.network.util.createAuthenticatedHeader
import ph.com.globe.data.network.util.logFailedToCreateAuthHeader
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.catalog.ContentSubscriptionStatusError
import ph.com.globe.model.catalog.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class ContentSubscriptionStatusNetworkCall @Inject constructor(
    private val catalogRetrofit: CatalogRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: ContentSubscriptionStatusParams): LfResult<ContentSubscriptionStatusResult, ContentSubscriptionStatusError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(ContentSubscriptionStatusError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            catalogRetrofit.getContentSubscriptionStatus(
                headers.plus("Channel" to "1202"),
                params.serviceId,
                params.toQueryMap()
            )
        }.fold(Response<ContentSubscriptionStatusResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "ContentSubscriptionsNetworkCall"
}

private fun NetworkError.toSpecific() = ContentSubscriptionStatusError.General(GeneralError.Other(this))
