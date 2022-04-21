/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.test

import dagger.Component
import ph.com.globe.encryption.EncryptionComponent

@Component(modules = [TestModule::class])
interface TestEncryptionComponent : EncryptionComponent
