/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.rush.usecases.GetGameVouchersUrlUseCase
import ph.com.globe.domain.rush.usecases.GetSpinwheelUrlUseCase
import ph.com.globe.domain.rush.usecases.ShouldShowSpinwheelButtonUseCase

@Module(subcomponents = [RushComponent::class])
internal interface RushModule

@ManagerScope
@Subcomponent
interface RushComponent {

    fun shouldShowSpinwheelButton(): ShouldShowSpinwheelButtonUseCase

    fun getSpinwheelUrl(): GetSpinwheelUrlUseCase

    fun getGameVouchersUrl(): GetGameVouchersUrlUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): RushComponent
    }
}
