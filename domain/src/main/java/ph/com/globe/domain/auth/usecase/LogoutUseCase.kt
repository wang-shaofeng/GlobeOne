/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.app_data.AppDataDomainManager
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.LogoutError
import ph.com.globe.util.LfResult
import ph.com.globe.util.onSuccess
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authManager: AuthDataManager,
    private val appDataDomainManager: AppDataDomainManager
) {

    suspend fun execute(): LfResult<Unit?, LogoutError> =
        authManager.logout().onSuccess {
            authManager.removeUserData()
            appDataDomainManager.clearDatabase()
            authManager.sendLogoutEvent(false)
        }
}
