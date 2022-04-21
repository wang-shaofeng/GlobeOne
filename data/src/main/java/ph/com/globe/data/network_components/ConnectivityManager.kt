/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network_components

import ph.com.globe.domain.connectivity.ConnectivityDataManager
import ph.com.globe.model.network.NetworkConnectionStatus
import javax.inject.Inject

class ConnectivityManager @Inject constructor(
    private val networkStatusProvider: NetworkStatusProvider
) : ConnectivityDataManager {
    override fun getNetworkStatus(): NetworkConnectionStatus =
        networkStatusProvider.provideNetworkConnectionStatus()
}
