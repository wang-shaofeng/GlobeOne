/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetPrepaidPromoActiveSubscriptionError
import ph.com.globe.model.account.GetPrepaidPromoActiveSubscriptionRequest
import ph.com.globe.model.account.GetPrepaidPromoActiveSubscriptionResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetPrepaidPromoActiveSubscriptionUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(request: GetPrepaidPromoActiveSubscriptionRequest): LfResult<GetPrepaidPromoActiveSubscriptionResponse, GetPrepaidPromoActiveSubscriptionError> =
        accountManager.fetchOcsAccessToken().fold({ token ->
            accountManager.getPrepaidPromoActiveSubscription(token, request).fold({
                LfResult.success(it)
            }, {
                LfResult.failure(it)
            })
        }, {
            LfResult.failure(GetPrepaidPromoActiveSubscriptionError.OcsTokenError)
        })
}
