/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.session.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ph.com.globe.data.DataScope
import javax.inject.Named

@Module
internal object UserSessionSharedPrefModule {

    @Provides
    @DataScope
    @Named(SHARED_PREFS_SESSION_KEY)
    fun provideSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_SESSION_KEY, Context.MODE_PRIVATE)
}

internal const val SHARED_PREFS_SESSION_KEY = "globe_session_shared_prefs"
