/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetAccountBrandError
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.account.GetAccountBrandResponse
import ph.com.globe.model.account.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetAccountBrandNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit
) : HasLogTag {

    suspend fun execute(params: GetAccountBrandParams): LfResult<GetAccountBrandResponse, GetAccountBrandError> {

        val response = kotlin.runCatching {
            accountRetrofit.getAccountBrand(params.toQueryMap())
        }.fold(Response<GetAccountBrandResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetAccountBrandNetworkCall"
}

private fun NetworkError.toSpecific(): GetAccountBrandError {
    when (this) {
        is NetworkError.Http -> {
            if (errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "The provided account is not valid.") {
                return GetAccountBrandError.InvalidAccount
            }
            if (errorResponse?.error?.code == "40002" && errorResponse?.error?.details == "The request parameter is invalid.") {
                return GetAccountBrandError.InvalidParameter
            }
        }

        else -> Unit
    }
    return GetAccountBrandError.General(Other(this))
}
