/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session.usecase

import ph.com.globe.domain.ReposManager
import javax.inject.Inject

class GetCurrentUserSessionIdUseCase @Inject constructor(private val reposManager: ReposManager) {

    fun execute(): Long = reposManager.getUserSessionRepo().getCurrentUserSession().sessionId

}
