/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import javax.inject.Inject

class ExchangeSocialAccessTokenWithGlobeSocialTokenUseCase @Inject constructor(
    private val authManager: AuthDataManager
) {
    suspend fun execute(token: String, provider: String) =
        authManager.exchangeSocialAccessTokenWithGlobeSocialToken(token, provider)
}
