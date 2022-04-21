/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.account

import ph.com.globe.errors.GeneralError

sealed class GetAccountBrandError {

    object InvalidAccount : GetAccountBrandError()

    object InvalidParameter : GetAccountBrandError()

    data class General(val error: GeneralError) : GetAccountBrandError()
}
