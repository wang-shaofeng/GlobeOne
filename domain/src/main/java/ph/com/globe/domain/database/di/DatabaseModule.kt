/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.database.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.database.ClearAllDataUseCase

@Module(subcomponents = [DatabaseComponent::class])
internal interface DatabaseModule

@ManagerScope
@Subcomponent
interface DatabaseComponent {

    fun provideClearAllDataUseCase(): ClearAllDataUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): DatabaseComponent
    }
}
