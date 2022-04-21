package ph.com.globe.errors.account

import ph.com.globe.errors.GeneralError

sealed class GetAccountAccessTypeError {
    data class General(val error: GeneralError) : GetAccountAccessTypeError()
}
