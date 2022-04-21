/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rush.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rush.RushRetrofit
import ph.com.globe.data.network.util.logFailedNetworkCall
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rush.CreateRushUserError
import ph.com.globe.model.rush.CreateRushUserParams
import ph.com.globe.model.rush.CreateRushUserRequestModel
import ph.com.globe.model.rush.CreateRushUserResponseModel
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class CreateRushUserNetworkCall @Inject constructor(
    private val rushRetrofit: RushRetrofit
) : HasLogTag {
    suspend fun execute(params: CreateRushUserParams): LfResult<CreateRushUserResponseModel, CreateRushUserError> {
        val response = kotlin.runCatching {
            rushRetrofit.createRushUser(
                "Bearer ${params.token}",
                CreateRushUserRequestModel(params.identifier)
            )
        }.fold(Response<CreateRushUserResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "CreateRushUserNetworkCall"
}

private fun NetworkError.toSpecific() = CreateRushUserError.General(GeneralError.Other(this))
