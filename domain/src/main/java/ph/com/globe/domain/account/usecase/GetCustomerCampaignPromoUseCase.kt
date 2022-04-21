/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import kotlinx.coroutines.*
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.account.GetCustomerCampaignPromoError
import ph.com.globe.model.account.AvailableCampaignPromosModel
import ph.com.globe.model.account.GetCustomerCampaignParams
import ph.com.globe.model.personalized_campaign.PersonalizedCampaignConfig
import ph.com.globe.util.LfResult
import ph.com.globe.util.onSuccess
import ph.com.globe.util.successOrErrorAction
import javax.inject.Inject

class GetCustomerCampaignPromoUseCase @Inject constructor(
    private val accountDataManager: AccountDataManager
) {
    suspend fun execute(
        phoneNumber: String,
        segment: String,
        channels: List<PersonalizedCampaignConfig>
    ): LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError> =
        withContext(Dispatchers.IO) {
            val asyncList =
                mutableListOf<Deferred<LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError>>>()

            val resultList = mutableListOf<AvailableCampaignPromosModel>()

            channels.forEach {
                asyncList.add(async {
                    accountDataManager.getCustomerCampaignPromo(
                        GetCustomerCampaignParams(
                            phoneNumber,
                            it.campaign_id,
                            segment,
                            it.get_mode,
                            it.custom_attrib
                        )
                    )
                })
            }

            for (item in asyncList) {
                item.await().onSuccess {
                    resultList.addAll(it)
                }
            }

            return@withContext LfResult.success(resultList)
        }
}
