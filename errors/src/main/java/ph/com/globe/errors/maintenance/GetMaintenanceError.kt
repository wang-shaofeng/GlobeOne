/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.maintenance

import ph.com.globe.errors.GeneralError

sealed class GetMaintenanceError {

    data class General(val error: GeneralError) : GetMaintenanceError()
}
