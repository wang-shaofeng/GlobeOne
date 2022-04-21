/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account_activities

import ph.com.globe.errors.account_activities.GetRewardsHistoryError
import ph.com.globe.model.account_activities.AccountRewardsTransaction
import ph.com.globe.util.LfResult
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

class GetRewardsHistoryUseCase @Inject constructor(private val accountActivitiesDataManager: AccountActivitiesDataManager) {
    suspend fun execute(
        primaryMsisdn: String,
        dateFrom: String,
        dateTo: String,
        subscriberType: Int
    ): LfResult<List<AccountRewardsTransaction>, GetRewardsHistoryError> {
        var offset = 0
        val list = mutableListOf<AccountRewardsTransaction>()
        var continueFetching = true

        while (continueFetching) {
            accountActivitiesDataManager.getRewardsHistory(
                primaryMsisdn,
                dateFrom,
                dateTo,
                offset,
                subscriberType
            ).onSuccess {
                if (it.accountRewardsTransactions.isNotEmpty()) {
                    list.addAll(it.accountRewardsTransactions)
                    offset =
                        (it.page?.pageFirstResult ?: 0) + (it.page?.totalRecordCount ?: 0)
                } else {
                    continueFetching = false
                }
            }.onFailure {
                return LfResult.failure(it)
            }
        }

        list.sortByDescending { it.date }
        return LfResult.success(list)
    }
}
