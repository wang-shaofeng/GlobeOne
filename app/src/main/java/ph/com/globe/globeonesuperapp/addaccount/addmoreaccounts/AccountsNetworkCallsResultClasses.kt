/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts

import ph.com.globe.model.account.EnrollAccountRequest

sealed class CheckNumberResult {
    object NumberFieldEmpty : CheckNumberResult()

    object SameNumberExists : CheckNumberResult()

    object InvalidNumberFormat : CheckNumberResult()

    data class UniqueNumber(val phoneNumber: String) : CheckNumberResult()
}

sealed class CheckNameResult {
    object NameFieldEmpty : CheckNameResult()

    object SameNameExists : CheckNameResult()

    data class UniqueName(val accountName: String) : CheckNameResult()
}

sealed class AccountsResult {
    object UserHasNoEnrolledAccounts : AccountsResult()

    data class AccountsSuccessResult(
        val accountRequests: List<EnrollAccountRequest>
    ) : AccountsResult()
}
