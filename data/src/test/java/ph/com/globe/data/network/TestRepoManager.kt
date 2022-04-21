/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import ph.com.globe.data.shared_preferences.payment.InMemoryPaymentParametersRepository
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.account.db.AccountGroupsRepo
import ph.com.globe.domain.account.db.AccountSubscriptionUsagesRepo
import ph.com.globe.domain.payment.repo.PaymentParametersRepo
import ph.com.globe.domain.profile.db.EnrolledAccountsRepo
import ph.com.globe.domain.profile.db.RegisteredUserRepo
import ph.com.globe.domain.session.repo.UserSessionRepo
import ph.com.globe.domain.shop.db.ShopItemsRepo
import ph.com.globe.errors.account.GetPrepaidPromoSubscriptionUsageError
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.account.domain_models.PromoSubscriptionUsage
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.model.session.UserSession
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.util.LfResult
import javax.inject.Inject

class TestRepoManager @Inject constructor() : ReposManager {

    @Inject
    lateinit var enrolledAccountsRepo: TestEnrolledAccountsRepo

    @Inject
    lateinit var userSessionRepo: UserSessionInMemoryRepo

    @Inject
    lateinit var registeredUserRepo: TestRegisteredUserRepo

    @Inject
    lateinit var accountGroupsRepo: TestAccountGroupsRepo

    @Inject
    lateinit var shopItemsRepo: TestShopItemsRepo

    @Inject
    lateinit var subscriptionUsageRepo: TestSubscriptionUsageRepo

    @Inject
    lateinit var paymentParametersRepo: InMemoryPaymentParametersRepository

    override fun getEnrolledAccountsRepo(): EnrolledAccountsRepo = enrolledAccountsRepo

    override fun getUserSessionRepo(): UserSessionRepo = userSessionRepo

    override fun getPaymentParametersRepo(): PaymentParametersRepo = paymentParametersRepo

    override fun getRegisteredUserRepo(): RegisteredUserRepo = registeredUserRepo

    override fun getAccountGroupsRepo(): AccountGroupsRepo = accountGroupsRepo

    override fun getShopItemsRepo(): ShopItemsRepo = shopItemsRepo

    override fun getSubscriptionUsagesRepo(): AccountSubscriptionUsagesRepo = subscriptionUsageRepo
}

class TestEnrolledAccountsRepo @Inject constructor() : EnrolledAccountsRepo {
    override suspend fun getAllEnrolledAccounts(): Flow<LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>> =
        flow {
            emit(LfResult.success(emptyList()))
        }

    override suspend fun refreshEnrolledAccounts() = Unit

    override suspend fun invalidateEnrolledAccounts() = Unit

    override suspend fun deleteEnrolledAccounts() = Unit

    override suspend fun deleteEnrolledAccount(primaryMsisdn: String) = Unit

    override suspend fun deleteMetadata() = Unit
}

class TestRegisteredUserRepo @Inject constructor() : RegisteredUserRepo {
    override suspend fun fetchRegisteredUser(): LfResult<Unit, GetRegisteredUserError> =
        LfResult.success(Unit)

    override suspend fun checkFreshnessAndUpdate(): LfResult<Unit, GetRegisteredUserError> =
        LfResult.success(Unit)

    override suspend fun getRegisteredUser(): Flow<RegisteredUser?> = flowOf()

    override suspend fun getUserNickname(): Flow<String?> = flowOf()

    override suspend fun getFirstName(): Flow<String?> = flowOf()

    override suspend fun refreshRegisteredUser() = Unit

    override suspend fun invalidateRegisteredUser() = Unit

    override suspend fun deleteRegisteredUser() = Unit

    override suspend fun deleteMetadata() = Unit
}

class TestAccountGroupsRepo @Inject constructor() : AccountGroupsRepo {
    override suspend fun fetchAccountGroups(params: AccountDetailsGroupsParams): LfResult<Unit, AccountDetailsGroupsError> =
        LfResult.success(Unit)

    override suspend fun checkFreshnessAndUpdate(params: AccountDetailsGroupsParams): LfResult<Unit, AccountDetailsGroupsError> =
        LfResult.success(Unit)

    override suspend fun getAccountGroups(primaryMsisdn: String): Flow<AccountDetailsGroups?> =
        emptyFlow()

    override suspend fun refreshAccountGroups(primaryMsisdn: String) = Unit

    override suspend fun invalidateAccountGroups(primaryMsisdn: String) = Unit

    override suspend fun deleteAllAccountsGroups() = Unit

    override suspend fun deleteMetadata() = Unit
}

class TestShopItemsRepo @Inject constructor() : ShopItemsRepo {
    override suspend fun fetchShopItems(): LfResult<Unit, GetAllOffersError> =
        LfResult.success(Unit)

    override suspend fun checkFreshnessAndUpdate(forceRefresh: Boolean): LfResult<Unit, GetAllOffersError> =
        LfResult.success(Unit)

    override fun getAllOffers(visibleOnMainCatalog: Boolean): Flow<List<ShopItem>> =
        flowOf(emptyList())

    override fun getPromos(): Flow<List<ShopItem>> = flowOf(emptyList())

    override fun getLoanable(): Flow<List<ShopItem>> = flowOf(emptyList())

    override fun getContentPromos(): Flow<List<ShopItem>> = flowOf(emptyList())

    override suspend fun refreshShopItems() = Unit

    override suspend fun invalidateShopItems() = Unit

    override suspend fun deleteShopItems() = Unit

    override suspend fun deleteMetadata() = Unit
}

class TestSubscriptionUsageRepo @Inject constructor() : AccountSubscriptionUsagesRepo {
    override suspend fun fetchAccountSubscriptionUsages(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<Unit, GetPrepaidPromoSubscriptionUsageError> =
        LfResult.success(Unit)

    override suspend fun checkFreshnessAndUpdate(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<Unit, GetPrepaidPromoSubscriptionUsageError> =
        LfResult.success(Unit)

    override suspend fun getAccountSubscriptionUsages(msisdn: String): Flow<PromoSubscriptionUsage?> =
        flowOf()

    override suspend fun refreshAccountSubscriptionUsages(msisdn: String) = Unit

    override suspend fun invalidateAccountSubscriptionUsages(msisdn: String) = Unit

    override suspend fun deleteAllAccountsSubscriptionUsages() = Unit

    override suspend fun deleteMetadata() = Unit
}

class UserSessionInMemoryRepo @Inject constructor() : UserSessionRepo {
    var currentUserSessionValue: UserSession = UserSession(0, System.currentTimeMillis())
    override fun getCurrentUserSession(): UserSession = currentUserSessionValue
    override fun setCurrentUserSession(userSession: UserSession) {
        currentUserSessionValue = userSession
    }

    override fun createUserSession(): UserSession =
        UserSession(
            currentUserSessionValue.sessionId + 1,
            System.currentTimeMillis()
        ).also { currentUserSessionValue = it }
}
