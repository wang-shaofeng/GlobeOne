/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.G2EncryptionRetrofit
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.model.auth.SymmetricKeyResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetSymmetricKeyNetworkCall @Inject constructor(
    private val cognitoRetrofit: G2EncryptionRetrofit
) : HasLogTag {

    suspend fun execute(accessToken: String): LfResult<SymmetricKeyResponse, Unit> {

        val response = kotlin.runCatching {
            cognitoRetrofit.getSymmetricKey(auth = "Bearer $accessToken")
        }.fold(Response<SymmetricKeyResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            LfResult.success(it)
        }, {
            LfResult.failure(Unit)
        })
    }

    override val logTag: String
        get() = "GetSymmetricKeyNetworkCall"
}
