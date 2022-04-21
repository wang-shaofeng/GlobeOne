/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package com.globe.inappupdate.remote_config

import android.content.Context
import com.globe.inappupdate.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.model.app_update.AppUpdateConditions
import ph.com.globe.model.app_update.toAppUpdateConditions
import ph.com.globe.model.feature_activation.FeatureActivationModel
import ph.com.globe.model.feature_activation.toFeatureActivationConfig
import ph.com.globe.model.prepaid.toChannelConfigMap
import ph.com.globe.model.personalized_campaign.PersonalizedCampaignConfig
import ph.com.globe.model.personalized_campaign.toPersonalizedCampaignConfig
import ph.com.globe.model.raffle.RaffleRemoteConfigModel
import ph.com.globe.model.raffle.raffleRemoteConfigModelsFromJson
import ph.com.globe.model.rewards.DataAsCurrencyConfig
import ph.com.globe.model.rewards.toDataAsCurrencyConfig
import ph.com.globe.model.rush.RushRemoteConfigData
import ph.com.globe.model.rush.toRushRemoteConfigData
import ph.com.globe.model.smart_rating.SmartRatingConfig
import ph.com.globe.model.smart_rating.toSmartRatingConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(@ApplicationContext val context: Context) :
    HasLogTag {

    private val firebaseRemoteConfig = Firebase.remoteConfig

    fun initialize() {
        firebaseRemoteConfig.setDefaultsAsync(
            remoteConfigDefaults
        )
    }

    suspend fun getAppUpdateConditions(): AppUpdateConditions? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val forcedUpdateConditionsJson =
                remoteConfigEntries[BuildConfig.FORCED_UPDATE]?.asString()

            try {
                forcedUpdateConditionsJson?.toAppUpdateConditions()
            } catch (e: Exception) {
                eLog(Exception("Bad forced upgrade config: $forcedUpdateConditionsJson"))
                remoteConfigDefaults[BuildConfig.FORCED_UPDATE]?.toAppUpdateConditions()
            }
        }

    suspend fun getSmartRatingConfig(): SmartRatingConfig? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val smartRatingConfigJson =
                remoteConfigEntries[BuildConfig.SMART_RATING_CONFIG]?.asString()
            try {
                smartRatingConfigJson?.toSmartRatingConfig()
            } catch (e: Exception) {
                eLog(Exception("Bad smart rating config: $smartRatingConfigJson"))
                remoteConfigDefaults[BuildConfig.SMART_RATING_CONFIG]?.toSmartRatingConfig()
            }
        }

    suspend fun getRafflesConfig(): List<RaffleRemoteConfigModel>? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()
            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val raffleConfigJson =
                remoteConfigEntries[BuildConfig.RAFFLE]?.asString()

            try {
                raffleConfigJson?.raffleRemoteConfigModelsFromJson()
            } catch (e: Exception) {
                eLog(Exception("Bad raffle config: $raffleConfigJson"))
                null
            }
        }

    suspend fun getRushData(): RushRemoteConfigData? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val rushRemoteConfigJson =
                remoteConfigEntries[BuildConfig.RUSH_CAMPAIGN]?.asString()
            try {
                rushRemoteConfigJson?.toRushRemoteConfigData()
            } catch (e: Exception) {
                eLog(Exception("Bad rush campaign config: $rushRemoteConfigJson"))
                remoteConfigDefaults[BuildConfig.RUSH_CAMPAIGN]?.toRushRemoteConfigData()
            }
        }

    suspend fun getDataAsCurrencyConfig(): DataAsCurrencyConfig? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val dataAsCurrencyJson = remoteConfigEntries[BuildConfig.DAC_CONFIG]?.asString()

            try {
                dataAsCurrencyJson?.toDataAsCurrencyConfig()
            } catch (e: Exception) {
                eLog(Exception("Bad data as currency config: $dataAsCurrencyJson"))
                remoteConfigDefaults[BuildConfig.DAC_CONFIG]?.toDataAsCurrencyConfig()
            }
        }

    suspend fun getFeatureActivationConfig(): List<FeatureActivationModel>? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val featureActivationConfigJson =
                remoteConfigEntries[BuildConfig.FEATURE_ACTIVATION]?.asString()

            try {
                featureActivationConfigJson?.toFeatureActivationConfig()
            } catch (e: Exception) {
                eLog(Exception("Bad feature activation config: $featureActivationConfigJson"))
                remoteConfigDefaults[BuildConfig.FEATURE_ACTIVATION]?.toFeatureActivationConfig()
            }
        }

    suspend fun getChannelMapConfig(): Map<String, String>? =
        withContext(Dispatchers.IO) {
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val promoChannelMapJson = remoteConfigEntries[BuildConfig.CHANNEL_MAP]?.asString()

            try {
                promoChannelMapJson?.toChannelConfigMap()
            } catch (e: Exception) {
                eLog(Exception("Bad promo channel map config: $promoChannelMapJson"))
                remoteConfigDefaults[BuildConfig.CHANNEL_MAP]?.toChannelConfigMap()
            }
        }

    suspend fun getPersonalizedCampaigns(): List<PersonalizedCampaignConfig>? =
        withContext(Dispatchers.IO){
            executeFetchTask()
            activateFetchTask()

            val remoteConfigEntries = firebaseRemoteConfig.all.mapKeys { it.key as String }
            val personalizedCampaignConfigJson =
                remoteConfigEntries[BuildConfig.PERSONALIZED_CAMPAIGNS]?.asString()

            try {
                personalizedCampaignConfigJson?.toPersonalizedCampaignConfig()
            } catch (e: Exception) {
                eLog(Exception("Bad feature personalized config: $personalizedCampaignConfigJson"))
                remoteConfigDefaults[BuildConfig.PERSONALIZED_CAMPAIGNS]?.toPersonalizedCampaignConfig()
            }
        }

    private suspend fun executeFetchTask() {
        val completableDeferred = CompletableDeferred<Unit>()

        firebaseRemoteConfig.fetch().addOnCompleteListener {
            completableDeferred.complete(Unit)
        }

        completableDeferred.await()
    }

    private suspend fun activateFetchTask() {
        val completableDeferred = CompletableDeferred<Unit>()

        firebaseRemoteConfig.activate().addOnCompleteListener {
            completableDeferred.complete(Unit)
        }

        completableDeferred.await()
    }

    override val logTag: String = "RemoteConfigManager"

}
