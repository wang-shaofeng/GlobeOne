/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network_components.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import ph.com.globe.data.network_components.DefaultNetworkStatusProvider
import ph.com.globe.data.network_components.NetworkStatusProvider

@Module
interface NetworkStatusModule {

    @Binds
    fun bindNetworkStateProvider(defaultNetworkStatusProvider: DefaultNetworkStatusProvider): NetworkStatusProvider
}
