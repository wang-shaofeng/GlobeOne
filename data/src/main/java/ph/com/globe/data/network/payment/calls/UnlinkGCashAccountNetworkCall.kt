/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.payment.PaymentRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.LinkingGCashError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class UnlinkGCashAccountNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError> {

        val headers =
            tokenRepository.createAuthenticatedHeader().fold({
                it
            }, {
                return LfResult.failure(LinkingGCashError.General(GeneralError.NotLoggedIn))
            })

        val response = kotlin.runCatching {
            paymentRetrofit.unlinkGCashAccount(
                headers,
                LinkingGCashAccountRequest(params.accountAlias)
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

    override val logTag = "UnlinkGCashAccountNetworkCall"
}

private fun NetworkError.toSpecific() = LinkingGCashError.General(Other(this))
