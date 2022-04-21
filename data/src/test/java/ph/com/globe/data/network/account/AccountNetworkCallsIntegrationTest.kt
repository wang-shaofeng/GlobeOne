/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.data.network.util.loginWithEmail
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.model.account.EnrollAccountParams
import ph.com.globe.model.account.ENROLL_ACCOUNT_CHANNEL
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment

internal class AccountNetworkCallsIntegrationTest {

    private lateinit var authManager: AuthDataManager
    private lateinit var accountManager: AccountDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        with(dataManagers) {
            authManager = getAuthDataManager()

            accountManager = getAccountDataManager()
        }
    }

    @Test
    @Ignore("Ignored until we can get proper test params.")
    fun `get account brand`(): Unit = runBlocking {
        authManager.loginWithEmail()
        val params = GetAccountBrandParams(
            "9271014487"
        )

        val result = accountManager.getAccountBrand(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore("Ignored because it can be done only once for given params.")
    fun `enroll accounts`(): Unit = runBlocking {
        authManager.loginWithEmail()
        val accountToEnroll =
            EnrollAccountParams(
                "",
                "9271014488",
                "John",
                AccountBrandType.Prepaid,
                AccountSegment.Mobile,
                listOf(ENROLL_ACCOUNT_CHANNEL)
            )

        val result = accountManager.enrollAccounts(accountToEnroll)

        assertThat(result.successOrNull()).isNotNull
    }
}
