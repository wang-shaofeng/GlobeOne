/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.payment

import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.balance.CheckBalanceSufficiencyError

sealed class PurchaseError {
    object InvalidParameters : PurchaseError()

    data class ShareLoadPromoOtpSendingError(val error: CreateServiceOrderError) : PurchaseError()

    data class General(val error: GeneralError) : PurchaseError()
}

sealed class CreateServiceOrderError {
    object InvalidParameters : CreateServiceOrderError()

    object InsufficientFunds : CreateServiceOrderError()

    data class General(val error: GeneralError) : CreateServiceOrderError()
}
