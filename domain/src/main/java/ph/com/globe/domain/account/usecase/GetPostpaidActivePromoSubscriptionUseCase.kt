/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetPostpaidActivePromoSubscriptionError
import ph.com.globe.model.account.GetPostpaidActivePromoSubscriptionRequest
import ph.com.globe.model.account.GetPostpaidActivePromoSubscriptionResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetPostpaidActivePromoSubscriptionUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(request: GetPostpaidActivePromoSubscriptionRequest): LfResult<GetPostpaidActivePromoSubscriptionResponse, GetPostpaidActivePromoSubscriptionError> =
        accountManager.fetchOcsAccessToken().fold({ token ->
            accountManager.getPostpaidActivePromoSubscription(token, request)
        }, {
            LfResult.failure(GetPostpaidActivePromoSubscriptionError.OcsTokenError)
        })
}
