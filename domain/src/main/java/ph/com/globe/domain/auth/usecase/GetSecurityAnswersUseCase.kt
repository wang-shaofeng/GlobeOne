/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.account.GetSecurityAnswersError
import ph.com.globe.model.auth.GetSecurityAnswersParams
import ph.com.globe.model.auth.SecurityAnswer
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetSecurityAnswersUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: GetSecurityAnswersParams): LfResult<List<SecurityAnswer>, GetSecurityAnswersError> =
        authManager.getSecurityAnswers(params)
}
