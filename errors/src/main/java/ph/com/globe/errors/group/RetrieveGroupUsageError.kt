/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.group

import ph.com.globe.errors.GeneralError

sealed class RetrieveGroupUsageError {

    object MobileNumberNotFound : RetrieveGroupUsageError()

    object SubscriberNotBelongToAnyPool : RetrieveGroupUsageError()

    object GroupNotExist : RetrieveGroupUsageError()

    object WalletNotFound : RetrieveGroupUsageError()

    data class General(val error: GeneralError) : RetrieveGroupUsageError()
}
