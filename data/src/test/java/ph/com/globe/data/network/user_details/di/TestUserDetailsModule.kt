/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.user_details.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ph.com.globe.data.network.user_details.InMemoryUserDetailsRepository
import ph.com.globe.data.network.user_details.UserDetailsRepository

@Module
@InstallIn(SingletonComponent::class)
internal object TestUserDetailsModule {
    @Provides
    fun bindUserDetailsRepository(): UserDetailsRepository = InMemoryUserDetailsRepository
}
