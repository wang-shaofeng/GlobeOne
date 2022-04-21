/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.util

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.errors.NetworkError

internal fun HasLogTag.logSuccessfulNetworkCall() =
    dLog("network call successful.")

internal fun HasLogTag.logFailedNetworkCall(error: NetworkError) =
    dLog("network call failed. Reason: $error")

internal fun HasLogTag.logFailedToCreateAuthHeader() =
    dLog("failed to retrieve authenticated header.")

internal fun HasLogTag.logFailedToFetchAccessToken() =
    dLog("failed to fetch access token.")
