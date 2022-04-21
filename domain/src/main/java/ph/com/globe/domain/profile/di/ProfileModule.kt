/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.profile.usecases.*

@Module(subcomponents = [ProfileComponent::class])
internal interface ProfileModule

@ManagerScope
@Subcomponent
interface ProfileComponent {

    fun provideGetRegisteredUserUseCase(): GetRegisteredUserUseCase

    fun provideGetUserNicknameUseCase(): GetUserNicknameUseCase

    fun provideGetUserFirstNameUseCase(): GetUserFirstNameUseCase

    fun provideGetEnrolledAccountsUseCase(): GetEnrolledAccountsUseCase

    fun provideRefreshEnrolledAccountsUseCase(): RefreshEnrolledAccountsUseCase

    fun provideInvalidateEnrolledAccountsUseCase(): InvalidateEnrolledAccountsUseCase

    fun provideDeleteEnrolledAccountsUseCase(): DeleteEnrolledAccountsUseCase

    fun provideDeleteEnrolledAccountUseCase(): DeleteEnrolledAccountUseCase

    fun provideGetCustomerDetailsUseCase(): GetCustomerDetailsUseCase

    fun provideGetCustomerInterestsUseCase(): GetCustomerInterestsUseCase

    fun provideGetERaffleEntriesUseCase(): GetERaffleEntriesUseCase

    fun provideAddCustomerInterestsUseCase(): AddCustomerInterestsUseCase

    fun provideUpdateUserProfileUseCase(): UpdateUserProfileUseCase

    fun provideSendVerificationEmailUseCase(): SendVerificationEmailUseCase

    fun provideVerifyEmailUseCase(): VerifyEmailUseCase

    fun provideCheckCompleteKYCUseCase(): CheckCompleteKYCUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): ProfileComponent
    }
}
