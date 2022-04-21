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
import ph.com.globe.errors.voucher.GetLoyaltySubscribersCouponDetailsError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.voucher.GetLoyaltySubscribersCouponDetailsParams
import ph.com.globe.model.voucher.LoyaltySubscriberCouponDetailsResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetLoyaltySubscribersCouponDetailsNetworkCall @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val voucherRetrofit: VoucherRetrofit
) : HasLogTag {

    suspend fun execute(params: GetLoyaltySubscribersCouponDetailsParams): LfResult<LoyaltySubscriberCouponDetailsResponse, GetLoyaltySubscribersCouponDetailsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(
                GetLoyaltySubscribersCouponDetailsError.General(GeneralError.NotLoggedIn)
            )
        }.plus("x-api-key" to BuildConfig.X_API_KEY)

        val response = kotlin.runCatching {
            voucherRetrofit.getLoyaltySubscribersCouponDetails(
                headers,
                params.subscriberId,
                params.subscriberType,
                params.channel,
                params.expiryDateFrom,
                params.expiryDateTo,
                params.offset,
                params.limit
            )
        }.fold(
            Response<LoyaltySubscriberCouponDetailsResponse>::toLfSdkResult,
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

    override val logTag = "GetLoyaltySubscribersCouponDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): GetLoyaltySubscribersCouponDetailsError =
    GetLoyaltySubscribersCouponDetailsError.General(GeneralError.Other(this))
