/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.group

import ph.com.globe.errors.GeneralError

sealed class SetMemberUsageLimitError {

    object GroupMemberNotExist : SetMemberUsageLimitError()

    object GroupNotExist : SetMemberUsageLimitError()

    object WalletNotFound : SetMemberUsageLimitError()

    object SubscriberNotFound : SetMemberUsageLimitError()

    object ExceededTotalUsageLimit : SetMemberUsageLimitError()

    data class General(val error: GeneralError) : SetMemberUsageLimitError()
}
