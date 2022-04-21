package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.domain.utils.LfNetworkCallPollHandler
import ph.com.globe.errors.payment.SubmitAdyenPaymentError
import ph.com.globe.errors.payment.toSubmitAdyenPaymentError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.onSuccess
import javax.inject.Inject

class SubmitAdyenPaymentUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager,
    private val reposManager: ReposManager
) : HasLogTag {
    suspend fun execute(paymentComponentJson: String): LfResult<SubmitAdyenPaymentResult, SubmitAdyenPaymentError> {
        val tokenId = reposManager.getPaymentParametersRepo().getTokenPaymentId()
        val merchantAccount = reposManager.getPaymentParametersRepo().getMerchantAccount()
        return paymentManager.paymentService(
            PaymentServiceParams(
                name = "DropinPaymentDataCreated",
                paymentComponentJson = paymentComponentJson,
                paymentTokenId = tokenId,
                merchantAccount = merchantAccount
                //  [workaround]
                // TODO merchantAccount = "GlobePH"
            )
        )
            .fold({
                val getSessionParams = GetPaymentSessionParams(tokenId)

                LfNetworkCallPollHandler(
                    getSessionParams,
                    { params ->
                        paymentManager.getPaymentSession(params as GetPaymentSessionParams)
                    }, { result ->
                        (result as GetPaymentSessionResult).isAdyenTransactionCompleted()
                    }
                ).poll().onSuccess { result ->
                    dLog(" polling successful.")
                    when {
                        result.containsActionObject() -> {
                            return@fold LfResult.success(
                                SubmitAdyenPaymentResult.SubmitAdyenPaymentSuccessNeedAction(
                                    actionObject = result.paymentResult?.action!!,
                                )
                            )
                        }
                        result.isAdyenTransactionCompleted() -> {
                            return@fold LfResult.success(
                                SubmitAdyenPaymentResult.SubmitAdyenPaymentFinished(
                                    resultCode = result.accounts.first().status.toAdyenResult()
                                )
                            )
                        }
                        else -> {
                            return@fold handlePollingFailed()
                        }
                    }
                }
                return handlePollingFailed()
            }, {
                LfResult.failure(it.toSubmitAdyenPaymentError())
            })
    }

    private fun handlePollingFailed(): LfResult<SubmitAdyenPaymentResult, SubmitAdyenPaymentError> =
        LfResult.failure(SubmitAdyenPaymentError.General.apply {
            dLog(" polling unsuccessful.")
        })

    override val logTag: String = "SubmitAdyenPaymentUseCase"
}
