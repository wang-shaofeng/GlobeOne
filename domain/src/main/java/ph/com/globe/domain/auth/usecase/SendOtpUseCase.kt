/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.SendOtpError
import ph.com.globe.model.auth.SendOtpParams
import ph.com.globe.model.auth.SendOtpResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(private val authManager: AuthDataManager) {
    suspend fun execute(params: SendOtpParams): LfResult<SendOtpResult, SendOtpError> =
        authManager.sendOtp(params)
}
