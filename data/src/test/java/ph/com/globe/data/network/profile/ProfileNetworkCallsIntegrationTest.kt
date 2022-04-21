/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.data.network.util.loginWithEmail
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.profile.ProfileDataManager

class ProfileNetworkCallsIntegrationTest {

    private lateinit var authManager: AuthDataManager
    private lateinit var profileManager: ProfileDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        with(dataManagers) {
            authManager = getAuthDataManager()

            profileManager = getProfileDataManager()
        }
    }

    @Test
    @Ignore("Ignored until user enrolls accounts")
    fun `get enrolled accounts`(): Unit = runBlocking {

        authManager.loginWithEmail()

        val result = profileManager.getEnrolledAccounts()

        assertThat(result.successOrNull()).isNotNull
    }

}
