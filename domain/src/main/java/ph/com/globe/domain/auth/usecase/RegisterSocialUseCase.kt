/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.errors.auth.RegisterError
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class RegisterSocialUseCase @Inject constructor(private val authManager: AuthDataManager) :
    HasLogTag {

    suspend fun execute(params: RegisterSocialParams): LfResult<RegisterSocialResult, RegisterError> {
        return authManager.loginSocial(LoginSocialParams(params.socialProvider, params.socialToken))
            .fold(
                {
                    LfResult.success(RegisterSocialResult.LoginSuccessful())
                },
                {
                    if (it is LoginError.UserNotYetRegistered) {
                        dLog("social register")
                        params.email = it.email
                        authManager.registerSocial(params).fold(
                            { registerResult ->
                                authManager.acceptUserAgreement(
                                    AcceptUserAgreementParams(
                                        (registerResult as RegisterSocialResult.RegisterSuccessful).token!!,
                                        UserAgreementParams("1.0", "1.0")
                                    )
                                ).fold(
                                    {
                                        LfResult.success(RegisterSocialResult.RegisterSuccessful())
                                    }, { error ->
                                        LfResult.failure(
                                            RegisterError.AcceptTermsError(error)
                                        )
                                    }
                                )
                            }, { error ->
                                LfResult.failure(error)
                            }
                        )
                    } else {
                        LfResult.failure(RegisterError.LoginFailedError(it))
                    }
                }
            )
    }

    override val logTag = "RegisterSocialUseCase"
}
