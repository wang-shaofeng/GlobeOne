/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.token.di

import android.content.Context
import android.content.SharedPreferences
import ph.com.globe.data.shared_preferences.token.DefaultTokenRepository
import ph.com.globe.data.shared_preferences.token.TokenRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import ph.com.globe.data.DataScope
import javax.inject.Named

@Module(includes = [TokenPersistModule::class])
internal interface TokenModule {

    @Binds
    @DataScope
    fun bindTokenRepository(tokenRepository: DefaultTokenRepository): TokenRepository
}

@Module
internal object TokenPersistModule {

    @Provides
    @Named(SHARED_PREFS_TOKEN_KEY)
    fun provideSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_TOKEN_KEY, Context.MODE_PRIVATE)
}

internal const val SHARED_PREFS_TOKEN_KEY = "globe_token_shared_prefs"
