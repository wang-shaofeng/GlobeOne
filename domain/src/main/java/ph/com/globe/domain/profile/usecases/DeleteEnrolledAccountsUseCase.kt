/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class DeleteEnrolledAccountsUseCase @Inject constructor(repoManager: ReposManager) {

    private val enrolledAccountsRepo = repoManager.getEnrolledAccountsRepo()

    suspend fun execute() = enrolledAccountsRepo.deleteEnrolledAccounts()

}
