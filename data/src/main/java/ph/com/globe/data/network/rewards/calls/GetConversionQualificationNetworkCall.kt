/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rewards.RewardsRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rewards.GetConversionQualificationError
import ph.com.globe.globeonesuperapp.domain.BuildConfig
import ph.com.globe.model.rewards.GetConversionQualificationModel
import ph.com.globe.model.rewards.GetConversionQualificationParams
import ph.com.globe.model.rewards.GetConversionQualificationResult
import ph.com.globe.model.util.brand.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetConversionQualificationNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository,
) : HasLogTag {

    suspend fun execute(params: GetConversionQualificationParams): LfResult<GetConversionQualificationResult, GetConversionQualificationError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetConversionQualificationError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            rewardsRetrofit.getConversionQualification(
                headers,
                mobileNumber = params.mobileNumber,
                rateId = when (params.brand) {
                    AccountBrand.GhpPostpaid -> BuildConfig.GHP_RATE_ID
                    AccountBrand.Hpw -> BuildConfig.PW_RATE_ID
                    AccountBrand.GhpPrepaid -> BuildConfig.GHP_PREPAID_RATE_ID
                    AccountBrand.Tm -> BuildConfig.TM_RATE_ID
                    else -> return LfResult.failure(
                        GetConversionQualificationError.General(
                            GeneralError.Other(NetworkError.InvalidParamsFormat)
                        )
                    )
                }
            )
        }.fold(Response<GetConversionQualificationModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetConversionQualificationNetworkCall"
}

private fun NetworkError.toSpecific(): GetConversionQualificationError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202") {
                return GetConversionQualificationError.NoQualifications
            }
        }

        else -> Unit
    }
    return GetConversionQualificationError.General(GeneralError.Other(this))
}

