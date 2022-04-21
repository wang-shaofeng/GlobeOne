/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session.usecase

import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class PauseUserSessionUseCase @Inject constructor(private val reposManager: ReposManager) {

    fun execute() {
        val currentUserSession = reposManager.getUserSessionRepo().getCurrentUserSession()
        currentUserSession.pauseTimeInMillis = System.currentTimeMillis()
        reposManager.getUserSessionRepo().setCurrentUserSession(currentUserSession)
    }
}
