/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.user_details.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.user_details.usecases.EncryptDataUseCase
import ph.com.globe.domain.user_details.usecases.GetEmailUseCase

@Module(subcomponents = [UserDetailsComponent::class])
internal interface UserDetailsModule

@ManagerScope
@Subcomponent
interface UserDetailsComponent {

    fun provideGetEmailUseCase(): GetEmailUseCase

    fun provideEncryptDataUseCase(): EncryptDataUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): UserDetailsComponent
    }
}
