/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package com.globe.inappupdate.usecases

import com.globe.inappupdate.remote_config.RemoteConfigManager
import ph.com.globe.model.personalized_campaign.PersonalizedCampaignConfig
import ph.com.globe.model.personalized_campaign.filterDateValidity
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.LfResult
import javax.inject.Inject

class PersonalizedCampaignUseCase @Inject constructor(private val remoteConfigManager: RemoteConfigManager) {
    suspend fun execute(brand: AccountBrand): LfResult<List<PersonalizedCampaignConfig>, Exception> {
        val campaigns = remoteConfigManager.getPersonalizedCampaigns()

        val brandedCampagins = campaigns?.filter {
            it.applied_brand.contains(brand.name)
        }

        val filteredCampaigns = brandedCampagins?.filterDateValidity()

        return if (!filteredCampaigns.isNullOrEmpty()) {
            LfResult.success(filteredCampaigns)
        } else {
            LfResult.failure(Exception("Campaign id doesn't not exist or expired campaign"))
        }
    }
}
