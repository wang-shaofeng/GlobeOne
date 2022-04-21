/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.payment

import ph.com.globe.errors.GeneralError

sealed class GetGcashBalanceError {

    object NoGCashAccount : GetGcashBalanceError()

    object GCashNotLinked : GetGcashBalanceError()

    object SuspendedOrInactive : GetGcashBalanceError()

    data class General(val error: GeneralError) : GetGcashBalanceError()
}
