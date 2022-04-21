/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.errors.profile.SendVerificationEmailError
import ph.com.globe.util.LfResult
import ph.com.globe.util.onFailure
import javax.inject.Inject

class SendVerificationEmailUseCase @Inject constructor(
    private val profileManager: ProfileDataManager,
    private val authManager: AuthDataManager
) {
    suspend fun execute(): LfResult<Unit, SendVerificationEmailError> =
        profileManager.sendVerificationEmail().onFailure {
            if (it is SendVerificationEmailError.EmailIsAlreadyVerified) {
                authManager.removeUserData()
            }
        }
}
