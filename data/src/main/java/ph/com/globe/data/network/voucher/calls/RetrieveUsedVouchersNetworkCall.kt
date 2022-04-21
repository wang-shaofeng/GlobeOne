/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.voucher.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.network.voucher.VoucherRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.model.voucher.RetrieveUsedVouchersParams
import ph.com.globe.model.voucher.RetrieveUsedVouchersResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class RetrieveUsedVouchersNetworkCall @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val voucherRetrofit: VoucherRetrofit
) : HasLogTag {

    suspend fun execute(params: RetrieveUsedVouchersParams): LfResult<RetrieveUsedVouchersResponse, RetrieveUsedVouchersError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(
                RetrieveUsedVouchersError.General(GeneralError.NotLoggedIn)
            )
        }

        val response = kotlin.runCatching {
            voucherRetrofit.retrieveUsedVouchers(
                headers,
                params.mobileNumber,
                params.accountNumber
            )
        }.fold(
            Response<RetrieveUsedVouchersResponse>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

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

    override val logTag = "RetrieveUsedVouchersNetworkCall"
}

private fun NetworkError.toSpecific(): RetrieveUsedVouchersError =
    RetrieveUsedVouchersError.General(GeneralError.Other(this))
