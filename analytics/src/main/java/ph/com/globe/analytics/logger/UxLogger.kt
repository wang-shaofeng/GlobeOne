/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

/**
 * Logger that is used to log user interactions and exceptions that are of importance when finding
 * the root cause of various exceptions that cause the app to crash.
 */
interface UxLogger {

    /**
     * Logs a given message.
     */
    fun dLog(message: String)

    /**
     * Logs exception.
     */
    fun eLog(exception: Throwable)
}
