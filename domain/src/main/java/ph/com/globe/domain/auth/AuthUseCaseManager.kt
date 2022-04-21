/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.auth.di.AuthComponent
import ph.com.globe.errors.account.GetSecurityAnswersError
import ph.com.globe.errors.account.GetSecurityQuestionsError
import ph.com.globe.errors.auth.*
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class AuthUseCaseManager @Inject constructor(
    factory: AuthComponent.Factory
) : AuthDomainManager {

    private val authComponent: AuthComponent = factory.create()

    override fun getLoginStatus(): LoginStatus =
        authComponent.provideGetEmailStatusUseCase().execute()

    override suspend fun loginEmail(params: LoginEmailParams): LfResult<LoginResult, LoginError> =
        withContext(Dispatchers.IO) { authComponent.provideLoginEmailUseCase().execute(params) }

    override suspend fun requestResetPassword(params: RequestResetPasswordParams): LfResult<Unit, RequestResetPasswordError> =
        withContext(Dispatchers.IO) {
            authComponent.provideRequestPasswordResetUseCase().execute(params)
        }

    override suspend fun registerEmail(params: RegisterEmailParams): LfResult<RegisterEmailResult, RegisterError> =
        withContext(Dispatchers.IO) { authComponent.provideRegisterEmailUseCase().execute(params) }

    override suspend fun registerSocial(params: RegisterSocialParams): LfResult<RegisterSocialResult, RegisterError> =
        withContext(Dispatchers.IO) { authComponent.provideRegisterSocialUseCase().execute(params) }

    override suspend fun acceptUserAgreement(params: AcceptUserAgreementParams): LfResult<Unit, AcceptUserAgreementError> =
        withContext(Dispatchers.IO) {
            authComponent.provideAcceptUserAgreementUseCase().execute(params)
        }

    override suspend fun loginSocial(params: LoginSocialParams): LfResult<LoginSocialResult, LoginError> =
        withContext(Dispatchers.IO) { authComponent.provideLoginSocialUseCase().execute(params) }

    override suspend fun sendOtp(params: SendOtpParams): LfResult<SendOtpResult, SendOtpError> =
        withContext(Dispatchers.IO) { authComponent.provideSendOtpUseCase().execute(params) }

    override suspend fun verifyOtp(params: VerifyOtpParams): LfResult<VerifyOtpResult, VerifyOtpError> =
        withContext(Dispatchers.IO) { authComponent.provideVerifyOtpUseCase().execute(params) }

    override suspend fun getOtp(params: GetOtpParams): LfResult<GetOtpResult, GetOtpError> =
        withContext(Dispatchers.IO) { authComponent.provideGetOtpUseCase().execute(params) }

    override suspend fun getSecurityQuestions(params: GetSecurityQuestionsParams): LfResult<GetSecurityQuestionsResult, GetSecurityQuestionsError> =
        withContext(Dispatchers.IO) {
            authComponent.provideGetSecurityQuestionsUseCase().execute(params)
        }

    override suspend fun validateSecurityAnswers(params: ValidateSecurityAnswersParams): LfResult<Unit, ValidateSecurityAnswersError> =
        withContext(Dispatchers.IO) {
            authComponent.provideValidateSecurityAnswersUseCase().execute(params)
        }

    override suspend fun getSecurityAnswers(params: GetSecurityAnswersParams): LfResult<List<SecurityAnswer>, GetSecurityAnswersError> =
        withContext(Dispatchers.IO) {
            authComponent.provideGetSecurityAnswersUseCase().execute(params)
        }

    override suspend fun logout(): LfResult<Unit?, LogoutError> = withContext(Dispatchers.Default) {
        authComponent.provideLogoutUseCase().execute()
    }

    override suspend fun forceLogout() = withContext(Dispatchers.Default) {
        authComponent.provideForceLogoutUseCase().execute()
    }

    override fun logoutEvent(): Flow<Boolean> = authComponent.provideLogoutEventUseCase().get()

    override fun removeUserDataIfRefreshUserTokenExpired() =
        authComponent.provideRemoveUserDataIfRefreshUserTokenExpiredUseCase().execute()

    override suspend fun exchangeSocialAccessTokenWithGlobeSocialToken(
        token: String,
        provider: String
    ): LfResult<String, ExchangeTokenError> = withContext(Dispatchers.IO) {
        authComponent.provideExchangeSocialAccessTokenWithGlobeSocialTokenUseCase()
            .execute(token, provider)
    }

    override suspend fun validateSimSerial(params: ValidateSimSerialParams): LfResult<ValidateSimSerialResponse, ValidateSimSerialPairingError> =
        withContext(Dispatchers.IO) {
            authComponent.provideValidateSimSerialUseCase().execute(params)
        }

    override suspend fun setSymmetricKey() {
        withContext(Dispatchers.IO) {
            authComponent.provideFetchSymmetricUseCase().execute()
        }
    }

    override suspend fun getSymmetricKey() {
        withContext(Dispatchers.IO) {
            authComponent.provideGetSymmetricUseCase().execute()
        }
    }
}
