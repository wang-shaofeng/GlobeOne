/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.credit

import ph.com.globe.errors.GeneralError

sealed class LoanPromoError {

    data class General(val error: GeneralError) : LoanPromoError()
}
