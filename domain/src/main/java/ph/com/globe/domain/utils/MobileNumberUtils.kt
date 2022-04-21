/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.utils

import ph.com.globe.model.shop.formattedForPhilippines

fun String.getMemberRole(ownerMobileNumber: String) =
    if (ownerMobileNumber.formattedForPhilippines() == this.formattedForPhilippines()) GROUP_ROLE_OWNER else GROUP_ROLE_MEMBER

const val GROUP_ROLE_MEMBER = "member"
const val GROUP_ROLE_OWNER = "owner"
