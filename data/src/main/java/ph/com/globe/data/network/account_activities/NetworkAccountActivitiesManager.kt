/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account_activities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.account_activities.AccountActivitiesDataManager
import ph.com.globe.errors.account_activities.GetRewardsHistoryError
import ph.com.globe.model.account_activities.AccountRewards
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkAccountActivitiesManager @Inject constructor(
    factory: AccountActivitiesComponent.Factory
) : AccountActivitiesDataManager {

    val component = factory.create()

    override suspend fun getRewardsHistory(
        primaryMsisdn: String,
        dateFrom: String,
        dateTo: String,
        offset: Int,
        subscribeType: Int

    ): LfResult<AccountRewards, GetRewardsHistoryError> =
        withContext(Dispatchers.IO) {
            component.provideGetRewardsHistoryNetworkCall().execute(
                primaryMsisdn,
                dateFrom,
                dateTo,
                offset,
                subscribeType
            )
        }
}
