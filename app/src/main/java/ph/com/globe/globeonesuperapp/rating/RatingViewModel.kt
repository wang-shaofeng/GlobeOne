/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rating

import android.content.SharedPreferences
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.analytics.events.NO_EMAIL_STORED
import ph.com.globe.analytics.events.custom.AppRatingAnalytics
import ph.com.globe.domain.session.SessionDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.build_version.BuildVersionProvider
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.shared_preferences.*
import ph.com.globe.model.app_update.toVersionCode
import ph.com.globe.model.smart_rating.ImprovementOption
import ph.com.globe.util.fold
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val remoteConfigManager: RemoteConfigManager,
    private val buildVersionProvider: BuildVersionProvider,
    private val sessionDomainManager: SessionDomainManager,
    private val userDetailsDomainManager: UserDetailsDomainManager
) : ViewModel() {

    var improvementOptionsList = listOf<ImprovementOptionUI>()
    private var _improvementOptionsFetched = MutableLiveData<Boolean>()
    var improvementOptionsFetched: LiveData<Boolean> = _improvementOptionsFetched

    private var _ratingRulesFulfilled = MutableLiveData<OneTimeEvent<Boolean>>()
    var ratingRulesFulfilled: LiveData<OneTimeEvent<Boolean>> = _ratingRulesFulfilled

    private var lastRating = sharedPreferences.getInt(RATING_LAST_RATING, 0)

    private var lastRatingTime = sharedPreferences.getLong(RATING_LAST_RATING_TIME, 0)

    private var appOpenTime = sharedPreferences.getLong(RATING_OPEN_TIME, 0)

    private var appInstallTime = sharedPreferences.getLong(RATING_INSTALL_TIME, 0)

    private var lastRatedVersion = sharedPreferences.getInt(RATING_LAST_RATED_VERSION, 0)

    private var lastRatedInSessionId = sharedPreferences.getLong(RATING_LAST_RATED_IN_SESSION, 0)

    private var enrolledAccountCount = sharedPreferences.getInt(RATING_ENROLLED_ACCOUNTS_COUNT, 0)

    private var ratingOmitted = sharedPreferences.getBoolean(RATING_OMITTED, false)

    private var appVersion = buildVersionProvider.provideBuildVersionCode()

    private var currentTime = System.currentTimeMillis()

    private var currentSessionId: Long = sessionDomainManager.getCurrentUserSessionId()

    var currentRating = 0

    private var userEmail = ""

    init {
        viewModelScope.launch {
            remoteConfigManager.getSmartRatingConfig()?.let { smartRatingConfig ->
                improvementOptionsList =
                    smartRatingConfig.options.en.map { it.toImprovementOptionUI() }
                _improvementOptionsFetched.value = true
            }
        }

        userDetailsDomainManager.getEmail().fold({ email ->
            userEmail = email
        }, {
            userEmail = NO_EMAIL_STORED
        })
    }

    private fun getLastRatingParams() {
        lastRating = sharedPreferences.getInt(RATING_LAST_RATING, 0)
        lastRatingTime = sharedPreferences.getLong(RATING_LAST_RATING_TIME, 0)
        appOpenTime = sharedPreferences.getLong(RATING_OPEN_TIME, 0)
        appInstallTime = sharedPreferences.getLong(RATING_INSTALL_TIME, 0)
        lastRatedVersion = sharedPreferences.getInt(RATING_LAST_RATED_VERSION, 0)
        lastRatedInSessionId = sharedPreferences.getLong(RATING_LAST_RATED_IN_SESSION, 0)
        enrolledAccountCount = sharedPreferences.getInt(RATING_ENROLLED_ACCOUNTS_COUNT, 0)
        ratingOmitted = sharedPreferences.getBoolean(RATING_OMITTED, false)
        appVersion = buildVersionProvider.provideBuildVersionCode()
        currentTime = System.currentTimeMillis()
        currentSessionId = sessionDomainManager.getCurrentUserSessionId()
    }

    fun saveLastRatingParameters(
        omitted: Boolean
    ) {
        lastRating = currentRating
        lastRatingTime = System.currentTimeMillis()
        lastRatedVersion = appVersion
        lastRatedInSessionId = currentSessionId
        ratingOmitted = omitted
        currentTime = System.currentTimeMillis()
        _ratingRulesFulfilled.value = OneTimeEvent(false)

        sharedPreferences.edit()
            .putInt(RATING_LAST_RATING, currentRating)
            .putLong(RATING_LAST_RATING_TIME, System.currentTimeMillis())
            .putInt(RATING_LAST_RATED_VERSION, appVersion)
            .putLong(RATING_LAST_RATED_IN_SESSION, lastRatedInSessionId)
            .putBoolean(RATING_OMITTED, omitted)
            .apply()
    }

    private fun hasDashboardBeenEnteredMoreThanOnceInASession(): Boolean {
        val dashboardEnteredInSessionId = (sharedPreferences.getLong(
            DASHBOARD_ENTRY_SESSION_ID_KEY,
            -1
        ))
        return (dashboardEnteredInSessionId == currentSessionId).also {
            sharedPreferences.edit().putLong(DASHBOARD_ENTRY_SESSION_ID_KEY, currentSessionId)
                .apply()
        }
    }

    /**
     * If any negative scenario is fullfilled, rating popup will be disabled
     */
    fun evaluateRatingConditions() =
        viewModelScope.launch {
            getLastRatingParams()
            remoteConfigManager.getSmartRatingConfig()?.let { smartRatingConfig ->
                if (smartRatingConfig.enabled)
                    with(smartRatingConfig.conditions) {
                        _ratingRulesFulfilled.value = OneTimeEvent(
                            !((android.low_version.enable && appVersion < (android.low_version.value).toVersionCode()) // App version under lowest allowed version
                                    || (android.high_version.enable && appVersion > (android.high_version.value).toVersionCode()) // App version over highest allowed version
                                    || (onceeachversion.enable && onceeachversion.value && lastRatedVersion == appVersion) // One rating per version allowed, already rated version
                                    || (interval.enable && (currentTime - lastRatingTime) < interval.value * DateUtils.SECOND_IN_MILLIS) // not enough time since the last rating
                                    || (enrolledaccounts.enable && enrolledaccounts.value && enrolledAccountCount == 0) // Enrolled accounts mandatory, no enrolled accounts
                                    || (waittime.enable && (if (waittime.value == 0) !hasDashboardBeenEnteredMoreThanOnceInASession() else (currentTime - appOpenTime) < waittime.value * DateUtils.SECOND_IN_MILLIS)) // not enough time from app opening
                                    || (firstshown.enable && (currentTime - appInstallTime) < firstshown.value * DateUtils.SECOND_IN_MILLIS) // Not enough time from app instalation
                                    || (optionID.enable && lastRating > optionID.value) // Last rating is good enough
                                    || (omitted.enable && !omitted.value && ratingOmitted) // it's forbidden to show popup after it's omitted and it was omitted
                                    || (lastRatedInSessionId == currentSessionId)) // the dialog was already shown in current session
                        )
                    }
                else
                    _ratingRulesFulfilled.value = OneTimeEvent(false)
            }
        }

    fun setCurrentTime(time: Long) {
        currentTime = time
    }

    private fun analyticsImprovementOptionIndexes() = improvementOptionsList
        .filter { it.applied }
        .map { it.id }.toString()
        .replace("[", "{")
        .replace("]", "}")


    private fun analyticsImprovementOptionValues() = improvementOptionsList
        .filter { it.applied }
        .map { it.title }.toString()
        .replace("[", "{")
        .replace("]", "}")

    private fun analyticsDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}-${
            calendar.get(
                Calendar.YEAR
            )
        }"
    }

    private fun analyticsTime(): String {
        val calendar = Calendar.getInstance()
        val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
        val zoneOffset = calendar.get(Calendar.ZONE_OFFSET) / 3600000
        return timeString + " (GMT+${zoneOffset})"
    }

    fun getRatingAnalyticsEvent(
        review: String,
        closingAction: String,
    ) = AppRatingAnalytics(
        analyticsDate(),
        analyticsTime(),
//        Removed email encryption is added
//        userEmail,
        currentRating.toString(),
        if (currentRating < 4) analyticsImprovementOptionIndexes() else "",
        if (currentRating < 4) analyticsImprovementOptionValues() else "",
        review,
        closingAction,
        "android",
        BuildConfig.VERSION_NAME
    )
}

data class ImprovementOptionUI(
    val id: Int,
    val title: String,
    var applied: Boolean
)

fun ImprovementOption.toImprovementOptionUI() = ImprovementOptionUI(id, value, false)

private const val DASHBOARD_ENTRY_SESSION_ID_KEY = "dashboard_entry_key"

