/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.app_data.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.app_data.usecases.ClearDatabaseUseCase
import ph.com.globe.domain.app_data.usecases.FetchRegisteredUserUseCase
import ph.com.globe.domain.app_data.usecases.RefreshAccountDetailsDataUseCase

@Module(subcomponents = [AppDataComponent::class])
internal interface AppDataModule

@ManagerScope
@Subcomponent
interface AppDataComponent {

    fun provideFetchRegisteredUserUseCase(): FetchRegisteredUserUseCase

    fun provideClearDatabaseUseCase(): ClearDatabaseUseCase

    fun provideRefreshAccountDetailsDataUseCase(): RefreshAccountDetailsDataUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): AppDataComponent
    }
}
