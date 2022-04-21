/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.GetOtpError
import ph.com.globe.model.auth.GetOtpParams
import ph.com.globe.model.auth.GetOtpResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetOtpUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: GetOtpParams): LfResult<GetOtpResult, GetOtpError> {

        return authManager.getOtp(params).fold(
            {
                LfResult.success(it.result)
            },
            {
                LfResult.failure(it)
            }
        )
    }
}
