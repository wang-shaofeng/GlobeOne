/*
 * Copyright (C) 2022 LotusFlare
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
import ph.com.globe.errors.voucher.GetPromoVouchersError
import ph.com.globe.model.voucher.GetPromoVouchersParams
import ph.com.globe.model.voucher.PromoVouchersResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetPromoVouchersNetworkCall @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val voucherRetrofit: VoucherRetrofit,
) : HasLogTag {

    suspend fun execute(params: GetPromoVouchersParams): LfResult<PromoVouchersResponse, GetPromoVouchersError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(
                GetPromoVouchersError.General(GeneralError.NotLoggedIn)
            )
        }.plus("Channel" to "superapp")

        val response = kotlin.runCatching {
            voucherRetrofit.getPromoVouchers(
                headers,
                params.pageNumber,
                params.pageSize,
                params.mobileNumber,
            )
        }.fold(
            Response<PromoVouchersResponse>::toLfSdkResult,
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

    override val logTag = "GetPromoVouchersNetworkCall"
}

private fun NetworkError.toSpecific(): GetPromoVouchersError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 &&
                errorResponse?.error?.code == "40402"
            ) {
                return GetPromoVouchersError.PromoVouchersNotFound
            }
        }

        else -> Unit
    }
    return GetPromoVouchersError.General(GeneralError.Other(this))
}
