/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.shop.usecases.*

@Module(subcomponents = [ShopComponent::class])
internal interface ShopModule

@ManagerScope
@Subcomponent
interface ShopComponent {

    fun provideFetchOffersUseCase(): FetchOffersUseCase

    fun provideGetPromosUseCase(): GetPromosUseCase

    fun provideGetAllOffersUseCase(): GetAllOffersUseCase

    fun provideGetLoanableUseCase(): GetLoanableUseCase

    fun provideGetContentPromosUseCase(): GetContentPromosUseCase

    fun provideValidateRetailerUseCase(): ValidateRetailerUseCase

    fun provideGetPromoSubscriptionHistoryUseCase(): GetPromoSubscriptionHistoryUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): ShopComponent
    }
}
