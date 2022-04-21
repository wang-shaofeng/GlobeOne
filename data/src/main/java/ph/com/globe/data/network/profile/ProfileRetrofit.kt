/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile

import ph.com.globe.model.profile.response_models.*
import retrofit2.Response
import retrofit2.http.*

interface ProfileRetrofit {

    @GET("v1/userManagement/users")
    suspend fun getRegisteredUser(
        @HeaderMap headers: Map<String, String>
    ): Response<GetRegisteredUserResponse>

    @GET("v1/userManagement/accounts")
    suspend fun getEnrolledAccounts(
        @HeaderMap headers: Map<String, String>
    ): Response<GetEnrolledAccountsResponse>

    @GET("v2/customerManagement/customer")
    suspend fun getCustomerDetails(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetCustomerDetailsResponse>

    @GET("v1/customerPreferences/interests")
    suspend fun getCustomerInterests(
        @HeaderMap headers: Map<String, String>
    ): Response<GetCustomerInterestsResponse>

    @GET("/v1/productOffering/eRaffle/entries")
    suspend fun getERaffleEntries(
        @HeaderMap headers: Map<String, String>
    ): Response<GetERaffleEntriesResponse>

    @POST("v1/customerPreferences/nickname")
    suspend fun addCustomerNickname(
        @HeaderMap headers: Map<String, String>,
        @Body nicknameRequest: AddCustomerNicknameRequest
    ): Response<Unit?>

    @POST("v1/customerPreferences/interests")
    suspend fun addCustomerInterests(
        @HeaderMap headers: Map<String, String>,
        @Body interestsRequest: AddCustomerInterestsRequest
    ): Response<Unit?>

    @PUT("v2/userManagement/users")
    suspend fun updateUserProfile(
        @HeaderMap headers: Map<String, String>,
        @Body params: UpdateUserProfileRequestParams
    ): Response<Unit?>

    @POST("v2/userManagement/registrations/notify")
    suspend fun resendEmailVerification(
        @HeaderMap headers: Map<String, String>,
        @Body sendVerificationEmailBody: SendVerificationEmailBody
    ): Response<Unit?>

    @POST("v2/userManagement/registrations/verify")
    suspend fun verifyEmail(
        @HeaderMap headers: Map<String, String>,
        @Body verifyEmailModel: VerifyEmailModel
    ): Response<VerifyEmailResponse>

    @POST(" /v2/userManagement/users/verification")
    suspend fun checkCompleteKYC(
        @HeaderMap headers: Map<String, String>
    ): Response<CheckCompleteKYCResponse>

}
