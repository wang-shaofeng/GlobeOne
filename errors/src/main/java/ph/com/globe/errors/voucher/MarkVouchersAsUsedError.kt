/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.voucher

import ph.com.globe.errors.GeneralError

sealed class MarkVouchersAsUsedError {

    data class General(val error: GeneralError) : MarkVouchersAsUsedError()
}
