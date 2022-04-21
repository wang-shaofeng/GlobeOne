/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.VerifyOtpError
import ph.com.globe.model.auth.VerifyOtpParams
import ph.com.globe.model.auth.VerifyOtpResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: VerifyOtpParams): LfResult<VerifyOtpResult, VerifyOtpError> =
        authManager.verifyOtp(params)

}
