/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.utils

import kotlinx.coroutines.delay
import ph.com.globe.analytics.logger.CompositeUxLogger.dLog
import ph.com.globe.util.LfResult
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess

/**
 * Used to poll the NetworkCalls that have return type of [LfResult]
 *
 * @param params The parameter to be used as an argument to call [pollingCall]
 * @param pollingCall The network call that is to be polled. It is created as a type of [LfNetworkCall]
 * @param successCheck The optional parameter that is used within [onSuccess] block and performs additional checks
 * if needed. For exp. if you want to keep polling a certain network call even if you are receiving successful
 * result but expecting a certain value within the result body: {result -> (result as MyResultType).body.contains("somethingOfValue")}
 * @param pollingCounter The optional parameter set in case we want to poll the call, custom number of times. default value is
 * [DEFAULT_POLLING_REPETITIONS]
 * @param pollingDelay The optional parameter set in case we want custom polling delay between the calls. default value is [DEFAULT_POLLING_DELAY]
 */
class LfNetworkCallPollHandler<T, K>(
    private val params: Any,
    private val pollingCall: LfNetworkCall<T, K>,
    private val successCheck: SuccessCheck = { _ -> true },
    private val stopPollingFailureCheck: StopPollingFailureCheck = { _ -> false },
    private var pollingCounter: Int = DEFAULT_POLLING_REPETITIONS,
    private var pollingDelay: Long = DEFAULT_POLLING_DELAY,
) {
    private lateinit var pollingResult: LfResult<T, K>

    /**
     * Called to start polling the provided [pollingCall]
     *
     * @return [LfResult]
     */
    suspend fun poll(): LfResult<T, K> {

        // Trying to get the positive result {pollingCounter} times.
        while (pollingCounter-- > 0) {
            dLog(" polling, remaining attempts $pollingCounter.")
            pollingResult = pollingCall(params)
            pollingResult.onSuccess { result ->
                if (successCheck(result as Any)) {
                    return pollingResult
                }
            }
            pollingResult.onFailure { error ->
                if (stopPollingFailureCheck(error as Any)) {
                    return pollingResult
                }
            }
            delay(pollingDelay)
        }
        return pollingResult
    }
}

typealias LfNetworkCall<T, K> = suspend (params: Any) -> LfResult<T, K>

typealias SuccessCheck = suspend (result: Any) -> Boolean
typealias StopPollingFailureCheck = suspend (error: Any) -> Boolean

const val DEFAULT_POLLING_REPETITIONS = 12
const val DEFAULT_POLLING_DELAY = 5000L
