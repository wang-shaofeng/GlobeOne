/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rush

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.rush.RushDataManager
import ph.com.globe.errors.rush.CreateRushUserError
import ph.com.globe.errors.rush.GetRushAccessTokenError
import ph.com.globe.model.rush.CreateRushUserParams
import ph.com.globe.model.rush.CreateRushUserResponseModel
import ph.com.globe.model.rush.GetRushAccessTokenResponseModel
import ph.com.globe.model.rush.GetRushUserAccessTokenParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkRushManager @Inject constructor(
    factory: RushComponent.Factory
) : RushDataManager {

    private val rushComponent = factory.create()

    override suspend fun getRushUserAccessToken(params: GetRushUserAccessTokenParams): LfResult<GetRushAccessTokenResponseModel, GetRushAccessTokenError> =
        withContext(Dispatchers.IO) {
            rushComponent.provideGetRushUserAccessTokenNetworkCall().execute(params)
        }

    override suspend fun getRushAdminAccessToken(): LfResult<GetRushAccessTokenResponseModel, GetRushAccessTokenError> =
        withContext(Dispatchers.IO) {
            rushComponent.provideGetRushAdminAccessTokenNetworkCall().execute()
        }

    override suspend fun createRushUser(params: CreateRushUserParams): LfResult<CreateRushUserResponseModel, CreateRushUserError> =
        withContext(Dispatchers.IO) {
            rushComponent.provideCreateRushUserNetworkCall().execute(params)
        }
}
