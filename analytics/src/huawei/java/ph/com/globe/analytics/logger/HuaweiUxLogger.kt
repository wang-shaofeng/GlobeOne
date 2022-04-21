/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

import android.util.Log
import javax.inject.Inject

/**
 * [UxLogger] for Huawei analytics.
 */
// TODO implement this once Huawei integration is available
class HuaweiUxLogger @Inject constructor() : UxLogger {

    override fun dLog(message: String) = Unit

    override fun eLog(exception: Throwable) = Unit
}
