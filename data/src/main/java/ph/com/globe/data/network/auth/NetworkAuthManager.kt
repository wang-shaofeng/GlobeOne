/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.data.network.user_details.UserDetailsRepository
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetSecurityAnswersError
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.errors.auth.*
import ph.com.globe.model.auth.*
import ph.com.globe.model.profile.response_models.RegisterUserParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.onSuccess
import javax.inject.Inject

class NetworkAuthManager @Inject constructor(
    factory: AuthComponent.Factory,
    // This is temporary, once RepoManager is introduced, this should be removed
    private val tokenRepository: TokenRepository,
    private val userDetailsRepository: UserDetailsRepository
) : AuthDataManager {

    private val authComponent: AuthComponent = factory.create()

    override fun getDeviceId(): String =
        tokenRepository.getDeviceId()

    override fun getLoginStatus(): LoginStatus =
        tokenRepository.getLoginStatus()

    override fun setLoginStatus(loginStatus: LoginStatus) =
        tokenRepository.setLoginStatus(loginStatus)

    override fun getAccessToken(): LfResult<String, NetworkError.NoAccessToken> =
        tokenRepository.getAccessToken()

    override fun setAccessToken(token: String) = tokenRepository.setAccessToken(token)

    override fun fetchAccessToken(): LfResult<String, GetAccessTokenError> =
        authComponent.provideGetAccessTokenNetworkCall().execute()

    override fun getUserToken(): LfResult<String, NetworkError.UserNotLoggedInError> =
        tokenRepository.getUserToken()

    override fun setUserToken(token: String) = tokenRepository.setUserToken(token)

    override fun refreshUserToken(): LfResult<String, RefreshUserTokenError> =
        authComponent.provideRefreshUserTokenNetworkCall().execute()

    override suspend fun loginEmail(params: LoginEmailParams): LfResult<LoginResponse?, LoginError> =
        withContext(Dispatchers.IO) { authComponent.provideLoginEmailNetworkCall().execute(params) }

    override suspend fun registerUser(params: RegisterUserParams): LfResult<Unit, LoginError> =
        withContext(Dispatchers.IO) {
            authComponent.provideRegisterUserNetworkCall().execute(params)
        }

    override suspend fun loginSocial(params: LoginSocialParams): LfResult<LoginSocialResult, LoginError> =
        withContext(Dispatchers.IO) {
            authComponent.provideLoginSocialNetworkCall().execute(params)
        }

    override suspend fun registerEmail(params: RegisterEmailParams): LfResult<RegisterEmailResult, RegisterError> =
        withContext(Dispatchers.IO) {
            authComponent.provideRegisterEmailNetworkCall().execute(params)
        }

    override suspend fun registerSocial(params: RegisterSocialParams): LfResult<RegisterSocialResult, RegisterError> =
        withContext(Dispatchers.IO) {
            authComponent.provideRegisterSocialNetworkCall().execute(params)
        }

    override suspend fun acceptUserAgreement(params: AcceptUserAgreementParams): LfResult<Unit, AcceptUserAgreementError> =
        withContext(Dispatchers.IO) {
            authComponent.provideAcceptUserAgreementNetworkCall().execute(params)
        }

    override suspend fun requestPasswordReset(params: RequestResetPasswordParams): LfResult<Unit, RequestResetPasswordError> =
        withContext(Dispatchers.IO) {
            authComponent.provideRequestResetPasswordNetworkCall().execute(params)
        }

    override suspend fun sendOtp(params: SendOtpParams): LfResult<SendOtpResult, SendOtpError> =
        withContext(Dispatchers.IO) { authComponent.provideSendOtpNetworkCall().execute(params) }

    override suspend fun verifyOtp(params: VerifyOtpParams): LfResult<VerifyOtpResult, VerifyOtpError> =
        withContext(Dispatchers.IO) { authComponent.provideVerifyOtpNetworkCall().execute(params) }

    override suspend fun getOtp(params: GetOtpParams): LfResult<GetOtpResponse, GetOtpError> =
        withContext(Dispatchers.IO) { authComponent.provideGetOtpNetworkCall().execute(params) }

    override suspend fun getSecurityQuestions(params: GetSecurityQuestionsParams): LfResult<GetSecurityQuestionsResult, GetSecurityQuestionsError> =
        withContext(Dispatchers.IO) {
            authComponent.provideGetSecurityQuestionsNetworkCall().execute(params)
        }

    override suspend fun validateSecurityAnswers(params: ValidateSecurityAnswersParams): LfResult<Unit, ValidateSecurityAnswersError> =
        withContext(Dispatchers.IO) {
            authComponent.provideValidateSecurityAnswersNetworkCall().execute(params)
        }

    override suspend fun getSecurityAnswers(params: GetSecurityAnswersParams): LfResult<List<SecurityAnswer>, GetSecurityAnswersError> =
        withContext(Dispatchers.IO) {
            authComponent.provideGetSecurityAnswersNetworkCall().execute(params)
        }

    override fun logoutEvent(): Flow<Boolean> = tokenRepository.logoutEvent()

    override fun removeUserData() {
        tokenRepository.removeUserToken()
        tokenRepository.clearTimeWhenUserTokenWasFetched()
        tokenRepository.removeLoginStatus()
        userDetailsRepository.removeEmail()
    }

    override fun sendLogoutEvent(isUserTokenExpired: Boolean) {
        tokenRepository.sendLogoutEvent(isUserTokenExpired)
    }

    override fun removeAccessToken() = tokenRepository.removeAccessToken()

    override fun getTimeWhenUserTokenWasFetched(): Long =
        tokenRepository.getTimeWhenUserTokenWasFetched()

    override fun setTimeWhenUserTokenWasFetched(time: Long) {
        tokenRepository.setTimeWhenUserTokenWasFetched(time)
    }

    override suspend fun logout(): LfResult<Unit?, LogoutError> = withContext(Dispatchers.IO) {
        authComponent.provideLogoutNetworkCall().execute()
    }

    override suspend fun exchangeSocialAccessTokenWithGlobeSocialToken(
        token: String,
        provider: String
    ): LfResult<String, ExchangeTokenError> = withContext(Dispatchers.IO) {
        authComponent.provideExchangeSocialAccessTokenWithGlobeSocialTokenNetworkCall()
            .execute(token, provider)
    }

    override suspend fun validateSimSerial(params: ValidateSimSerialParams): LfResult<ValidateSimSerialResponse, ValidateSimSerialPairingError> =
        withContext(Dispatchers.IO) {
            authComponent.provideValidateSimSerialNetworkCall().execute(params)
        }

    override suspend fun setSymmetricKey() {
        withContext(Dispatchers.IO) {
            if (tokenRepository.getSymmetricKey() == null) {
                authComponent.provideGetCognitoAccessNetworkCall().execute().onSuccess {
                    authComponent.provideGetSymmetricKeyNetworkCall().execute(it.access_token)
                        .onSuccess { result ->
                            tokenRepository.setSymmetricKey(result.secretKey)
                        }
                }
            }
        }
    }

    override suspend fun getSymmetricKey(): String? =
        withContext(Dispatchers.IO) { tokenRepository.getSymmetricKey() }
}
