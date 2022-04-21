/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetPrepaidPromoSubscriptionUsageError
import ph.com.globe.model.account.network_models.DataItemJson
import ph.com.globe.model.account.network_models.PromoSubscriptionUsageResult
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageParams
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetPrepaidPromoSubscriptionUsageUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<PromoSubscriptionUsageResult, GetPrepaidPromoSubscriptionUsageError> =
        accountManager.fetchOcsAccessToken().fold({ token ->
            accountManager.getPrepaidPromoSubscriptionUsage(
                GetPrepaidPromoSubscriptionUsageParams(
                    token,
                    request
                )
            ).fold({
                val data = mutableListOf<DataItemJson>()

                it.promoSubscriptionUsage.mainData?.let {
                    data.addAll(it)
                }

                it.promoSubscriptionUsage.appData?.let {
                    data.addAll(it)
                }

                LfResult.success(PromoSubscriptionUsageResult(data))
            }, {
                LfResult.failure(it)
            })
        }, {
            LfResult.failure(GetPrepaidPromoSubscriptionUsageError.OcsTokenError)
        })
}
