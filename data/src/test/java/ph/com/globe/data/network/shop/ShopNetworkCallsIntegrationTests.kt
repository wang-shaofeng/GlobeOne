/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.model.auth.LoginEmailParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.util.fold

internal class ShopNetworkCallsIntegrationTests {

    private lateinit var shopManager: ShopDataManager
    private lateinit var authManager: AuthDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        with(dataManagers) {
            shopManager = getShopDataManager()
            authManager = getAuthDataManager()
        }
    }

    @Test
    @Ignore("Ignored because stagging is not ready yet")
    fun `get all offers`(): Unit = runBlocking {

        val result = shopManager.fetchData()

        Assertions.assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore("Ignored")
    fun `validate retailer`(): Unit = runBlocking {
        var isRetailerResult1 = false
        var isRetailerResult2 = false
        shopManager.validateRetailer("09971182065").fold({ isRetailer ->
            isRetailerResult1 = isRetailer
        }, {})
        shopManager.validateRetailer("09271014487").fold({ isRetailer ->
            isRetailerResult2 = isRetailer
        }, {})
        Assertions.assertThat(isRetailerResult1).isTrue
        Assertions.assertThat(isRetailerResult2).isFalse()
    }

    @Test
    @Ignore("Ignored because of the production")
    fun `get promo subscription history`(): Unit = runBlocking {
        val params = GetPromoSubscriptionHistoryParams("09271014487")

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "ognjen.bogicevic+25@lotusflare.com",
                "Test1234!"
            )
        )
        val result = shopManager.getPromoSubscriptionHistory(params)

        Assertions.assertThat(result.successOrNull()).isNotNull
    }
}
