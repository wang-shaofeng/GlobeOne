/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network_components

import ph.com.globe.model.network.NetworkConnectionStatus
import javax.inject.Inject

class TestNetworkStatusProvider : NetworkStatusProvider {
    override fun provideNetworkConnectionStatus(): NetworkConnectionStatus =
        NetworkConnectionStatus.ConnectedToMobileData
}
