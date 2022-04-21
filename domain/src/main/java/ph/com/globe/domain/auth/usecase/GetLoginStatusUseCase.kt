/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.model.auth.LoginStatus
import javax.inject.Inject

class GetLoginStatusUseCase @Inject constructor(
    private val authManager: AuthDataManager
) {

    fun execute(): LoginStatus =
        authManager.getLoginStatus()

}
