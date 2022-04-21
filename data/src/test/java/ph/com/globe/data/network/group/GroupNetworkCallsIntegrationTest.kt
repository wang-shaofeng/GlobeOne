/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.group

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.data.network.DaggerTestDataComponent
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.model.auth.LoginEmailParams
import ph.com.globe.model.group.*

internal class GroupNetworkCallsIntegrationTest {

    private lateinit var groupManager: GroupDataManager
    private lateinit var authManager: AuthDataManager

    @Before
    fun setUp() {
        val dataManagers = DaggerTestDataComponent.create().provideDataManagers()

        with(dataManagers) {
            groupManager = getGroupDataManager()
            authManager = getAuthDataManager()
        }
    }

    @Ignore("Ignored until provided test data")
    @Test
    fun `get group list owner`(): Unit = runBlocking {
        val params = GetGroupListParams("Group", true)

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.getGroupList(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored until provided test data")
    @Test
    fun `get group list`(): Unit = runBlocking {
        val params = GetGroupListParams("Group", false)

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.getGroupList(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored because you can add only once this number")
    @Test
    fun `add group member`(): Unit = runBlocking {
        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )

        val params = AddGroupMemberParams("575802191119150", "9271014488", "Prepaid 2")
        val result = groupManager.addGroupMember(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored until provided test data")
    @Test
    fun `retrieve group usage`(): Unit = runBlocking {
        val params = RetrieveGroupUsageParams("Group", "CMP_SHARENSURF")

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.retrieveGroupUsage(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored until provided test data")
    @Test
    fun `retrieve member usage`(): Unit = runBlocking {
        val params = RetrieveMemberUsageParams(
            isGroupOwner = false,
            memberAccountAlias = "Prepaid 1",
            keyword = "CMP_SHARENSURF",
            memberMobileNumber = "9271014487"
        )

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.retrieveMemberUsage(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored until provided test data")
    @Test
    fun `retrieve group service`(): Unit = runBlocking {
        val params = RetrieveGroupServiceParams(accountAlias = "Prepaid 1")

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.retrieveGroupService(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored because you can delete only once this number")
    @Test
    fun `delete group member`(): Unit = runBlocking {
        val params = DeleteGroupMemberParams(true,"575802191119150", "remove", "9271014488")

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.deleteGroupMember(params)

        assertThat(result.successOrNull()).isNotNull
    }

    @Ignore("Ignored until provided test data")
    @Test
    fun `set member usage limit`(): Unit = runBlocking {
        val params =
            SetMemberUsageLimitParams("9271014488", "9271014487", "CPGS", "9271014487", "2999")

        authManager.getAccessToken()
        authManager.loginEmail(
            LoginEmailParams(
                "igor.stevanovic@lotusflare.com",
                "Test12345!"
            )
        )
        val result = groupManager.setMemberUsageLimit(params)

        assertThat(result.successOrNull()).isNotNull
    }
}
