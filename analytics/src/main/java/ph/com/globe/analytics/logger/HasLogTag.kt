/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.logger

/**
 * Marks implementors as having [logTag] field, that can be used for logging purposes.
 */
interface HasLogTag {
    val logTag: String
}

object CompositeUxLogger : UxLogger {

    private val loggers: MutableList<UxLogger> = mutableListOf()

    @Synchronized
    fun installLogger(uxLogger: UxLogger) {
        if (uxLogger == this) {
            throw (IllegalStateException("Composite logger cannot install itself."))
        }

        loggers.add(uxLogger)
    }

    override fun dLog(message: String) {
        loggers.forEach {
            it.dLog(message)
        }
    }

    override fun eLog(exception: Throwable) {
        loggers.forEach {
            it.eLog(exception)
        }
    }
}

fun HasLogTag.dLog(message: String) =
    CompositeUxLogger.dLog("$logTag $message")

fun HasLogTag.eLog(e: Exception) =
    CompositeUxLogger.eLog(e)
