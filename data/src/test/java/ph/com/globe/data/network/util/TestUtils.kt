/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.util

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.errors.auth.RegisterError
import ph.com.globe.model.auth.LoginEmailParams
import ph.com.globe.model.auth.LoginResponse
import ph.com.globe.model.auth.RegisterEmailParams
import ph.com.globe.model.auth.RegisterEmailResult
import ph.com.globe.util.LfResult

internal suspend fun AuthDataManager.loginWithEmail(): LfResult<LoginResponse?, LoginError> {
    val loginParams = LoginEmailParams(
        "ognjen.bogicevic@lotusflare.com",
        "Test12345!",
        null
    )

    return loginEmail(loginParams)
}

internal suspend fun AuthDataManager.testRegisterEmail(): LfResult<RegisterEmailResult, RegisterError> {
    val registerParams = RegisterEmailParams(
        "ognjen.bogicevic@lotusflare.com",
        "Test12345!",
        "Test12345!"
    )

    return registerEmail(registerParams)
}
