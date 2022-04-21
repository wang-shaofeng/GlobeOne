/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.domain_models

data class RegisteredUser(
    val emailVerificationDate: String? = null,
    val email: String,
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

data class RegisteredUserAddress(
    val province: String? = null,
    val city: String? = null,
    val barangay: String? = null,
    val street: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postal: String? = null
)
