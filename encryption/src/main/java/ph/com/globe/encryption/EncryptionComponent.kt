/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ph.com.globe.encryption.api.EncryptionModule
import javax.inject.Scope

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Scope
annotation class EncryptionScope

@EncryptionScope
@Component(modules = [EncryptionModule::class])
interface EncryptionComponent {

    /**
     * Crypto implementation based off Google Tink library. It uses Android Keystore master key
     * for API levels 23, to guard the local key against it. For APIs lower than 23 it doesn't
     * wrap the key. That is still safe since the key is stored at private storage, so if attacker
     * has a root access he can read anything (including Android Keystore master key).
     *
     *
     * **Note:**
     *
     * Google Tink doesn't guarantee that it will work for versions below API level 19.
     * Good thing is that most of our apps are using min API lev of 21 so this shouldn't matter.
     * More info here https://github.com/google/tink/blob/master/docs/KNOWN-ISSUES.md
     * Note about mentioned updateAAD feature (in the KNOWN-ISSUES.md)... we are not using that, so we will not have problems on API level 19.
     * One more thing to mention is that many people tested Google Tink lib and they figured out that it actually works on lower API levels.
     */
    fun suspendableStringCrypto(): SuspendableStringCrypto

    fun stringCrypto(): StringCrypto

    fun stringAesCrypto(): StringAesCrypto

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): EncryptionComponent
    }
}
