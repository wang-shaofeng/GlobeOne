/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.errors.auth.RegisterError
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.onFailure
import javax.inject.Inject

class RegisterEmailUseCase @Inject constructor(
    private val authManager: AuthDataManager,
    private val userDetailsDataManager: UserDetailsDataManager
) {

    suspend fun execute(params: RegisterEmailParams): LfResult<RegisterEmailResult, RegisterError> {
        return authManager.registerEmail(params).fold(
            {
                // On successful register

                // We first need to accept terms and conditions
                val acceptTermsResult = authManager.acceptUserAgreement(
                    AcceptUserAgreementParams(
                        it.token,
                        UserAgreementParams("1.0", "1.0")
                    )
                )

                acceptTermsResult.onFailure { error ->
                    return@fold LfResult.failure(
                        RegisterError.AcceptTermsError(
                            error
                        )
                    )
                }
                authManager.setLoginStatus(LoginStatus.UNVERIFIED)
                userDetailsDataManager.setEmail(params.email)
                LfResult.success(it)
            },
            {
                LfResult.failure(it)
            }
        )
    }
}
