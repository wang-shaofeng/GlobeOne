/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account_activities

import ph.com.globe.errors.account_activities.GetRewardsHistoryError
import ph.com.globe.model.account_activities.AccountRewards
import ph.com.globe.util.LfResult

interface AccountActivitiesDataManager {
    suspend fun getRewardsHistory(
        primaryMsisdn: String,
        dateFrom: String,
        dateTo: String,
        offset: Int,
        subscribeType: Int
    ): LfResult<AccountRewards, GetRewardsHistoryError>
}
