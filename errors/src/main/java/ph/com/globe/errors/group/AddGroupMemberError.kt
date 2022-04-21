/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.errors.group

import ph.com.globe.errors.GeneralError

sealed class AddGroupMemberError {

    object SubscriberBrandNotFound : AddGroupMemberError()

    object PoolNotExist : AddGroupMemberError()

    object PoolNotActive : AddGroupMemberError()

    object SubscriberAlreadyMember : AddGroupMemberError()

    object OwnerCantBeAdded : AddGroupMemberError()

    object MemberLimitReached : AddGroupMemberError()

    data class General(val error: GeneralError) : AddGroupMemberError()
}
