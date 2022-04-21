/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.connectivity

import ph.com.globe.domain.connectivity.di.ConnectivityComponent
import ph.com.globe.model.network.NetworkConnectionStatus
import javax.inject.Inject

class ConnectivityUseCaseManager @Inject constructor(
    factory: ConnectivityComponent.Factory
) : ConnectivityDomainManager {

    private val component = factory.create()

    override fun getNetworkStatus(): NetworkConnectionStatus =
        component.provideConnectivityUseCase().get()
}
