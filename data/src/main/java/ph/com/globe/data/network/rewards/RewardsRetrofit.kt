/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards

import ph.com.globe.model.rewards.*
import retrofit2.Response
import retrofit2.http.*

interface RewardsRetrofit {

    @GET("v2/loyaltyManagement/rewards/points")
    suspend fun getRewardPoints(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>,
        @Query(value = "email", encoded = true) email: String? = null,
        @Query("posId") posId: String? = null,
        @Query("loyaltyProgramId") loyaltyProgramId: String? = null
    ): Response<GetRewardPointsResponse>

    @GET("v2/dataCurrency/qualification")
    suspend fun getConversionQualification(
        @HeaderMap headers: Map<String, String>,
        @Query("mobileNumber") mobileNumber: String,
        @Query("rateId") rateId: String
    ): Response<GetConversionQualificationModel>

    @POST("v2/dataCurrency/conversion")
    suspend fun addDataConversion(
        @HeaderMap headers: Map<String, String>,
        @Body addDataConversionRequest: AddDataConversionRequest
    ): Response<Unit>

    @GET("v2/dataCurrency/conversion/{conversionId}")
    suspend fun getDataConversionDetails(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "conversionId", encoded = false) conversionId: String
    ): Response<GetDataConversionDetailsResponse>

    @GET("v1/loyaltyManagement/rewards")
    suspend fun getRewardsCatalog(
        @HeaderMap headers: Map<String, String>,
        @Query("loyaltyProgramId") loyaltyProgramId: String,
        @Header("Channel") channel: String = "Z"
    ): Response<RewardsCatalogResponseModel>

    @GET("v2/loyaltyManagement/customerProfile")
    suspend fun getLoyaltyCustomerProfile(
        @HeaderMap headers: Map<String, String>,
        @Query("mobileNumber") mobileNumber: String
    ): Response<LoyaltyCustomerProfileResponseModel>

    @POST("v2/loyaltyManagement/rewards/redeem")
    suspend fun redeemLoyaltyRewards(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>,
        @Body body: RedeemLoyaltyRewardsRequestModel,
        @Header("Channel") channel: String = "Z"
    ): Response<RedeemLoyaltyRewardsResponseModel>

    @GET("v1/productOrdering/merchants")
    suspend fun getMerchantDetails(
        @HeaderMap headers: Map<String, String>,
        @Query("uuid") uuid: String? = null,
        @Query("mobileNumber") mobileNumber: String? = null
    ): Response<GetMerchantDetailsResponse>

    @POST("v2/loyaltyManagement/rewards/points/redemption")
    suspend fun redeemRewardsPoints(
        @HeaderMap headers: Map<String, String>,
        @Body body: RedeemPointsRequestModel,
        @Header("Channel") channel: String = "Z"
    ): Response<RedeemPointsResponseModel>
}
