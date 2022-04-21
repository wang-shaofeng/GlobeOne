/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.catalog.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.catalog.usecase.*

@Module(subcomponents = [CatalogComponent::class])
internal interface CatalogModule

@ManagerScope
@Subcomponent
interface CatalogComponent {

    fun provideContentSubscriptionStatusUseCase(): ContentSubscriptionStatusUseCase

    fun provideProvisionContentPromoUseCase(): ProvisionContentPromoUseCase

    fun provideUnsubscribeContentPromoUseCase(): UnsubscribeContentPromoUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): CatalogComponent
    }
}
