/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import com.google.gson.Gson
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

class CreateGCashPaymentSessionNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> {

        val body = CreateGCashPaymentSessionRequest(
            paymentType = params.paymentType,
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
            ),
            paymentInformation = GCashPaymentInformation(
                environmentInformation = EnvironmentInformation(
                    extendedInfo = params.takeIf { params.purchaseType is PurchaseType.BuyContentVoucher }
                        ?.let {
                            val extendedInfo = ExtendedInfo(params.transformToContentPortalData())
                            Gson().toJson(extendedInfo)
                        }
                ),
                order = Order(
                    when (val purchaseType = params.purchaseType) {
                        is PurchaseType.BuyLoadRetailer -> "Retailer"
                        is PurchaseType.BuyLoadConsumer -> "Consumer"
                        is PurchaseType.BuyGoCreatePromo -> purchaseType.displayName
                        is PurchaseType.BuyPromo -> purchaseType.displayName
                        is PurchaseType.BuyContentVoucher -> purchaseType.displayName
                        is PurchaseType.PayBill -> "string"
                        else -> ""
                    }
                ),
                subMerchantName = (params.purchaseType as? PurchaseType.BuyContentVoucher)?.denomCategory
            )
        )

        val response = kotlin.runCatching {
            paymentRetrofit.createGCashPaymentSession(
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

    override val logTag = "CreateGCashPaymentSessionNetworkCall"
}

private fun NetworkError.toSpecific() = PaymentError.General(Other(this))
