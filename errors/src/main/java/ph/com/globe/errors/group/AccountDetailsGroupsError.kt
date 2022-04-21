/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.group

import ph.com.globe.errors.GeneralError

sealed class AccountDetailsGroupsError {

    object MobileNumberNotFound : AccountDetailsGroupsError()

    object SubscriberNotBelongToAnyPool : AccountDetailsGroupsError()

    object GroupNotExist : AccountDetailsGroupsError()

    object WalletNotFound : AccountDetailsGroupsError()

    data class General(val error: GeneralError) : AccountDetailsGroupsError()
}
