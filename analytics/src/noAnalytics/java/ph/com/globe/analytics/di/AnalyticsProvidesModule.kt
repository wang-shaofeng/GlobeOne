/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.GlobeNoAnalytics
import ph.com.globe.analytics.logger.NoAnalyticsUxLogger
import ph.com.globe.analytics.logger.UxLogger

@Module
@InstallIn(SingletonComponent::class)
class AnalyticsProvidesModule {

    @Provides
    fun provideGlobeAnalyticsLogger(globeNoAnalytics: GlobeNoAnalytics): GlobeAnalyticsLogger =
        GlobeAnalyticsLogger(
            listOf(globeNoAnalytics)
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindsModule {

    @Binds
    abstract fun bindUxLogger(noAnalyticsUxLogger: NoAnalyticsUxLogger): UxLogger
}
