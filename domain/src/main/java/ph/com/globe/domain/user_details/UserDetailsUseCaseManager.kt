/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.user_details

import ph.com.globe.domain.user_details.di.UserDetailsComponent
import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class UserDetailsUseCaseManager @Inject constructor(
    factory: UserDetailsComponent.Factory
) : UserDetailsDomainManager {

    private val component = factory.create()

    override fun getEmail(): LfResult<String, NetworkError.UserNotLoggedInError> =
        component.provideGetEmailUseCase().get()

    override fun encryptData(data: String): String {
        return component.provideEncryptDataUseCase().execute(data)
    }
}
