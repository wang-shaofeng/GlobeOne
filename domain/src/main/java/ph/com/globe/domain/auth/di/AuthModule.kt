/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.auth.usecase.*

@Module(subcomponents = [AuthComponent::class])
internal interface AuthModule

@ManagerScope
@Subcomponent
interface AuthComponent {

    fun provideLoginEmailUseCase(): LoginEmailUseCase

    fun provideRegisterEmailUseCase(): RegisterEmailUseCase

    fun provideRegisterSocialUseCase(): RegisterSocialUseCase

    fun provideRequestPasswordResetUseCase(): RequestPasswordResetUseCase

    fun provideLoginSocialUseCase(): LoginSocialUseCase

    fun provideAcceptUserAgreementUseCase(): AcceptUserAgreementUseCase

    fun provideSendOtpUseCase(): SendOtpUseCase

    fun provideVerifyOtpUseCase(): VerifyOtpUseCase

    fun provideGetOtpUseCase(): GetOtpUseCase

    fun provideGetSecurityQuestionsUseCase(): GetSecurityQuestionsUseCase

    fun provideValidateSecurityAnswersUseCase(): ValidateSecurityAnswersUseCase

    fun provideGetSecurityAnswersUseCase(): GetSecurityAnswersUseCase

    fun provideGetEmailStatusUseCase(): GetLoginStatusUseCase

    fun provideLogoutUseCase(): LogoutUseCase

    fun provideLogoutEventUseCase(): LogoutEventUseCase

    fun provideForceLogoutUseCase(): ForceLogoutUseCase

    fun provideRemoveUserDataIfRefreshUserTokenExpiredUseCase(): RemoveUserDataIfRefreshUserTokenExpiredUseCase

    fun provideExchangeSocialAccessTokenWithGlobeSocialTokenUseCase(): ExchangeSocialAccessTokenWithGlobeSocialTokenUseCase

    fun provideValidateSimSerialUseCase(): ValidateSimSerialUseCase

    fun provideFetchSymmetricUseCase(): SetSymmetricKeyUseCase

    fun provideGetSymmetricUseCase(): GetSymmetricKeyUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }
}
