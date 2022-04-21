/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.ValidateSecurityAnswersError
import ph.com.globe.model.auth.ValidateSecurityAnswersParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class ValidateSecurityAnswersUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: ValidateSecurityAnswersParams): LfResult<Unit, ValidateSecurityAnswersError> =
        authManager.validateSecurityAnswers(params)
}
