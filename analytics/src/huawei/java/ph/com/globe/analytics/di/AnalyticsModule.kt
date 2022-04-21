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
import ph.com.globe.analytics.logger.GlobeHuaweiAnalytics
import ph.com.globe.analytics.logger.HuaweiUxLogger
import ph.com.globe.analytics.logger.UxLogger

@Module
@InstallIn(SingletonComponent::class)
class AnalyticsProvidesModule {

    @Provides
    fun provideAnalyticsLogger(globeHuaweiAnalytics: GlobeHuaweiAnalytics): GlobeAnalyticsLogger =
        GlobeAnalyticsLogger(
            listOf(globeHuaweiAnalytics)
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindsModule {

    @Binds
    abstract fun bindUxLogger(huaweiUxLogger: HuaweiUxLogger): UxLogger
}
