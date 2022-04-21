/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.connectivity.usecases

import ph.com.globe.domain.connectivity.ConnectivityDataManager
import javax.inject.Inject

class ConnectivityUseCase @Inject constructor(
    val connectivityDataManager: ConnectivityDataManager
) {
    fun get() = connectivityDataManager.getNetworkStatus()
}
