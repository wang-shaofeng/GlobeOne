/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account_activities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.account_activities.di.AccountActivitiesComponent
import ph.com.globe.errors.account_activities.GetRewardsHistoryError
import ph.com.globe.model.account_activities.AccountRewardsTransaction
import ph.com.globe.util.LfResult
import javax.inject.Inject

class AccountActivitiesUseCaseManager @Inject constructor(
    factory: AccountActivitiesComponent.Factory
) : AccountActivitiesDomainManager {

    val component = factory.create()

    override suspend fun getRewardsHistory(
        primaryMsisdn: String,
        dateFrom: String,
        dateTo: String,
        subscriberType: Int
    ): LfResult<List<AccountRewardsTransaction>, GetRewardsHistoryError> =
        withContext(Dispatchers.IO) {
            component.provideGetRewardsHistoryUseCase()
                .execute(primaryMsisdn, dateFrom, dateTo, subscriberType)
        }
}
