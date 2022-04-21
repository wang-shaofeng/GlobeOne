/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.errors.auth.RegisterError
import ph.com.globe.model.auth.*
import ph.com.globe.model.profile.response_models.RegisterUserParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class LoginSocialUseCase @Inject constructor(
    private val authManager: AuthDataManager
) : HasLogTag {

    suspend fun execute(params: LoginSocialParams): LfResult<LoginSocialResult, LoginError> {
        return authManager.loginSocial(params).fold(
            { socialLoginResult ->
                // if the UserToken returned from loginSocial has a non 'true' field isNew
                // that means that the user in not just registered
                if ((socialLoginResult as LoginSocialResult.SocialLoginSuccessful).isNew != true) {
                    LfResult.success(socialLoginResult)
//                    return profileDomainManager.getRegisteredUser().fold({
//                        dLog("successful social login")
//                        LfResult.success(socialLoginResult)
//                    }, {
//                        dLog("login with not migrated account")
//                        val socialLoginSuccessfulResult =
//                            socialLoginResult as LoginSocialResult.SocialLoginSuccessful
//                        return authManager.registerUser(
//                            RegisterUserParams(
//                                email = socialLoginSuccessfulResult.email ?: "",
//                                emailVerificationDate = socialLoginSuccessfulResult.emailVerified
//                                    ?: ""
//                            ),
//                        ).fold({
//                            LfResult.success(
//                                LoginSocialResult.LoginSuccessfulMigrationNeeded(
//                                    socialLoginSuccessfulResult.email ?: ""
//                                )
//                            )
//                        }, {
//                            LfResult.failure(it)
//                        })
//                    })
                } else {
                    // if the UserToken returned from loginSocial has field isNew set to 'true'
                    // that means that the user in just registered so we proceed with accepting terms and conditions
                    authManager.acceptUserAgreement(
                        AcceptUserAgreementParams(
                            socialLoginResult.userToken!!,
                            UserAgreementParams("1.0", "1.0")
                        )
                    ).fold(
                        {
                            LfResult.success(LoginSocialResult.SocialRegisterSuccessful)
                        }, { error ->
                            LfResult.failure(
                                LoginError.SocialRegisterFailedError(
                                    RegisterError.AcceptTermsError(error)
                                )
                            )
                        }
                    )
                }
            },
            { error ->
                when (error) {
                    is LoginError.UserNotYetRegistered -> {
                        dLog("social register")
                        val registerSocialParams =
                            RegisterSocialParams(
                                error.email,
                                params.socialProvider,
                                params.socialToken
                            )
                        authManager.registerSocial(registerSocialParams).fold(
                            {
                                authManager.acceptUserAgreement(
                                    AcceptUserAgreementParams(
                                        (it as RegisterSocialResult.RegisterSuccessful).token!!,
                                        UserAgreementParams("1.0", "1.0")
                                    )
                                ).fold(
                                    {
                                        LfResult.success(LoginSocialResult.SocialRegisterSuccessful)
                                    }, { error ->
                                        LfResult.failure(
                                            LoginError.SocialRegisterFailedError(
                                                RegisterError.AcceptTermsError(error)
                                            )
                                        )
                                    }
                                )
                            }, {
                                LfResult.failure(LoginError.SocialRegisterFailedError(it))
                            }
                        )
                    }
                    else -> {
                        LfResult.failure(error)
                    }
                }
            }
        )
    }

    override val logTag = "LoginSocialUseCase"
}
