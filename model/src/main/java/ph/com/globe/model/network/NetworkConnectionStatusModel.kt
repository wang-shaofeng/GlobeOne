/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.network

sealed class NetworkConnectionStatus {
    object ConnectedToMobileData: NetworkConnectionStatus()
    object ConnectedToWifi: NetworkConnectionStatus()
    object NotConnectedToInternet: NetworkConnectionStatus()
    object Other: NetworkConnectionStatus()
}
