/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network

import ph.com.globe.domain.DataManagers
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
import javax.inject.Inject

class NetworkManagers @Inject constructor(
    private val authDataManager: AuthDataManager,
    private val profileDataManager: ProfileDataManager,
    private val balanceDataManager: BalanceDataManager,
    private val accountDataManager: AccountDataManager,
    private val paymentDataManager: PaymentDataManager,
    private val rewardsDataManager: RewardsDataManager,
    private val creditDataManager: CreditDataManager,
    private val shopDataManager: ShopDataManager,
    private val groupDataManager: GroupDataManager,
    private val userDetailsDataManager: UserDetailsDataManager,
    private val catalogDataManager: CatalogDataManager,
    private val connectivityDataManager: ConnectivityDataManager,
    private val rushDataManager: RushDataManager,
    private val accountActivitiesDataManager: AccountActivitiesDataManager,
    private val voucherDataManager: VoucherDataManager,
    private val billingsDataManager: BillingsDataManager,
    private val prepaidDataManager: PrepaidDataManager,
    private val bannersDataManager: BannersDataManager,
    private val maintenanceDataManager: MaintenanceDataManager
) : DataManagers {

    override fun getAuthDataManager(): AuthDataManager = authDataManager

    override fun getProfileDataManager(): ProfileDataManager = profileDataManager

    override fun getBalanceDataManager(): BalanceDataManager = balanceDataManager

    override fun getAccountDataManager(): AccountDataManager = accountDataManager

    override fun getPaymentDataManager(): PaymentDataManager = paymentDataManager

    override fun getRewardsDataManager(): RewardsDataManager = rewardsDataManager

    override fun getCreditDataManager(): CreditDataManager = creditDataManager

    override fun getShopDataManager(): ShopDataManager = shopDataManager

    override fun getGroupDataManager(): GroupDataManager = groupDataManager

    override fun getUserDetailsDataManager(): UserDetailsDataManager = userDetailsDataManager

    override fun getCatalogDataManager(): CatalogDataManager = catalogDataManager

    override fun getConnectivityDataManager(): ConnectivityDataManager = connectivityDataManager

    override fun getRushDataManager(): RushDataManager = rushDataManager

    override fun getAccountActivitiesDataManager(): AccountActivitiesDataManager =
        accountActivitiesDataManager

    override fun getVoucherDataManager(): VoucherDataManager = voucherDataManager

    override fun getBillingsDataManager(): BillingsDataManager = billingsDataManager

    override fun getPrepaidDataManager(): PrepaidDataManager = prepaidDataManager

    override fun getBannersDataManager(): BannersDataManager = bannersDataManager

    override fun getMaintenanceDataManager(): MaintenanceDataManager = maintenanceDataManager
}
