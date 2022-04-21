/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.domain.account_activities.AccountActivitiesDataManager
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.balance.BalanceDataManager
import ph.com.globe.domain.banners.BannersDataManager
import ph.com.globe.domain.billings.BillingsDataManager
import ph.com.globe.domain.catalog.CatalogDataManager
import ph.com.globe.domain.connectivity.ConnectivityDataManager
import ph.com.globe.domain.credit.CreditDataManager
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.domain.maintenance.MaintenanceDataManager
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.domain.prepaid.PrepaidDataManager
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.domain.rush.RushDataManager
import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.domain.voucher.VoucherDataManager

interface DataManagers {

    fun getAuthDataManager(): AuthDataManager

    fun getProfileDataManager(): ProfileDataManager

    fun getBalanceDataManager(): BalanceDataManager

    fun getAccountDataManager(): AccountDataManager

    fun getPaymentDataManager(): PaymentDataManager

    fun getRewardsDataManager(): RewardsDataManager

    fun getCreditDataManager(): CreditDataManager

    fun getShopDataManager(): ShopDataManager

    fun getGroupDataManager(): GroupDataManager

    fun getUserDetailsDataManager(): UserDetailsDataManager

    fun getCatalogDataManager(): CatalogDataManager

    fun getConnectivityDataManager(): ConnectivityDataManager

    fun getRushDataManager(): RushDataManager

    fun getAccountActivitiesDataManager(): AccountActivitiesDataManager

    fun getVoucherDataManager(): VoucherDataManager

    fun getBillingsDataManager(): BillingsDataManager

    fun getPrepaidDataManager(): PrepaidDataManager

    fun getBannersDataManager(): BannersDataManager

    fun getMaintenanceDataManager(): MaintenanceDataManager
}
