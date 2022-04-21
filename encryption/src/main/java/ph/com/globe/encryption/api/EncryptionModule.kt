/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import dagger.Binds
import dagger.Module
import ph.com.globe.encryption.Crypto
import ph.com.globe.encryption.StringAesCrypto
import ph.com.globe.encryption.StringCrypto
import ph.com.globe.encryption.SuspendableStringCrypto

@Module
internal abstract class EncryptionModule {

    @Binds
    abstract fun bindsGoogleTinkCrypto(tinkCrypto: TinkCrypto): Crypto

    @Binds
    abstract fun bindsStringCrypto(defaultStringCrypto: DefaultStringCrypto): StringCrypto

    @Binds
    abstract fun SuspendableStringCrypto(suspendableStringCrypto: DefaultSuspendableStringCrypto): SuspendableStringCrypto

    @Binds
    abstract fun bindsAesStringCrypto(defaultStringAesCrypto: DefaultStringAesCrypto): StringAesCrypto
}
