/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.session

import ph.com.globe.domain.session.di.SessionComponent
import javax.inject.Inject

class SessionUseCaseManager @Inject constructor(
    factory: SessionComponent.Factory
) : SessionDomainManager {

    private val sessionComponent: SessionComponent = factory.create()

    override fun getCurrentUserSessionId(): Long =
        sessionComponent.provideGetCurrentSessionIdUseCase().execute()

    override fun startUserSession() = sessionComponent.provideStartSessionUseCase().execute()

    override fun pauseUserSession() = sessionComponent.providePauseSessionUseCase().execute()

}
