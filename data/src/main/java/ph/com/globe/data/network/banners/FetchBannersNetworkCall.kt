/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.banners.FetchBannersError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.banners.BannerCarouselModel
import ph.com.globe.model.banners.BannersResponseModel
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class FetchBannersNetworkCall @Inject constructor(
    private val bannersRetrofit: BannersRetrofit
) : HasLogTag {

    suspend fun execute(): LfResult<List<BannerCarouselModel>, FetchBannersError> {

        val response = kotlin.runCatching {
            bannersRetrofit.getBanners(mapOf(
                "version" to BuildConfig.VERSION_NAME.extractVersionNameNumber(),
                "mobile_type" to "android")
            )
        }.fold(Response<BannersResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.data.field_mobile_section_component)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "FetchBannersNetworkCall"
}

private fun NetworkError.toSpecific() = FetchBannersError.General(GeneralError.Other(this))
