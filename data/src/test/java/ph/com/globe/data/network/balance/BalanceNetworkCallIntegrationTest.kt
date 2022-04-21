/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.balance

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.data.network.util.loginWithEmail
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.balance.BalanceDataManager
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams

class BalanceNetworkCallIntegrationTest {

    private lateinit var balanceManager: BalanceDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        with(dataManagers) {
            balanceManager = getBalanceDataManager()
        }
    }

    @Test
    @Ignore("Ignored until we get test msisdn")
    fun `check balance sufficiency`(): Unit = runBlocking {

        val result = balanceManager.checkBalanceSufficiency(
            CheckBalanceSufficiencyParams(
                "091014487",
                "100.0"
            )
        )

        assertThat(result.successOrNull()).isNotNull
    }
}
