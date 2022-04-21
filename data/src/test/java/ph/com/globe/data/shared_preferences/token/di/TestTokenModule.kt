/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.token.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ph.com.globe.data.shared_preferences.token.InMemoryTokenRepository
import ph.com.globe.data.shared_preferences.token.TokenRepository

@Module
@InstallIn(SingletonComponent::class)
internal object TestTokenModule {
    @Provides
    fun bindTokenRepository(): TokenRepository = InMemoryTokenRepository
}
