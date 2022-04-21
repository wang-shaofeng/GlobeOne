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
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.onSuccess
import javax.inject.Inject

class CheckPaymentSuccessfulUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager,
    private val reposManager: ReposManager
) :
    HasLogTag {

    suspend fun execute(thirdPartyResult: ThirdPartyPaymentResult): LfResult<GetPaymentSessionResult, PaymentError> {

        LfNetworkCallPollHandler(
            GetPaymentSessionParams(reposManager.getPaymentParametersRepo().getTokenPaymentId()),
            { params -> paymentManager.getPaymentSession(params as GetPaymentSessionParams) },
            { result ->
                (result as GetPaymentSessionResult).isPurchaseCompleted(thirdPartyResult)
            }
        ).poll().onSuccess { result ->
            dLog(" polling successful.")
            return when {
                result.isPaymentUnsuccessful(thirdPartyResult) -> LfResult.failure(PaymentError.PaymentFailed)
                else -> LfResult.success(result)
            }
        }
        dLog(" polling unsuccessful.")
        return LfResult.failure(PaymentError.PollingFailed)
    }

    override val logTag = "CheckPaymentSuccessfulUseCase"

}

private fun GetPaymentSessionResult.isPurchaseCompleted(thirdPartyResult: ThirdPartyPaymentResult): Boolean =
    accounts[0].checkIsCompleted() && thirdPartyResult.isFinal()

private fun GetPaymentSessionResult.isPaymentUnsuccessful(thirdPartyResult: ThirdPartyPaymentResult): Boolean =
    accounts[0].status != thirdPartyResult.result
