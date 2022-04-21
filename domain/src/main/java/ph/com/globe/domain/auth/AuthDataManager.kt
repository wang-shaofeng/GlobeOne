/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetSecurityAnswersError
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.errors.auth.*
import ph.com.globe.model.auth.*
import ph.com.globe.model.profile.response_models.RegisterUserParams
import ph.com.globe.util.LfResult

interface AuthDataManager {

    fun getDeviceId(): String

    fun getLoginStatus(): LoginStatus

    fun setLoginStatus(loginStatus: LoginStatus)

    fun getAccessToken(): LfResult<String, NetworkError.NoAccessToken>

    fun setAccessToken(token: String)

    fun fetchAccessToken(): LfResult<String, GetAccessTokenError>

    fun getUserToken(): LfResult<String, NetworkError.UserNotLoggedInError>

    fun setUserToken(token: String)

    fun refreshUserToken(): LfResult<String, RefreshUserTokenError>

    suspend fun loginEmail(params: LoginEmailParams): LfResult<LoginResponse?, LoginError>

    suspend fun registerUser(params: RegisterUserParams): LfResult<Unit, LoginError>

    suspend fun loginSocial(params: LoginSocialParams): LfResult<LoginSocialResult, LoginError>

    suspend fun registerEmail(params: RegisterEmailParams): LfResult<RegisterEmailResult, RegisterError>

    suspend fun registerSocial(params: RegisterSocialParams): LfResult<RegisterSocialResult, RegisterError>

    suspend fun acceptUserAgreement(params: AcceptUserAgreementParams): LfResult<Unit, AcceptUserAgreementError>

    suspend fun requestPasswordReset(params: RequestResetPasswordParams): LfResult<Unit, RequestResetPasswordError>

    suspend fun sendOtp(params: SendOtpParams): LfResult<SendOtpResult, SendOtpError>

    suspend fun verifyOtp(params: VerifyOtpParams): LfResult<VerifyOtpResult, VerifyOtpError>

    suspend fun getOtp(params: GetOtpParams): LfResult<GetOtpResponse, GetOtpError>

    suspend fun getSecurityQuestions(params: GetSecurityQuestionsParams): LfResult<GetSecurityQuestionsResult, GetSecurityQuestionsError>

    suspend fun validateSecurityAnswers(params: ValidateSecurityAnswersParams): LfResult<Unit, ValidateSecurityAnswersError>

    suspend fun getSecurityAnswers(params: GetSecurityAnswersParams): LfResult<List<SecurityAnswer>, GetSecurityAnswersError>

    fun removeUserData()

    fun sendLogoutEvent(isUserTokenExpired: Boolean)

    fun logoutEvent(): Flow<Boolean>

    fun removeAccessToken()

    fun getTimeWhenUserTokenWasFetched(): Long

    fun setTimeWhenUserTokenWasFetched(time: Long)

    suspend fun logout(): LfResult<Unit?, LogoutError>

    suspend fun exchangeSocialAccessTokenWithGlobeSocialToken(
        token: String,
        provider: String
    ): LfResult<String, ExchangeTokenError>

    suspend fun validateSimSerial(params: ValidateSimSerialParams): LfResult<ValidateSimSerialResponse, ValidateSimSerialPairingError>

    suspend fun setSymmetricKey()

    suspend fun getSymmetricKey(): String?
}
