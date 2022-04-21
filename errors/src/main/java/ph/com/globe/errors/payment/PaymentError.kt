/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.payment

import ph.com.globe.errors.GeneralError

sealed class PaymentError {

    object PollingFailed : PaymentError()

    object PaymentFailed : PaymentError()

    data class General(val error: GeneralError) : PaymentError()
}
