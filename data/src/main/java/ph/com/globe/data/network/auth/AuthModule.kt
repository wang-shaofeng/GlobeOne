/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.G2_ENCRYPTION_SERVER
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.GLOBE_SIGN_IN_SERVER
import ph.com.globe.data.network.auth.calls.*
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [AuthComponent::class])
internal interface AuthModule

@ManagerScope
@Subcomponent(modules = [AuthProvidesModule::class])
interface AuthComponent {

    fun provideGetAccessTokenNetworkCall(): GetAccessTokenNetworkCall

    fun provideLoginEmailNetworkCall(): LoginEmailNetworkCall

    fun provideRegisterUserNetworkCall(): RegisterUserNetworkCall

    fun provideLoginSocialNetworkCall(): LoginSocialNetworkCall

    fun provideRequestResetPasswordNetworkCall(): RequestResetPasswordNetworkCall

    fun provideSendOtpNetworkCall(): SendOtpNetworkCall

    fun provideVerifyOtpNetworkCall(): VerifyOtpNetworkCall

    fun provideRegisterEmailNetworkCall(): RegisterEmailNetworkCall

    fun provideGetOtpNetworkCall(): GetOtpNetworkCall

    fun provideGetSecurityQuestionsNetworkCall(): GetSecurityQuestionsNetworkCall

    fun provideValidateSecurityAnswersNetworkCall(): ValidateSecurityAnswersNetworkCall

    fun provideGetSecurityAnswersNetworkCall(): GetSecurityAnswersNetworkCall

    fun provideAcceptUserAgreementNetworkCall(): AcceptUserAgreementNetworkCall

    fun provideRegisterSocialNetworkCall(): RegisterSocialNetworkCall

    fun provideRefreshUserTokenNetworkCall(): RefreshUserTokenNetworkCall

    fun provideLogoutNetworkCall(): LogoutNetworkCall

    fun provideExchangeSocialAccessTokenWithGlobeSocialTokenNetworkCall(): ExchangeSocialAccessTokenWithGlobeSocialTokenNetworkCall

    fun provideValidateSimSerialNetworkCall() : ValidateSimSerialNetworkCall

    fun provideGetCognitoAccessNetworkCall() : GetCognitoAccessTokenNetworkCall

    fun provideGetSymmetricKeyNetworkCall() : GetSymmetricKeyNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }
}

@Module
internal object AuthProvidesModule {

    @Provides
    @ManagerScope
    fun providesCognitoRetrofit(@Named(G2_ENCRYPTION_SERVER) retrofit: Retrofit): G2EncryptionRetrofit =
        retrofit.create(G2EncryptionRetrofit::class.java)

    @Provides
    @ManagerScope
    fun providesAuthRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): AuthRetrofit =
        retrofit.create(AuthRetrofit::class.java)

    @Provides
    @ManagerScope
    fun providesSignInRetrofit(@Named(GLOBE_SIGN_IN_SERVER) retrofit: Retrofit): SignInRetrofit =
        retrofit.create(SignInRetrofit::class.java)
}
