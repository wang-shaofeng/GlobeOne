/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import javax.inject.Inject

class LogoutEventUseCase @Inject constructor(private val authManager: AuthDataManager) {
    fun get() = authManager.logoutEvent()
}
