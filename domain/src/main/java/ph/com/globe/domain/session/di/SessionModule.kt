/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.session.usecase.GetCurrentUserSessionIdUseCase
import ph.com.globe.domain.session.usecase.PauseUserSessionUseCase
import ph.com.globe.domain.session.usecase.StartUserSessionUseCase

@Module(subcomponents = [SessionComponent::class])
internal interface SessionModule

@ManagerScope
@Subcomponent
interface SessionComponent {

    fun provideStartSessionUseCase(): StartUserSessionUseCase

    fun providePauseSessionUseCase(): PauseUserSessionUseCase

    fun provideGetCurrentSessionIdUseCase(): GetCurrentUserSessionIdUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): SessionComponent
    }
}
