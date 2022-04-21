/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.model.auth.LoginEmailParams
import ph.com.globe.model.auth.LoginResult
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.profile.response_models.RegisterUserParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class LoginEmailUseCase @Inject constructor(
    private val authManager: AuthDataManager,
    private val profileManager: ProfileDataManager,
    private val userDetailsDataManager: UserDetailsDataManager
) : HasLogTag {

    suspend fun execute(params: LoginEmailParams): LfResult<LoginResult, LoginError> =
        authManager.loginEmail(params).fold(
            { loginResponse ->
                LfResult.success(LoginResult.LoginSuccessful)
//                return profileManager.getRegisteredUser().fold({
//                    dLog("successful email login")
//                    if (loginResponse != null) {
//                        LfResult.success(LoginResult.LoginSuccessful)
//                    } else {
//                        LfResult.failure(LoginError.UserEmailNotVerified(params.email))
//                    }
//                }, {
//                    dLog("login with not migrated account")
//                    return authManager.registerUser(RegisterUserParams(email = params.email)).fold({
//                        LfResult.success(LoginResult.LoginSuccessfulMigrationNeeded)
//                    }, {
//                        LfResult.failure(it)
//                    })
//                })
            },
            {
                if (it is LoginError.UserEmailNotVerified) {
                    userDetailsDataManager.setEmail(params.email)
                    authManager.setLoginStatus(LoginStatus.UNVERIFIED)
                }
                LfResult.failure(it)
            }
        )

    override val logTag = "LoginEmailUseCase"
}
