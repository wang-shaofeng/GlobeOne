/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.maintenance

import ph.com.globe.model.banners.CTAType
import java.io.Serializable

/**
 * @param hasOuterMaintenance has outer maintenance
 * @param componentsMap the map's key see [MaintenanceTabId]
 */
data class MaintenanceModel(
    val outerMaintenance: MaintenanceUIModel,
    val componentsMap: Map<String, MaintenanceUIModel>?
)

data class MaintenanceUIModel(
    val pageTabId: String,
    val hasMaintenance: Boolean,
    val imageUrl: String,
    val title: String,
    val content: String,
    val cta: CtaUIModel?
) : Serializable

data class CtaUIModel(
    val title: String?,
    val ctaLink: String?,
    val ctaType: CTAType?
) : Serializable {
    fun hasGoback() = title.equals(FUNC_GO_BACK, true)
}

private const val FUNC_GO_BACK = "func_go_back"

object MaintenanceTabId {

    // login
    const val LOGIN_OUTER = "login_outer"
    const val LOGIN_LOGIN = "login"
    const val LOGIN_CREAT_ACCOUNT = "create_account"

    // dashboard
    const val DASHBOARD_OUTER = "dashboard_outer"
    const val DASHBOARD_RUSH = "rush"

    // rewards
    const val REWARDS_OUTER = "rewards_outer"
    const val REWARDS_REDEEM_ALL = "redeem_all"
    const val REWARDS_DONATE = "donate"
    const val REWARDS_PYWPTS = "pywpts"
    const val REWARDS_DAC = "dac"
    const val REWARDS_PROMO_REDEEM = "promo_redeem"
    const val REWARDS_RAFFLE_REDEEM = "raffle_redeem"
    const val REWARDS_OTHER_REDEEM = "other_redeem"

    // shop
    const val SHOP_OUTER = "shop_outer"
    const val SHOP_PROMOS = "promos"
    const val SHOP_GP_PROMO = "gp_promo"
    const val SHOP_TM_PROMO = "tm_promo"
    const val SHOP_HPW_PROMO = "hpw_promo"
    const val SHOP_LOANS = "loans"
    const val SHOP_GP_LOAN = "gp_loan"
    const val SHOP_TM_LOAN = "tm_loan"
    const val SHOP_HPW_LOAN = "hpw_loan"
    const val SHOP_LOAD = "load"
    const val SHOP_CONTENT = "content"

    // account
    const val ACCOUNT_OUTER = "account_outer"

    // discover
    const val DISCOVER_OUTER = "discover_outer"
}
