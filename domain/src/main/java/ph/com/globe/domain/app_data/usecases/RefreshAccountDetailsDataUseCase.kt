/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.app_data.usecases

import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class RefreshAccountDetailsDataUseCase @Inject constructor(reposManager: ReposManager) {

    private val accountGroupsRepo = reposManager.getAccountGroupsRepo()
    private val accountSubscriptionUsagesRepo = reposManager.getSubscriptionUsagesRepo()

    suspend fun execute(msisdn: String) {
        accountGroupsRepo.refreshAccountGroups(msisdn)
        accountSubscriptionUsagesRepo.refreshAccountSubscriptionUsages(msisdn)
    }
}
