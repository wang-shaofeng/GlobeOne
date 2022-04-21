/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.net.Uri

sealed class DeepLinkAction {
    object ResetPassword : DeepLinkAction()
    object RewardCategoryRaffleTab : DeepLinkAction()
    object RewardCategoryAllTab : DeepLinkAction()
    object ShopTab : DeepLinkAction()
    object RewardLanding : DeepLinkAction()
    object OpenSpinwheel : DeepLinkAction()
    object ShopLoad : DeepLinkAction()
    data class EmailVerification(val verificationCode: String) : DeepLinkAction()
}

data class DeepLinkObject(val deepLinkAction: DeepLinkAction)

class DeepLinkHandler(
    private val handleDeepLink: (DeepLinkObject) -> Unit,
) {
    fun setDeepLink(uri: Uri) {
        val deeplink = uri.convertToDeepLinkObject()
        deeplink?.let { handleDeepLink(deeplink) }
    }

    private fun Uri.convertToDeepLinkObject(): DeepLinkObject? {

        val action = when (getQueryParameter("action") ?: "") {
            "email_verification" ->
                getQueryParameter("verification_code")?.let { DeepLinkAction.EmailVerification(it) }
            "reward_category_raffle_tab" -> DeepLinkAction.RewardCategoryRaffleTab
            "reward_category_all_tab" -> DeepLinkAction.RewardCategoryAllTab
            "shop_tab" -> DeepLinkAction.ShopTab
            "reward_landing" -> DeepLinkAction.RewardLanding
            "open_spinwheel" -> DeepLinkAction.OpenSpinwheel
            "reset_password" -> DeepLinkAction.ResetPassword
            "shop_load" -> DeepLinkAction.ShopLoad
            else -> null
        }

        return action?.let { DeepLinkObject(it) }
    }
}
