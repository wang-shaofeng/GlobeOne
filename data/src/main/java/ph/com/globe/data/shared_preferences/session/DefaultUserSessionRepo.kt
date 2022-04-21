/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.session

import android.content.SharedPreferences
import com.google.gson.Gson
import ph.com.globe.data.shared_preferences.session.DefaultUserSessionRepo.Companion.CURRENT_USER_SESSION_KEY
import ph.com.globe.data.shared_preferences.session.di.SHARED_PREFS_SESSION_KEY
import ph.com.globe.domain.session.repo.UserSessionRepo
import ph.com.globe.model.session.UserSession
import javax.inject.Inject
import javax.inject.Named

class DefaultUserSessionRepo @Inject constructor(@Named(SHARED_PREFS_SESSION_KEY) private val sharedPreferences: SharedPreferences) :
    UserSessionRepo {

    override fun getCurrentUserSession(): UserSession =
        when (val currentSession = sharedPreferences.getCurrentUserSessionIfExists()) {
            null -> createUserSession()
            else -> currentSession
        }

    override fun setCurrentUserSession(userSession: UserSession) {
        sharedPreferences.setCurrentUserSession(userSession)
    }

    override fun createUserSession(): UserSession {
        return UserSession(getCurrentSessionId() + 1L, System.currentTimeMillis()).also {
            sharedPreferences.edit().putString(CURRENT_USER_SESSION_KEY, Gson().toJson(it)).apply()
        }
    }

    private fun getCurrentSessionId(): Long =
        when (val currentSession = sharedPreferences.getCurrentUserSessionIfExists()) {
            null -> 0L
            else -> currentSession.sessionId
        }

    internal companion object {
        const val CURRENT_USER_SESSION_KEY = "CurrentUserSession_key"
    }
}

fun SharedPreferences.getCurrentUserSessionIfExists(): UserSession? =
    if (contains(CURRENT_USER_SESSION_KEY))
        Gson().fromJson(
            getString(CURRENT_USER_SESSION_KEY, ""),
            UserSession::class.java
        ) else null

fun SharedPreferences.setCurrentUserSession(userSession: UserSession) {
    edit().putString(CURRENT_USER_SESSION_KEY, Gson().toJson(userSession)).apply()
}
