/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.social_sign_in_controller

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocialSignInControllerModule {
    @Provides
    fun provideSocialSignInController(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): SocialSignInController {
        return DefaultSocialSignInController(
            facebookSingInController = FacebookSingInController(context),
            googleSignInController = GoogleSignInController(context),
            sharedPreferences
        )
    }
}
