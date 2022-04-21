/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.account.GetSecurityAnswersError
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.errors.auth.*
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult

interface AuthDomainManager {

    fun getLoginStatus(): LoginStatus

    suspend fun loginEmail(params: LoginEmailParams): LfResult<LoginResult, LoginError>

    suspend fun registerEmail(params: RegisterEmailParams): LfResult<RegisterEmailResult, RegisterError>

    suspend fun registerSocial(params: RegisterSocialParams): LfResult<RegisterSocialResult, RegisterError>

    suspend fun acceptUserAgreement(params: AcceptUserAgreementParams): LfResult<Unit, AcceptUserAgreementError>

    suspend fun loginSocial(params: LoginSocialParams): LfResult<LoginSocialResult, LoginError>

    suspend fun requestResetPassword(params: RequestResetPasswordParams): LfResult<Unit, RequestResetPasswordError>

    suspend fun sendOtp(params: SendOtpParams): LfResult<SendOtpResult, SendOtpError>

    suspend fun verifyOtp(params: VerifyOtpParams): LfResult<VerifyOtpResult, VerifyOtpError>

    suspend fun getOtp(params: GetOtpParams): LfResult<GetOtpResult, GetOtpError>

    suspend fun getSecurityQuestions(params: GetSecurityQuestionsParams): LfResult<GetSecurityQuestionsResult, GetSecurityQuestionsError>

    suspend fun validateSecurityAnswers(params: ValidateSecurityAnswersParams): LfResult<Unit, ValidateSecurityAnswersError>

    suspend fun getSecurityAnswers(params: GetSecurityAnswersParams): LfResult<List<SecurityAnswer>, GetSecurityAnswersError>

    suspend fun logout(): LfResult<Unit?, LogoutError>

    suspend fun forceLogout()

    fun logoutEvent(): Flow<Boolean>

    fun removeUserDataIfRefreshUserTokenExpired()

    suspend fun exchangeSocialAccessTokenWithGlobeSocialToken(
        token: String,
        provider: String
    ): LfResult<String, ExchangeTokenError>

    suspend fun validateSimSerial(params: ValidateSimSerialParams): LfResult<ValidateSimSerialResponse, ValidateSimSerialPairingError>

    suspend fun setSymmetricKey()

    suspend fun getSymmetricKey()
}
