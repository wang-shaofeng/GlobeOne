/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.GetConversionQualificationError
import ph.com.globe.globeonesuperapp.domain.BuildConfig
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.profile.response_models.EnrolledAccountJson
import ph.com.globe.model.profile.response_models.pickPrimaryMsisdn
import ph.com.globe.model.profile.response_models.toDomain
import ph.com.globe.model.rewards.GetConversionQualificationParams
import ph.com.globe.model.rewards.QualificationDetails
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetConversionQualificationUseCase @Inject constructor(
    private val rewardsDataManager: RewardsDataManager,
    private val profileDataManager: ProfileDataManager,
    private val accountDataManager: AccountDataManager
) {

    private val ghpRateId by lazy { BuildConfig.GHP_RATE_ID }
    private val pwRateId by lazy { BuildConfig.PW_RATE_ID }
    private val ghpPrepaidRateId by lazy { BuildConfig.GHP_PREPAID_RATE_ID }
    private val tmRateId by lazy { BuildConfig.TM_RATE_ID }

    suspend fun execute(): LfResult<List<QualificationDetails>, GetConversionQualificationError> {
        val qualificationsDetailsList = mutableListOf<QualificationDetails>()
        profileDataManager.getEnrolledAccounts().fold({ enrolledAccounts ->

            val qualificationResults = enrolledAccounts.map { account ->
                CoroutineScope(Dispatchers.IO).async {
                    getConversionQualification(account)
                }
            }.awaitAll()

            qualificationsDetailsList.addAll(qualificationResults.flatten())

        }, {
            return LfResult.failure(GetConversionQualificationError.HelperCallsError)
        })
        return LfResult.success(qualificationsDetailsList)
    }

    private suspend fun getConversionQualification(account: EnrolledAccountJson): List<QualificationDetails> {
        val accountQualifications = mutableListOf<QualificationDetails>()

        accountDataManager.getAccountBrand(GetAccountBrandParams(account.pickPrimaryMsisdn()))
            .fold({ brandResponse ->
                rewardsDataManager.getConversionQualification(
                    GetConversionQualificationParams(
                        account.pickPrimaryMsisdn(),
                        brandResponse.result.brand
                    )
                ).fold({ qualificationResult ->
                    qualificationResult.qualifications?.let { qualifications ->
                        val qualificationsDetailsSublist = qualifications.map { qualification ->
                            QualificationDetails(
                                enrolledAccount = account.toDomain(),
                                accountName = account.accountAlias,
                                number = account.pickPrimaryMsisdn(),
                                brand = brandResponse.result.brand,
                                segment = account.segment,
                                promoName = qualification.promoName,
                                min = qualification.min,
                                max = qualification.max,
                                dataRemaining = qualification.dataRemaining,
                                exchangeRate = qualificationResult.rate?.targetRate ?: 1,
                                qualificationId = qualification.qualificationId,
                                rateId = when (brandResponse.result.brand) {
                                    AccountBrand.GhpPostpaid -> ghpRateId
                                    AccountBrand.Hpw -> pwRateId
                                    AccountBrand.GhpPrepaid -> ghpPrepaidRateId
                                    AccountBrand.Tm -> tmRateId
                                    else -> ""
                                }
                            )
                        }
                        if (qualificationsDetailsSublist.isNotEmpty()) {
                            if (brandResponse.result.brand == AccountBrand.Hpw) accountQualifications.addAll(
                                qualificationsDetailsSublist
                            )
                            else accountQualifications.add(
                                qualificationsDetailsSublist.first()
                            )
                        } else accountQualifications.add(
                            createNoDataQualification(account)
                        )

                    } ?: run {
                        accountQualifications.add(
                            QualificationDetails(
                                enrolledAccount = account.toDomain(),
                                accountName = account.accountAlias,
                                number = account.pickPrimaryMsisdn(),
                                segment = account.segment,
                                error = INSUFFICIENT_DATA
                            )
                        )
                    }
                }, {
                    accountQualifications.add(
                        createNoDataQualification(account)
                    )
                })
            }, {
                accountQualifications.add(
                    createNoDataQualification(account)
                )
            })

        return accountQualifications
    }

    private fun createNoDataQualification(account: EnrolledAccountJson) =
        QualificationDetails(
            enrolledAccount = account.toDomain(),
            accountName = account.accountAlias,
            number = account.pickPrimaryMsisdn(),
            error = NO_DATA
        )
}

const val NO_DATA = "no_data"
const val INSUFFICIENT_DATA = "insufficient_data"
