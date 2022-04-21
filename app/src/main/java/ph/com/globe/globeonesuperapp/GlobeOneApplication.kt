/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ph.com.globe.analytics.logger.CompositeUxLogger
import ph.com.globe.analytics.logger.UxLogger
import javax.inject.Inject

@HiltAndroidApp
class GlobeOneApplication : Application() {

    @Inject
    lateinit var uxLogger: UxLogger

    override fun onCreate() {
        super.onCreate()

        CompositeUxLogger.installLogger(uxLogger)
    }
}
