/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.account

import ph.com.globe.errors.GeneralError

sealed class GetAccountStatusError {

    object InvalidAccount : GetAccountStatusError()

    object InactiveAccount : GetAccountStatusError()

    object NoLongerInSystemAccount: GetAccountStatusError()

    data class General(val error: GeneralError) : GetAccountStatusError()
}
