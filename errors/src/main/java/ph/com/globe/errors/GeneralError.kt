/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors

sealed class GeneralError {

    object NotLoggedIn: GeneralError()

    object General: GeneralError()

    data class Other(val error: NetworkError): GeneralError()
}
