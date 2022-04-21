/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.auth.AuthDataManager
import javax.inject.Inject

class ForceLogoutUseCase @Inject constructor(
    private val authManager: AuthDataManager,
    repoManager: ReposManager
) {

    private val enrolledAccountsRepo = repoManager.getEnrolledAccountsRepo()

    suspend fun execute() {
        enrolledAccountsRepo.deleteEnrolledAccounts()
        enrolledAccountsRepo.deleteMetadata()
        authManager.removeUserData()
        authManager.sendLogoutEvent(false)
    }
}
