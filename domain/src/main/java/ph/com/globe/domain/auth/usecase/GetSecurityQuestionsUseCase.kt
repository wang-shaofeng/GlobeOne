/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.model.auth.GetSecurityQuestionsParams
import ph.com.globe.model.auth.GetSecurityQuestionsResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetSecurityQuestionsUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: GetSecurityQuestionsParams): LfResult<GetSecurityQuestionsResult, GetSecurityQuestionsError> =
        authManager.getSecurityQuestions(params)
}
