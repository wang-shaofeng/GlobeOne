/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.app_data.usecases

import ph.com.globe.domain.ReposManager
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class FetchRegisteredUserUseCase @Inject constructor(
    repoManager: ReposManager
) {

    private val registeredUserRepo = repoManager.getRegisteredUserRepo()

    suspend fun execute(): LfResult<Unit, GetRegisteredUserError> =
        registeredUserRepo.fetchRegisteredUser()
}
