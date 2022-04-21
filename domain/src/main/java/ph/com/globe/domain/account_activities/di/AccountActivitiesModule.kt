/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account_activities.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.account_activities.GetRewardsHistoryUseCase

@Module(subcomponents = [AccountActivitiesComponent::class])
internal interface AccountActivitiesModule

@ManagerScope
@Subcomponent
interface AccountActivitiesComponent {

    fun provideGetRewardsHistoryUseCase(): GetRewardsHistoryUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): AccountActivitiesComponent
    }
}
