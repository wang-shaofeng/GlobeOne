/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.PurchaseCampaignPromoError
import ph.com.globe.model.payment.PurchaseResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class PurchaseCampaignPromoUseCase @Inject constructor(private val accountDataManager: AccountDataManager) {
    suspend fun execute(
        channel: String,
        mobileNumber: String,
        customParam1: String,
        maId: String,
        availMode: Int
    ): LfResult<PurchaseResult, PurchaseCampaignPromoError> =
    accountDataManager.purchaseCampaignPromo(channel, mobileNumber, customParam1, maId, availMode)
}
