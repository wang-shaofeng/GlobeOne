/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import ph.com.globe.encryption.api.AeadManager

class AeadManagerTest {

    private lateinit var aeadManager: AeadManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        aeadManager = AeadManager(context)
    }

    @Test
    fun setUpState_DoesntHaveKeys_hasTryUseMasterKeySettingSetToTrue() {
        assertThat(aeadManager._crypto).isNull()
        assertThat(aeadManager._aeadFactory).isNotNull
        with(aeadManager._sharedPrefsKeyStore) {
            assertThat(contains(EXPECTED_CRYPTO_KEY)).isFalse

            assertThat(contains(EXPECTED_TRY_MASTER_KEY)).isTrue
            assertThat(getBoolean(EXPECTED_TRY_MASTER_KEY, false)).isTrue
        }
    }

    @Test
    fun getOrGenerate_withDefaultTryMasterKeySetting_createsNewAead_andCachesIt() {
        testHelperFor_getOrGenerate(EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL)
    }

    @Test
    fun getOrGenerate_withInvertedDefaultTryMasterKeySetting_createsNewAeadWithNullParam_andCachesIt() {
        testHelperFor_getOrGenerate(EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL.not())
    }

    private fun testHelperFor_getOrGenerate(tryMasterKeySetting: Boolean) {
        val expectedCreateAeadParam = EXPECTED_MASTER_KEY_PATH.takeIf { tryMasterKeySetting }
        aeadManager.spyFactoryAndMockPreferences()
        with(aeadManager._sharedPrefsKeyStore) {
            `when`(contains(EXPECTED_TRY_MASTER_KEY)).thenReturn(true)
            `when`(getBoolean(EXPECTED_TRY_MASTER_KEY, EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL))
                .thenReturn(tryMasterKeySetting)
        }

        val first = aeadManager.getOrGenerateAead()
        val second = aeadManager.getOrGenerateAead()

        with(aeadManager) {
            assertThat(first).isSameAs(_crypto).isSameAs(second)
            verify(_aeadFactory).createAead(expectedCreateAeadParam)
            verify(_sharedPrefsKeyStore).contains(EXPECTED_TRY_MASTER_KEY)
            verify(_sharedPrefsKeyStore)
                .getBoolean(EXPECTED_TRY_MASTER_KEY, EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL)
            verifyNoMoreInteractions(_aeadFactory, _sharedPrefsKeyStore)
        }
    }

    @Test
    fun clearAead_and_doNotTryToUseMasterKey_deetesAeadAndSetsFlagNotToUseMasterKey() {
        testHelperFor_clearAeadAndStopTryingToUseMasterKey(true)
    }

    @Test
    fun clearAead_and_doNotTryToUseMasterKey_ifCommitFailsThrowException() {
        testHelperFor_clearAeadAndStopTryingToUseMasterKey(false)
    }

    private fun testHelperFor_clearAeadAndStopTryingToUseMasterKey(isCommitSuccessful: Boolean) {
        val mockedEditor = mock(SharedPreferences.Editor::class.java)
        with(aeadManager) {
            spyFactoryAndMockPreferences()
            _crypto = mock(Aead::class.java)
            `when`(_sharedPrefsKeyStore.edit()).thenReturn(mockedEditor)
            `when`(mockedEditor.putBoolean(EXPECTED_TRY_MASTER_KEY, false)).thenReturn(mockedEditor)
            `when`(mockedEditor.remove(EXPECTED_CRYPTO_KEY)).thenReturn(mockedEditor)
            `when`(mockedEditor.commit()).thenReturn(isCommitSuccessful)
        }

        if (isCommitSuccessful) {
            aeadManager.clearAeadAndStopTryingToUseMasterKey()
        } else {
            assertThatThrownBy {
                aeadManager.clearAeadAndStopTryingToUseMasterKey()
            }.isInstanceOf(IllegalStateException::class.java)
        }

        assertThat(aeadManager._crypto).isNull()
        with(aeadManager) {
            verify(_sharedPrefsKeyStore).edit()
            verify(mockedEditor).putBoolean(EXPECTED_TRY_MASTER_KEY, false)
            verify(mockedEditor).remove(EXPECTED_CRYPTO_KEY)
            verify(mockedEditor).commit()
            verifyNoMoreInteractions(_sharedPrefsKeyStore, _aeadFactory, mockedEditor)
        }
    }

    @Test
    fun getOrGenerate_thatFails_willRetryAndReturnOnSuccess_andWilCacheResultAsUsual() {
        with(aeadManager) {
            spyFactoryAndMockPreferences()
            `when`(_aeadFactory.createAead(EXPECTED_MASTER_KEY_PATH))
                .thenThrow(IllegalStateException::class.java)
                .thenThrow(IllegalArgumentException::class.java)
                .thenCallRealMethod()
            with(_sharedPrefsKeyStore) {
                `when`(contains(EXPECTED_TRY_MASTER_KEY)).thenReturn(true)
                `when`(getBoolean(EXPECTED_TRY_MASTER_KEY, EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL))
                    .thenReturn(EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL)
            }
        }

        val first = aeadManager.getOrGenerateAead()
        val second = aeadManager.getOrGenerateAead()

        with(aeadManager) {
            assertThat(first).isSameAs(_crypto).isSameAs(second)
            verify(_aeadFactory, times(3)).createAead(EXPECTED_MASTER_KEY_PATH)
            verify(_sharedPrefsKeyStore).contains(EXPECTED_TRY_MASTER_KEY)
            verify(_sharedPrefsKeyStore)
                .getBoolean(EXPECTED_TRY_MASTER_KEY, EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL)
            verifyNoMoreInteractions(_aeadFactory, _sharedPrefsKeyStore)
        }
    }

    @Test
    fun getOrGenerate_thatFailsAllTheTime_willEventuallyThrowExceptionWithLastExceptionAsCause_willRetryAtLeast3Times() {
        val lastExceptionThrown = NullPointerException("AndroidKeystore fails sometimes :(.")
        with(aeadManager) {
            spyFactoryAndMockPreferences()
            `when`(_aeadFactory.createAead(EXPECTED_MASTER_KEY_PATH))
                .thenThrow(IllegalStateException::class.java)
                .thenThrow(IllegalArgumentException::class.java)
                .thenThrow(lastExceptionThrown)
            with(_sharedPrefsKeyStore) {
                `when`(contains(EXPECTED_TRY_MASTER_KEY)).thenReturn(true)
                `when`(getBoolean(EXPECTED_TRY_MASTER_KEY, EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL))
                    .thenReturn(EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL)
            }
        }

        assertThatThrownBy { aeadManager.getOrGenerateAead() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasCause(lastExceptionThrown)

        with(aeadManager) {
            assertThat(_crypto).isNull()
            // when counting first call + at least 3 retries == 4 :).
            verify(_aeadFactory, atLeast(4)).createAead(EXPECTED_MASTER_KEY_PATH)
            verify(_sharedPrefsKeyStore).contains(EXPECTED_TRY_MASTER_KEY)
            verify(_sharedPrefsKeyStore)
                .getBoolean(EXPECTED_TRY_MASTER_KEY, EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL)
            verifyNoMoreInteractions(_aeadFactory, _sharedPrefsKeyStore)
        }
    }

    private fun AeadManager.spyFactoryAndMockPreferences() {
        _aeadFactory = spy(_aeadFactory)
        _sharedPrefsKeyStore = mock(SharedPreferences::class.java)
    }

    @After
    fun checkState() {
        //We are using var field so we can spy/mock them, but these fields shouldn't ever be null so we check here.
        assertThat(aeadManager._aeadFactory).isNotNull
        assertThat(aeadManager._sharedPrefsKeyStore).isNotNull
    }

    private companion object {
        const val EXPECTED_CRYPTO_KEY = "crypto_key"
        const val EXPECTED_TRY_MASTER_KEY = "try_to_use_master_key"
        const val EXPECTED_TRY_MASTER_KEY_DEFAULT_VAL = true

        const val EXPECTED_MASTER_KEY_PATH = "android-keystore://android_lib_master_key"
    }
}
