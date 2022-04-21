/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.app_data.usecases

import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class ClearDatabaseUseCase @Inject constructor(
    repoManager: ReposManager
) {

    private val enrolledAccountsRepo = repoManager.getEnrolledAccountsRepo()
    private val registeredUserRepo = repoManager.getRegisteredUserRepo()
    private val accountGroupsRepo = repoManager.getAccountGroupsRepo()
    private val subscriptionUsageRepo = repoManager.getSubscriptionUsagesRepo()

    suspend fun execute() {
        enrolledAccountsRepo.deleteEnrolledAccounts()
        enrolledAccountsRepo.deleteMetadata()
        registeredUserRepo.deleteRegisteredUser()
        registeredUserRepo.deleteMetadata()
        accountGroupsRepo.deleteAllAccountsGroups()
        accountGroupsRepo.deleteMetadata()
        subscriptionUsageRepo.deleteAllAccountsSubscriptionUsages()
        subscriptionUsageRepo.deleteMetadata()
    }
}
