/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import ph.com.globe.analytics.di.AnalyticsBindsModule
import ph.com.globe.analytics.di.AnalyticsProvidesModule
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.UxLogger

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsProvidesModule::class]
)
class TestAnalyticsProvidesModule {

    @Provides
    fun provideAnalyticsLogger(): GlobeAnalyticsLogger =
        GlobeAnalyticsLogger(
            listOf()
        )
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsBindsModule::class]
)
abstract class TestAnalyticsBindsModule {

    @Binds
    abstract fun bindUxLogger(testLogger: TestUxLogger): UxLogger
}
