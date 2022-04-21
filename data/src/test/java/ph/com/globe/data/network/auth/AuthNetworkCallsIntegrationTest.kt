/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.data.network.util.loginWithEmail
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.model.auth.*
import ph.com.globe.model.profile.response_models.RegisterUserParams
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment

internal class AuthNetworkCallsIntegrationTest {

    private lateinit var authManager: AuthDataManager
    private lateinit var profileManager: ProfileDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        authManager = dataManagers.getAuthDataManager()
        profileManager = dataManagers.getProfileDataManager()
    }

    @Test
    @Ignore("Temporarily ignored due to Bitrise failing")
    fun `get access token`(): Unit = runBlocking {

        val result = authManager.fetchAccessToken()

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("User is deleted at the moment. Unable to re-verify it's email.")
    @Test
    fun `login user with email`(): Unit = runBlocking {

        val result = authManager.loginWithEmail()

        assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore("OTP can be sent only three times a day per number")
    fun `send otp`(): Unit = runBlocking {
        val params = SendOtpParams(
            OtpType.SMS,
            "9271014489",
            listOf("EnrollAccount"),
            AccountBrandType.Prepaid,
            AccountSegment.Mobile
        )

        val result = authManager.sendOtp(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore
    fun `verify otp`(): Unit = runBlocking {

        val params = VerifyOtpParams(
            "cxs",
            "9271014489",
            "5c7d805b-ec2c-4803-8072-5c403e0d95c7",
            "219062",
            AccountBrandType.Prepaid,
            AccountSegment.Mobile,
            listOf("EnrollAccount")
        )

        val result = authManager.verifyOtp(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore("Need valid account for production")
    fun `get registered user`(): Unit = runBlocking {

        authManager.loginEmail(LoginEmailParams("globeregress1@gmail.com", "Globe@2019"))
        val result = profileManager.getRegisteredUser()

        assertThat(result.successOrNull()).isNotNull
    }

    @Test
    @Ignore("You can register only one user with this parameters")
    fun `register user`(): Unit = runBlocking {
        val params = RegisterUserParams(email = "globeregress1@gmail.com")

        authManager.loginEmail(LoginEmailParams("globeregress1@gmail.com", "Globe@2019"))
        val result = authManager.registerUser(params)

        assertThat(result.successOrNull()).isNotNull
    }
}
