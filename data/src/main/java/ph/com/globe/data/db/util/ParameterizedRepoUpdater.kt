/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.util

import ph.com.globe.util.LfResult
import javax.inject.Inject

/**
 * Repository updater for network calls that take in parameters. Its [update] method makes sure that
 * only one repository update is undergoing at any certain time, which stops excess network calls.
 * [Param] represents the parameters that the network call takes in, [Resp] represents the response
 * data of the network call itself, while [Err] represents the error that the network call may return.
 */
class ParameterizedRepoUpdater<Param, Resp, Err> @Inject constructor() {

    private val paramsUnderUpdate = mutableListOf<Param>()

    /**
     * Method that calls [networkCall] which fetches fresh data from the network and writes it into
     * database using [databaseInsertionCall]. It makes sure that there is only one undergoing update
     * at a time.
     * @returns null if the [networkCall] was successful, [Err] otherwise.
     */
    @Synchronized
    suspend fun update(
        params: Param,
        networkCall: ParameterizedNetworkCall<Param, Resp, Err>,
        databaseInsertionCall: DatabaseInsertionCall<Resp>
    ): Err? {
        if (paramsUnderUpdate.contains(params).not()) {
            paramsUnderUpdate.add(params)

            val result = networkCall.invoke(params)

            result.successOrNull()?.let { response ->
                databaseInsertionCall.invoke(response)
                paramsUnderUpdate.remove(params)
            } ?: kotlin.run {
                // Allowing further updates because the current one failed
                paramsUnderUpdate.remove(params)
                return result.error
            }
        }

        return null
    }

}

typealias ParameterizedNetworkCall<Param, Resp, Err> = suspend (Param) -> LfResult<Resp, Err>
typealias DatabaseInsertionCall<Resp> = suspend (Resp) -> Unit
