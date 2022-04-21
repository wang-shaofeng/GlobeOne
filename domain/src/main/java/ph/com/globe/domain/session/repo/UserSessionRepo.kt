/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session.repo

import ph.com.globe.model.session.UserSession

interface UserSessionRepo {
    fun getCurrentUserSession(): UserSession
    fun setCurrentUserSession(userSession: UserSession)
    fun createUserSession(): UserSession
}
