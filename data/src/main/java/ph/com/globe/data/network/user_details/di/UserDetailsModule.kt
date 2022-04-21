/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.user_details.di

import dagger.Binds
import dagger.Module
import ph.com.globe.data.DataScope
import ph.com.globe.data.network.user_details.DefaultUserDetailsRepository
import ph.com.globe.data.network.user_details.UserDetailsRepository

@Module
internal interface UserDetailsModule {

    @Binds
    @DataScope
    fun bindUserDetailsRepository(UserDetailsRepository: DefaultUserDetailsRepository): UserDetailsRepository
}
