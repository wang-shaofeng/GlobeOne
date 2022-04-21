/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.AcceptUserAgreementError
import ph.com.globe.model.auth.AcceptUserAgreementParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class AcceptUserAgreementUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: AcceptUserAgreementParams): LfResult<Unit, AcceptUserAgreementError> =
        authManager.acceptUserAgreement(params)
}
