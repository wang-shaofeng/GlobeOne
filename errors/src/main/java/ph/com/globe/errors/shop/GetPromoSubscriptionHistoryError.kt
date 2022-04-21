/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.shop

import ph.com.globe.errors.GeneralError

sealed class GetPromoSubscriptionHistoryError {

    data class General(val error: GeneralError) : GetPromoSubscriptionHistoryError()

    object NoSubscriptionsFound : GetPromoSubscriptionHistoryError()
}
