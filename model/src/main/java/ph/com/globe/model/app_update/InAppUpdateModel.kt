/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.app_update

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

sealed class InAppUpdateResult {
    object MandatoryUpdate : InAppUpdateResult()
    object RecommendedUpdate : InAppUpdateResult()
    object NoUpdate : InAppUpdateResult()
}

data class AppUpdateConditions(
    val updateEnabled: Boolean,
    val lowVersion: String,
    val highVersion: String
)

@JsonClass(generateAdapter = true)
internal data class AppUpdateConditionsJson(
    val enabled: Boolean,
    val conditions: ConditionsJson
)

@JsonClass(generateAdapter = true)
internal data class ConditionsJson(
    val android: AndroidConditionsJson
)

@JsonClass(generateAdapter = true)
internal data class AndroidConditionsJson(
    val low_version: VersionConditionJson,
    val high_version: VersionConditionJson
)

@JsonClass(generateAdapter = true)
internal data class VersionConditionJson(
    val value: String
)

fun String.toAppUpdateConditions(): AppUpdateConditions? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(AppUpdateConditionsJson::class.java)

    val appUpdateConditionsJson = adapter.fromJson(this)

    return appUpdateConditionsJson?.let {
        return AppUpdateConditions(
            it.enabled,
            it.conditions.android.low_version.value,
            it.conditions.android.high_version.value
        )
    }
}

fun String.toVersionCode(): Int {

    var versionCode = 0

    try {
        val versions = this.split('.')

        versionCode += versions[0].toInt() * 100
        versionCode += versions[1].toInt()
        versionCode *= 100

        if (this.contains("rc")) {
            val patchAndCandidate = versions[2].split("rc")
            versionCode += patchAndCandidate[0].toInt()
            versionCode *= 10
            versionCode += patchAndCandidate[1].toInt() - 1
        } else {
            versionCode += versions[2].toInt()
            versionCode = versionCode * 10 + 9
        }
    } catch (e: Exception) {
        // Silently catch exceptions
    }

    return versionCode
}
