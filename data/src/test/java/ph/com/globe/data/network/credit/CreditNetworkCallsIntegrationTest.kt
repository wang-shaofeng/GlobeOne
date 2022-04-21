/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.credit

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.credit.CreditDataManager
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.model.credit.LoanPromoRequest

internal class CreditNetworkCallsIntegrationTest {

    private lateinit var creditManager: CreditDataManager
    private lateinit var authManager: AuthDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        with(dataManagers) {
            creditManager = getCreditDataManager()
            authManager = getAuthDataManager()
        }
    }

    @Test
    @Ignore("Currently we don't have a number without a loan")
    fun `get credit info no loan`(): Unit = runBlocking {
        val params =
            GetCreditInfoParams("5c7d805b-ec2c-4803-8072-5c403e0d95c7", "9271014487")

        val result = creditManager.getCreditInfo(params)

        Assertions.assertThat(result.errorOrNull()).isNotNull
    }

    @Test
    @Ignore("Currently, we don't have any number with a loan")
    fun `get credit info has loan`(): Unit = runBlocking {
        val params =
            GetCreditInfoParams("5c7d805b-ec2c-4803-8072-5c403e0d95c7", "9271014487")

        val result = creditManager.getCreditInfo(params)

        Assertions.assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore("Currently we don't have a number without a loan")
    fun `loan promo`(): Unit = runBlocking {
        val params = LoanPromoParams(
            "5c7d805b-ec2c-4803-8072-5c403e0d95c7",
            LoanPromoRequest("GOSURF130", "123456", "9271014487")
        )

        val result = creditManager.loanPromo(params)

        Assertions.assertThat(result.successOrNull()).isNotNull
    }
}
