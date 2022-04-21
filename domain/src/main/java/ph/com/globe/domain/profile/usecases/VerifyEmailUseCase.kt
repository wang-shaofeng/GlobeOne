/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.errors.profile.VerifyEmailError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class VerifyEmailUseCase @Inject constructor(private val profileDataManager: ProfileDataManager) {
    suspend fun execute(verificationCode: String): LfResult<Unit, VerifyEmailError> =
        profileDataManager.verifyEmail(verificationCode)
}
