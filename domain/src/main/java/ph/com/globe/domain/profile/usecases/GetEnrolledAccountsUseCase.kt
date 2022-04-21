/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import kotlinx.coroutines.flow.Flow
import ph.com.globe.domain.ReposManager
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetEnrolledAccountsUseCase @Inject constructor(repoManager: ReposManager) {

    private val enrolledAccountsRepo = repoManager.getEnrolledAccountsRepo()

    suspend fun execute(): Flow<LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>> =
        enrolledAccountsRepo.getAllEnrolledAccounts()

}
