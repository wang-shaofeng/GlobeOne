package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.payment.ConfirmAdyenPaymentError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class ConfirmAdyenPaymentUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager,
    private val reposManager: ReposManager
) : HasLogTag {
    suspend fun execute(actionComponentJson: String): LfResult<ConfirmAdyenPaymentResult, ConfirmAdyenPaymentError> {
        val tokenId = reposManager.getPaymentParametersRepo().getTokenPaymentId()
        val merchantAccount = reposManager.getPaymentParametersRepo().getMerchantAccount()
        return paymentManager.paymentService(
            PaymentServiceParams(
                name = "DropinPaymentDetails",
                actionComponentJson = actionComponentJson,
                paymentTokenId = tokenId,
                merchantAccount = merchantAccount
            )
        )
            .fold({ result ->
                return@fold LfResult.success(
                    ConfirmAdyenPaymentResult.SubmitAdyenPaymentFinished(
                        resultCode = result.data.resultCode?.toAdyenResult()
                    )
                )
            }, {
                LfResult.failure(ConfirmAdyenPaymentError.General)
            })
    }

    override val logTag: String = "ConfirmAdyenPaymentUseCase"
}
