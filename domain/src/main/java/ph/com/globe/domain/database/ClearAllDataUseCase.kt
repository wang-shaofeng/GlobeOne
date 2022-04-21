/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.database

import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    repoManager: ReposManager
) {

    private val enrolledAccountsRepo = repoManager.getEnrolledAccountsRepo()

    suspend fun execute() {
        enrolledAccountsRepo.deleteEnrolledAccounts()
        enrolledAccountsRepo.deleteMetadata()
    }
}
