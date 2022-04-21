/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush

import ph.com.globe.errors.rush.CreateRushUserError
import ph.com.globe.errors.rush.GetRushAccessTokenError
import ph.com.globe.model.rush.CreateRushUserParams
import ph.com.globe.model.rush.CreateRushUserResponseModel
import ph.com.globe.model.rush.GetRushAccessTokenResponseModel
import ph.com.globe.model.rush.GetRushUserAccessTokenParams
import ph.com.globe.util.LfResult

interface RushDataManager {

    suspend fun getRushUserAccessToken(params: GetRushUserAccessTokenParams): LfResult<GetRushAccessTokenResponseModel, GetRushAccessTokenError>

    suspend fun getRushAdminAccessToken(): LfResult<GetRushAccessTokenResponseModel, GetRushAccessTokenError>

    suspend fun createRushUser(params: CreateRushUserParams): LfResult<CreateRushUserResponseModel, CreateRushUserError>
}
