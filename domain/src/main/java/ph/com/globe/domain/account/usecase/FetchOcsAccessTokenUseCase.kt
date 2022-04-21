/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetOcsAccessTokenError
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class FetchOcsAccessTokenUseCase @Inject constructor(
    private val accountManager: AccountDataManager
) {
    suspend fun execute(): LfResult<Unit, GetOcsAccessTokenError> =
        accountManager.fetchOcsAccessToken().fold({ token ->
            accountManager.setOcsToken(token)
            LfResult.success(Unit)
        }, {
            LfResult.failure(it)
        })
}
