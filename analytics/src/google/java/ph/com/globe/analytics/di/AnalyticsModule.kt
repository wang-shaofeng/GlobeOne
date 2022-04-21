/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.GlobeFirebaseAnalytics
import ph.com.globe.analytics.logger.UxLogger
import ph.com.globe.analytics.logger.FirebaseUxLogger

@Module
@InstallIn(SingletonComponent::class)
class AnalyticsProvidesModule {

    @Provides
    fun provideAnalyticsLogger(@ApplicationContext context: Context): GlobeAnalyticsLogger =
        GlobeAnalyticsLogger(
            listOf(GlobeFirebaseAnalytics(context))
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindsModule {

    @Binds
    abstract fun bindUxLogger(firebaseUxLogger: FirebaseUxLogger): UxLogger
}
