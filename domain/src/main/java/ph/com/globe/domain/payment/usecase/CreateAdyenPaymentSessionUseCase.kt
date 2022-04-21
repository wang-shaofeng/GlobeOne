/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.domain.utils.LfNetworkCallPollHandler
import ph.com.globe.errors.payment.PaymentError
import ph.com.globe.model.payment.CreatePaymentSessionParams
import ph.com.globe.model.payment.CreatePaymentSessionResult
import ph.com.globe.model.payment.GetPaymentSessionParams
import ph.com.globe.model.payment.GetPaymentSessionResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.onSuccess
import javax.inject.Inject

class CreateAdyenPaymentSessionUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager,
    private val reposManager: ReposManager
) : HasLogTag {

    suspend fun execute(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> {
        return paymentManager.createAdyenPaymentSession(params).fold(
            {
                reposManager.getPaymentParametersRepo().setTokenPaymentId(
                    (it as CreatePaymentSessionResult.CreatePaymentSessionSuccess).token
                )

                val getSessionParams = GetPaymentSessionParams(it.token)

                LfNetworkCallPollHandler(
                    getSessionParams,
                    { params ->
                        paymentManager.getPaymentSession(params as GetPaymentSessionParams)
                    }, { result ->
                        (!(result as GetPaymentSessionResult).paymentMethods.isNullOrEmpty())
                    }
                ).poll().onSuccess { result ->
                    dLog(" polling successful.")
                    reposManager.getPaymentParametersRepo().setMerchantAccount(
                        result.merchantAccount ?: return handlePollingFailed()
                    )
                    return@fold LfResult.success(
                        CreatePaymentSessionResult.CreateAdyenDropInSessionSuccess(
                            createPaymentSessionResult = it,
                            paymentMethods = result.paymentMethods
                                ?: return handlePollingFailed()
                        )
                    )
                }

                return handlePollingFailed()
            },
            {
                LfResult.failure(it)
            }
        )
    }

    private fun handlePollingFailed(): LfResult<CreatePaymentSessionResult, PaymentError> =
        LfResult.failure(PaymentError.PollingFailed.apply {
            dLog(" polling unsuccessful.")
        })

    override val logTag = "CreateAdyenPaymentSessionUseCase"
}
