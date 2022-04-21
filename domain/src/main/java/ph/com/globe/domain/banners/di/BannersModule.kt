/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.banners.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.banners.usecase.DashboardBannersUseCase

import ph.com.globe.domain.banners.usecase.FetchBannersUseCase

@Module(subcomponents = [BannersComponent::class])
internal interface BannersModule

@ManagerScope
@Subcomponent
interface BannersComponent {

    fun provideFetchBannersUseCase(): FetchBannersUseCase

    fun provideDashboardBannersUseCase(): DashboardBannersUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): BannersComponent
    }
}
