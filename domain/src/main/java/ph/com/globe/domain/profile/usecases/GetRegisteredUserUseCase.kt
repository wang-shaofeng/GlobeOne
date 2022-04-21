/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ph.com.globe.domain.ReposManager
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetRegisteredUserUseCase @Inject constructor(repoManager: ReposManager) {

    private val registeredUserRepo = repoManager.getRegisteredUserRepo()

    suspend fun execute(): Flow<LfResult<RegisteredUser?, GetRegisteredUserError>> =
        registeredUserRepo.checkFreshnessAndUpdate().fold(
            {
                registeredUserRepo.getRegisteredUser().map {
                    LfResult.success(it)
                }
            }, {
                flowOf(LfResult.failure(it))
            }
        )

}
