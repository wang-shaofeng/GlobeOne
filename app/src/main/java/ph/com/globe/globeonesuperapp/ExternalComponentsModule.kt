/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ph.com.globe.data.DaggerDataComponent
import ph.com.globe.data.DataComponent
import ph.com.globe.domain.DaggerUseCaseComponent
import ph.com.globe.encryption.DaggerEncryptionComponent
import ph.com.globe.encryption.EncryptionComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExternalComponentsModule {

    @Singleton
    @Provides
    fun provideEncryptionComponent(@ApplicationContext context: Context): EncryptionComponent =
        DaggerEncryptionComponent.factory().create(context)

    @Singleton
    @Provides
    fun provideNetworkComponent(
        @ApplicationContext context: Context,
        encryptionComponent: EncryptionComponent
    ): DataComponent =
        DaggerDataComponent.factory().create(context, encryptionComponent)

    @Singleton
    @Provides
    fun provideUseCaseComponent(
        dataComponent: DataComponent
    ) = DaggerUseCaseComponent.factory()
        .create(dataComponent.provideDataManagers(), dataComponent.provideReposManager())
}
