/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package com.globe.inappupdate.usecases

import com.globe.inappupdate.remote_config.RemoteConfigManager
import ph.com.globe.model.app_update.InAppUpdateResult
import ph.com.globe.model.app_update.toVersionCode
import ph.com.globe.util.LfResult
import javax.inject.Inject

class InAppUpdateUseCase @Inject constructor(private val remoteConfigManager: RemoteConfigManager) {
    suspend fun execute(currentVersionCode: Int): LfResult<InAppUpdateResult, Exception> {
        val conditions = remoteConfigManager.getAppUpdateConditions()
        return when {
            conditions?.updateEnabled != true || conditions.highVersion.toVersionCode() <= currentVersionCode ->
                LfResult.success(InAppUpdateResult.NoUpdate)

            conditions.highVersion.toVersionCode() > currentVersionCode && conditions.lowVersion.toVersionCode() <= currentVersionCode ->
                LfResult.success(InAppUpdateResult.RecommendedUpdate)

            conditions.lowVersion.toVersionCode() > currentVersionCode ->
                LfResult.success(InAppUpdateResult.MandatoryUpdate)

            else ->
                // will never happen since all the cases are covered above
                LfResult.failure(Exception("Invalid state"))
        }
    }
}
