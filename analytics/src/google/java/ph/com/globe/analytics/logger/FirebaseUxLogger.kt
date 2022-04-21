/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * [UxLogger] for Firebase analytics.
 */
class FirebaseUxLogger @Inject constructor() : UxLogger {

    /**
     * Logs a [message] to Firebase Crashlytics' log. These messages will not be shown unless there is
     * a crash in the app, or a non fatal exception occurs and is logged (see [eLog]). In order for these
     * logs to appear on the Crashlytics dashboard, the user must enter the app after the said crash or
     * non fatal exception has occurred.
     */
    override fun dLog(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    /**
     * Logs the [exception] as non-fatal exception.
     */
    override fun eLog(exception: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(exception)
    }
}
