/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.di

import ph.com.globe.analytics.logger.UxLogger
import javax.inject.Inject

class TestUxLogger @Inject constructor() : UxLogger {

    override fun dLog(message: String) = Unit

    override fun eLog(exception: Throwable) = Unit

}
