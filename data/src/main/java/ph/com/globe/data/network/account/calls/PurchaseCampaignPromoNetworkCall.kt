/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.account.PurchaseCampaignPromoError
import ph.com.globe.model.account.CampaignPromoRequestModel
import ph.com.globe.model.account.CampaignPromoResponseModel
import ph.com.globe.model.payment.MultiplePurchasePromoResult
import ph.com.globe.model.payment.PurchaseResult
import ph.com.globe.model.payment.TransactionResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class PurchaseCampaignPromoNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {
    suspend fun execute(
        channel: String,
        mobileNumber: String,
        customParam1: String,
        maId: String,
        availMode: Int
    ): LfResult<PurchaseResult, PurchaseCampaignPromoError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(PurchaseCampaignPromoError.General(GeneralError.NotLoggedIn))
        } + tokenRepository.createHeaderWithContentType()

        val result = runCatching {
            accountRetrofit.purchaseCampaignPromo(
                headers,
                CampaignPromoRequestModel(mobileNumber, channel, customParam1, maId),
                availMode
            )
        }.fold(Response<CampaignPromoResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return result.fold({
            logSuccessfulNetworkCall()
            LfResult.success(
                PurchaseResult.GeneralResult(
                    MultiplePurchasePromoResult(
                        listOf(
                            TransactionResult(
                                transactionId = it.result.transactionId,
                                serviceID = null,
                                keyword = null,
                                param = null,
                                price = "0",
                                status = "SUCCESS"
                            )
                        )
                    )
                )
            )
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(PurchaseCampaignPromoError.General(GeneralError.Other(it)))
        })
    }

    override val logTag: String = "PullFreebieNetworkCall"
}
