/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners.repositories

import dagger.Binds
import dagger.Module
import ph.com.globe.data.ManagerScope

@Module
internal interface BannersRepositoryModule {
    @Binds
    @ManagerScope
    fun bindBannersRepository(bannersRepository: DefaultBannersRepository): BannersRepository
}
