package ph.com.globe.errors.account

import ph.com.globe.errors.GeneralError

sealed class GetUsageConsumptionReportsError {
    data class General(val error: GeneralError) : GetUsageConsumptionReportsError()
}
