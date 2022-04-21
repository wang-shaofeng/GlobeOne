/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ph.com.globe.util.LfResult
import javax.inject.Inject

/**
 * Repository updater for network calls that take in no parameters. Its [update] method makes sure
 * that only one repository update is undergoing at any certain time, which stops excess network calls.
 * [Resp] represents the response data of the network call itself, while [Err] represents the error
 * that the network call may return.
 */
class ParameterlessRepoUpdater<Resp, Err> @Inject constructor() {

    var isUpdateOngoing = false
        private set
    private val mutex = Mutex()

    /**
     * Method that calls [networkCall] which fetches fresh data from the network and writes it into
     * database using [databaseInsertionCall]. It makes sure that there is only one undergoing update
     * at a time.
     * @returns null if the [networkCall] was successful, [Err] otherwise.
     */
    suspend fun update(
        networkCall: ParameterlessNetworkCall<Resp, Err>,
        databaseInsertionCall: DatabaseInsertionCall<Resp>
    ): Err? {

        if (!isUpdateOngoing) {

            mutex.withLock {

                if (!isUpdateOngoing) {
                    isUpdateOngoing = true

                    val result = try {
                        networkCall.invoke()
                    } catch (e: Exception) {
                        isUpdateOngoing = false
                        throw e
                    }

                    result.successOrNull()?.let { response ->
                        databaseInsertionCall.invoke(response)
                        isUpdateOngoing = false
                    } ?: run {
                        // Allowing further updates because the current one failed
                        isUpdateOngoing = false
                        return result.error
                    }
                }
            }
        }

        return null
    }
}

typealias ParameterlessNetworkCall<Resp, Err> = suspend () -> LfResult<Resp, Err>
