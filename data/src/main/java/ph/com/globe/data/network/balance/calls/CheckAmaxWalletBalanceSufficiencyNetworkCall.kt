/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.balance.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.balance.BalanceRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.balance.CheckBalanceSufficiencyError
import ph.com.globe.model.balance.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class CheckAmaxWalletBalanceSufficiencyNetworkCall @Inject constructor(
    private val balanceRetrofit: BalanceRetrofit,
) : HasLogTag {

    suspend fun execute(params: CheckAmaxWalletBalanceSufficiencyParams): LfResult<Boolean, CheckBalanceSufficiencyError> {

        val response = kotlin.runCatching {
            balanceRetrofit.checkAmaxWalletBalanceSufficiency(
                mapOf("amount" to params.amount, "walletType" to "A")
            )
        }.fold(Response<CheckBalanceSufficiencyResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result.sufficient)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "CheckBalanceSufficiencyNetworkCall"
}

private fun NetworkError.toSpecific(): CheckBalanceSufficiencyError =
    CheckBalanceSufficiencyError.General(Other(this))
