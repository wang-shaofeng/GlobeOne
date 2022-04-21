/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.payment.PaymentRetrofit
import ph.com.globe.data.network.payment.model.*
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.PaymentError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class CreateAdyenPaymentSessionNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> {

        val body = CreateAdyenPaymentSessionRequest(
            paymentType = params.paymentType,
            paymentInformation = AdyenPaymentInformation(
                responseUrl = params.returnUrl,
                entityType = when (params.purchaseType) {
                    is PurchaseType.BuyLoadRetailer -> "Retailer"
                    is PurchaseType.BuyLoadConsumer -> "Consumer"
                    is PurchaseType.BuyGoCreatePromo -> (params.purchaseType as PurchaseType.BuyGoCreatePromo).displayName
                    is PurchaseType.BuyPromo -> (params.purchaseType as PurchaseType.BuyPromo).displayName
                    else -> null
                }
            ),
            settlementInformation = listOf(
                SettlementInformation(
                    accountNumber = if (params.requestType == "PayBills") params.accountNumber else null,
                    mobileNumber = if (params.requestType == "PayBills") null else params.mobileNumber,
                    emailAddress = if (params.emailAddress.isNullOrEmpty()) null else params.emailAddress,
                    transactionType = params.transactionType,
                    requestType = params.requestType,
                    amount = params.amountAfterDiscount ?: params.price.toDouble(),
                    transactions = if (params.requestType == "PayBills") null else params.transformToTransaction()
                )
            )
        )
        val response = kotlin.runCatching {
            paymentRetrofit.createAdyenPaymentSession(
                tokenRepository.createHeaderForPaymentSession(),
                body
            )
        }.fold(Response<CreatePaymentSessionResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(CreatePaymentSessionResult.CreatePaymentSessionSuccess(it.result.tokenPaymentId))
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "CreateAdyenPaymentSessionNetworkCall"
}

private fun NetworkError.toSpecific() = PaymentError.General(Other(this))
