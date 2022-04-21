/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.profile.calls.*
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [ProfileComponent::class])
internal interface ProfileModule

@ManagerScope
@Subcomponent(modules = [ProfileProvidesModule::class])
interface ProfileComponent {

    fun provideGetRegisteredUserNetworkCall(): GetRegisteredUserNetworkCall

    fun provideGetEnrolledAccountsNetworkCall(): GetEnrolledAccountsNetworkCall

    fun provideGetCustomerDetailsNetworkCall(): GetCustomerDetailsNetworkCall

    fun provideGetCustomerInterestsNetworkCall(): GetCustomerInterestsNetworkCall

    fun provideGetERaffleEntriesNetworkCall(): GetERaffleEntriesNetworkCall

    fun provideAddCustomerInterestsNetworkCall(): AddCustomerInterestsNetworkCall

    fun provideUpdateUserProfileNetworkCall(): UpdateUserProfileNetworkCall

    fun provideSendVerificationEmailNetworkCall(): SendVerificationEmailNetworkCall

    fun provideVerifyEmailNetworkCall(): VerifyEmailNetworkCall

    fun provideCheckCompleteKYCNetworkCall(): CheckCompleteKYCNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): ProfileComponent
    }
}

@Module
internal object ProfileProvidesModule {

    @Provides
    @ManagerScope
    fun providesProfileRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): ProfileRetrofit =
        retrofit.create(ProfileRetrofit::class.java)
}
