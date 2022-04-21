/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session

interface SessionDomainManager {

    fun getCurrentUserSessionId(): Long

    fun startUserSession()

    fun pauseUserSession()
}
