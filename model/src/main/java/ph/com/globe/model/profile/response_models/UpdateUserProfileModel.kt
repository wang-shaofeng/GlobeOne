/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.response_models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserProfileRequestParams (
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val suffix: String? = null,
    val birthdate: String? = null,
    val salutation: String? = null,
    val contactNumber: String? = null,
    val address: RegisteredUserAddress? = null
)


