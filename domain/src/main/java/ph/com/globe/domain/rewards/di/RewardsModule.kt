/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.rewards.usecases.*

@Module(subcomponents = [RewardsComponent::class])
internal interface RewardsModule

@ManagerScope
@Subcomponent
interface RewardsComponent {

    fun provideGetRewardPointsUseCase(): GetRewardPointsUseCase

    fun provideGetConversionQualificationUseCase(): GetConversionQualificationUseCase

    fun provideAddDataConversionUseCase(): AddDataConversionUseCase

    fun provideGetDataConversionDetailsUseCase(): GetDataConversionDetailsUseCase

    fun provideFetchRewardsCatalogUseCase(): FetchRewardsCatalogUseCase

    fun provideRandomRewardsFromEachCategoryUseCase(): GetRandomRewardsFromEachCategoryUseCase

    fun provideFreeRandomRewardsUseCase(): GetFreeRandomRewardsUseCase

    fun provideRewardsForCategoryUseCase(): GetRewardsForCategoryUseCase

    fun provideRandomFromEachCategoryDependsOnPointsUseCase(): GetRandomFromEachCategoryDependsOnPointsUseCase

    fun provideGetLoyaltyProgramIdFromSpecificMsisdnUseCase(): GetLoyaltyProgramIdFromSpecificMsisdnUseCase

    fun provideGetAllRewardsUseCase(): GetAllRewardsUseCase

    fun provideRedeemLoyaltyRewardsUseCase(): RedeemLoyaltyRewardsUseCase

    fun provideGetMerchantDetailsUseCase(): GetMerchantDetailsUseCase

    fun provideRedeemPointsUseCase(): RedeemPointsUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): RewardsComponent
    }
}
