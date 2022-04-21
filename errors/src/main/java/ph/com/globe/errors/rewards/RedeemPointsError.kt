/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.rewards

import ph.com.globe.errors.GeneralError

sealed class RedeemPointsError {
    data class General(val error: GeneralError) : RedeemPointsError()
    object InsufficientBalancePoints : RedeemPointsError()
    object SubscriberAccountIsBlacklisted : RedeemPointsError()
}
