/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.analytics

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.internal.Version
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.events.EventCategory.*
import ph.com.globe.analytics.events.custom.*
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.auth.LoginStatus
import javax.inject.Inject

interface AnalyticsEventsProvider {

    fun provideEvent(
        category: EventCategory,
        vararg labelParams: String,
        labelKeyword: String = KEYWORD_LABEL,
        productName: String? = null,
        loginSignUpMethod: String? = null,
        searchKeyword: String? = null
    ): AnalyticsEvent

    fun provideScreenViewEvent(
        screenName: String
    ): AnalyticsEvent

    fun provideCustomGAEvent(
        category: GAEventCategory,
        eventLabel: String,
        email: String? = null,
        msisdn: String? = null,
        brand: String? = null,
        cxsMessageId: String? = null,
        loginSignUpMethod: String? = null
    ) : AnalyticsEvent

}

class DefaultAnalyticsEventsProvider @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val authDomainManager: AuthDomainManager
) : AnalyticsEventsProvider {

    override fun provideEvent(
        category: EventCategory,
        vararg labelParams: String,
        labelKeyword: String,
        productName: String?,
        loginSignUpMethod: String?,
        searchKeyword: String?
    ): AnalyticsEvent {

        val params = mutableMapOf(
            EVENT_LABEL to "$labelKeyword=${labelParams.joinToString("-")}",
            LOGGED_IN_STATUS to loggedInStatus,
            USER_AGENT to userAgent
        )

        productName?.let { params.put(PRODUCT_NAME, it) }
        loginSignUpMethod?.let { params.put(LOGIN_SIGNUP_METHOD, it) }
        searchKeyword?.let { params.put(SEARCH_KEYWORD, it) }

        return when (category) {
            is Acquisition -> AcquisitionEvent(category.type, params)
            Conversion -> ConversionEvent(params)
            Core -> CoreSearchEvent(params)
            Engagement -> EngagementEvent(params)
        }
    }

    override fun provideScreenViewEvent(
        screenName: String,
    ) = ScreenViewEvent(
        mapOf(
            LOGGED_IN_STATUS to loggedInStatus,
            SCREEN_NAME to screenName
        )
    )

    override fun provideCustomGAEvent(
        category: GAEventCategory,
        eventLabel: String,
        email: String?,
        msisdn: String?,
        brand: String?,
        cxsMessageId: String?,
        loginSignUpMethod: String?
    ): AnalyticsEvent {
        val params = mutableMapOf(
            EVENT_LABEL to eventLabel,
            USER_ID to email.let { it ?: "{}" },
            LOGGED_IN_STATUS to loggedInStatus,
            MSISDN to msisdn.let { it ?: "{}" },
            BRAND to brand.let { it ?: "{}" },
            CXS_MESSAGE_ID to cxsMessageId.let { it ?: "{}" }
        )
        loginSignUpMethod?.let { params.put(LOGIN_SIGNUP_METHOD, it) }
        return GACustomEvent(category, params)
    }

    private val loggedInStatus: String
        get() {
            val isLoggedIn = authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN
            return if (isLoggedIn) YES
            else NO
        }

    private val userAgent: String =
        "${context.getString(R.string.app_name)}/" +
                "${BuildConfig.VERSION_NAME} " +
                "(${context.packageName}; " +
                "build:${BuildConfig.VERSION_CODE} " +
                "Android SDK ${Build.VERSION.SDK_INT}) " +
                Version.userAgent()
}
