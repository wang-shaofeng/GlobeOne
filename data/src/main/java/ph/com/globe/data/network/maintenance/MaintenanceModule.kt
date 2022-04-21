/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.CMS_SERVER
import ph.com.globe.data.network.maintenance.calls.GetMaintenanceNetworkCall
import ph.com.globe.data.network.maintenance.repositories.MaintenanceRepository
import ph.com.globe.data.network.maintenance.repositories.MaintenanceRepositoryModule
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [MaintenanceComponent::class])
internal interface MaintenanceModule

@ManagerScope
@Subcomponent(modules = [MaintenanceProvidesModule::class, MaintenanceRepositoryModule::class])
interface MaintenanceComponent {

    fun provideGetMaintenanceNetworkCall(): GetMaintenanceNetworkCall

    fun provideMaintenanceRepository(): MaintenanceRepository

    @Subcomponent.Factory
    interface Factory {
        fun create(): MaintenanceComponent
    }
}

@Module
internal object MaintenanceProvidesModule {

    @Provides
    @ManagerScope
    fun providesMaintenanceRetrofit(@Named(CMS_SERVER) retrofit: Retrofit): MaintenanceRetrofit =
        retrofit.create(MaintenanceRetrofit::class.java)
}
