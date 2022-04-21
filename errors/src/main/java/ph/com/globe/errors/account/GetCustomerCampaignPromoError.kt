/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.errors.account

import ph.com.globe.errors.GeneralError

sealed class GetCustomerCampaignPromoError {
    data class General(val error: GeneralError) : GetCustomerCampaignPromoError()

    // Mobile number not found in offers.
    object ResourceNotFound : GetCustomerCampaignPromoError()
}
