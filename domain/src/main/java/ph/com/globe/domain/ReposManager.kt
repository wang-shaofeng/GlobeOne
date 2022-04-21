/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain

import ph.com.globe.domain.account.db.AccountGroupsRepo
import ph.com.globe.domain.account.db.AccountSubscriptionUsagesRepo
import ph.com.globe.domain.payment.repo.PaymentParametersRepo
import ph.com.globe.domain.profile.db.EnrolledAccountsRepo
import ph.com.globe.domain.profile.db.RegisteredUserRepo
import ph.com.globe.domain.session.repo.UserSessionRepo
import ph.com.globe.domain.shop.db.ShopItemsRepo

interface ReposManager {

    fun getEnrolledAccountsRepo(): EnrolledAccountsRepo

    fun getUserSessionRepo(): UserSessionRepo

    fun getPaymentParametersRepo(): PaymentParametersRepo

    fun getRegisteredUserRepo(): RegisteredUserRepo

    fun getAccountGroupsRepo(): AccountGroupsRepo

    fun getShopItemsRepo(): ShopItemsRepo

    fun getSubscriptionUsagesRepo(): AccountSubscriptionUsagesRepo
}
