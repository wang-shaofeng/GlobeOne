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
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.CreateServiceOrderError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class CreateServiceOrderLoadNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError> {

        val headers = tokenRepository.createHeaderWithSessionCredentials()
            .plus(tokenRepository.createHeaderWithContentType())

        var referenceId: String? = null
        val response = when (params.purchaseType) {
            is PurchaseType.BuyLoadConsumer -> {
                kotlin.runCatching {
                    paymentRetrofit.createServiceOrderLoad(
                        headers,
                        CreateServiceOrderLoadRequest(
                            params.sourceNumber,
                            params.targetNumber,
                            (params.purchaseType as PurchaseType.BuyLoadConsumer).amount.toDouble()
                        )
                    )
                }.fold(
                    {
                        referenceId = it.headers().get("x-provider-transaction-id")
                        it.toEmptyLfSdkResult()
                    }, {
                        it.toLFSdkResult()
                    })
            }
            is PurchaseType.BuyLoadRetailer -> {
                return LfResult.failure(CreateServiceOrderError.InvalidParameters)
            }
            else -> {
                return LfResult.failure(CreateServiceOrderError.InvalidParameters)
            }
        }
        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(CreateServiceIdResult(referenceId ?: ""))
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "CreateServiceOrderLoadNetworkCall"
}

private fun NetworkError.toSpecific() = CreateServiceOrderError.General(Other(this))
