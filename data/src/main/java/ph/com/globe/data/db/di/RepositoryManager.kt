/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.di

import ph.com.globe.data.db.enrolled_accounts.EnrolledAccountRepository
import ph.com.globe.data.db.groups.AccountGroupsRepository
import ph.com.globe.data.db.prepaid_promo_subscription_usage.AccountSubscriptionUsageRepository
import ph.com.globe.data.db.profile_info.RegisteredUserRepository
import ph.com.globe.data.db.shop.ShopItemsRepository
import ph.com.globe.data.shared_preferences.payment.DefaultPaymentParametersRepository
import ph.com.globe.data.shared_preferences.session.DefaultUserSessionRepo
import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class RepositoryManager @Inject constructor(
    private val enrolledAccountsRepo: EnrolledAccountRepository,
    private val userSessionRepo: DefaultUserSessionRepo,
    private val paymentParametersRepo: DefaultPaymentParametersRepository,
    private val registeredUserRepo: RegisteredUserRepository,
    private val accountGroupsRepo: AccountGroupsRepository,
    private val shopItemsRepo: ShopItemsRepository,
    private val subscriptionUsagesRepo: AccountSubscriptionUsageRepository
) : ReposManager {

    override fun getEnrolledAccountsRepo() = enrolledAccountsRepo

    override fun getUserSessionRepo() = userSessionRepo

    override fun getPaymentParametersRepo() = paymentParametersRepo

    override fun getRegisteredUserRepo() = registeredUserRepo

    override fun getAccountGroupsRepo() = accountGroupsRepo

    override fun getShopItemsRepo() = shopItemsRepo

    override fun getSubscriptionUsagesRepo() = subscriptionUsagesRepo
}
