/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.group.usecase.*

@Module(subcomponents = [GroupComponent::class])
internal interface GroupModule

@ManagerScope
@Subcomponent
interface GroupComponent {

    fun provideGetGroupListUseCase(): GetGroupListUseCase

    fun provideAddGroupMemberUseCase(): AddGroupMemberUseCase

    fun provideRetrieveGroupUsageUseCase(): RetrieveGroupUsageUseCase

    fun provideRetrieveMemberUsageUseCase(): RetrieveMemberUsageUseCase

    fun provideDeleteGroupMemberUseCase(): DeleteGroupMemberUseCase

    fun provideSetMemberUsageLimitUseCase(): SetMemberUsageLimitUseCase

    fun provideRetrieveGroupsAccountDetailsUseCase(): RetrieveGroupsAccountDetailsUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): GroupComponent
    }
}
