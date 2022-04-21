/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session.usecase

import ph.com.globe.domain.ReposManager
import ph.com.globe.model.session.isExpired
import javax.inject.Inject

class StartUserSessionUseCase @Inject constructor(private val reposManager: ReposManager) {

    fun execute() {
        val currentUserSession = reposManager.getUserSessionRepo().getCurrentUserSession()
        if (currentUserSession.isExpired()) {
            // we create new session if the old one is expired
            reposManager.getUserSessionRepo().createUserSession()
        } else {
            // we unpause the session by removing the pauseTimeInMillis value
            currentUserSession.pauseTimeInMillis = null
            reposManager.getUserSessionRepo().setCurrentUserSession(currentUserSession)
        }
    }
}
