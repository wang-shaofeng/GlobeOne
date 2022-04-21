/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth

import okhttp3.RequestBody
import ph.com.globe.data.network.auth.model.*
import ph.com.globe.model.auth.*
import ph.com.globe.model.profile.response_models.RegisterUserParams
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface AuthRetrofit {

    @POST("v1/channels/oauth/token")
    fun getAccessToken(
        @HeaderMap headers: Map<String, String>,
        @Body getOffersJsonRequest: RequestBody
    ): Call<GetAccessTokenResponse>

    @POST("v2/userManagement/login")
    suspend fun login(
        @HeaderMap headers: Map<String, String>,
        @Body getOffersJsonRequest: LoginJsonRequest
    ): Response<LoginResponse?>

    @POST("v2/userManagement/users")
    suspend fun registerUser(
        @HeaderMap headers: Map<String, String>,
        @Body params: RegisterUserParams
    ): Response<Unit?>

    @POST("v2/userManagement/registrations")
    suspend fun register(
        @HeaderMap headers: Map<String, String>,
        @Body registerJsonRequest: RegisterJsonRequest
    ): Response<RegisterJsonResponse?>

    @POST("v2/userManagement/registrations")
    suspend fun registerSocial(
        @HeaderMap headers: Map<String, String>,
        @Body request: RegisterSocialRequest
    ): Response<RegisterSocialResponse>

    @POST("v1/userManagement/agreements/acceptance")
    suspend fun acceptUserAgreement(
        @HeaderMap headers: Map<String, String>,
        @Body request: UserAgreementParams
    ): Response<Unit?>

    @POST("v2/userManagement/passwords/request")
    suspend fun requestResetPassword(
        @HeaderMap headers: Map<String, String>,
        @Body requestResetPasswordRequest: RequestResetPasswordRequest
    ): Response<Unit?>

    @GET("/v1/accountManagement/accounts/securityQuestions")
    suspend fun getSecurityQuestions(
        @QueryMap query: Map<String, String>,
    ): Response<GetSecurityQuestionsResponse>

    @GET("testApi/securityAnswers")
    suspend fun getSecurityAnswers(
        @Query("referenceId") referenceId: String,
    ): Response<GetSecurityAnswersResponse>

    @POST("/v1/accountManagement/accounts/securityQuestions")
    suspend fun validateSecurityAnswers(
        @Body request: ValidateSecurityAnswersRequest,
    ): Response<Unit?>

    @POST("v2/communicationMessage/otp")
    suspend fun sendOtp(
        @Body sendOtpJsonRequest: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("v2/communicationMessage/otp/verification")
    suspend fun verifyOtp(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOtpParams: VerifyOtpRequest
    ): Response<Unit?>

    @POST("v2/communicationMessage/otp/verification")
    suspend fun verifyOtpWithThirdParty(
        @HeaderMap headers: Map<String, String>,
        @Body verifyOtpJsonRequest: VerifyOtpWithThirdPartyParams
    ): Response<Unit?>

    @GET("v1/communicationMessage/otp")
    suspend fun getOtp(
        @QueryMap query: Map<String, String>,
        @Query("categoryIdentifier") categoryIdentifiers: String
    ): Response<GetOtpResponse>

    @PUT("v2/userManagement/token")
    fun refreshUserToken(
        @HeaderMap headers: Map<String, String>
    ): Call<RefreshUserTokenResponse>

    @POST("v2/userManagement/logout")
    suspend fun logout(
        @HeaderMap headers: Map<String, String>
    ): Response<Unit?>

    @POST("v1/customerManagement/sim/pairing")
    suspend fun validateSimSerial(
        @Body validateSimSerialRequest: ValidateSimSerialRequest
    ): Response<ValidateSimSerialResponse>
}
