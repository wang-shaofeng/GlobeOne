/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.GoCreateSummaryFragmentBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.gocreate.GoCreateGeneralViewModel
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.BUY_PROMO
import ph.com.globe.globeonesuperapp.utils.payment.GO_CREATE
import ph.com.globe.globeonesuperapp.utils.payment.checkoutToPaymentParams
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.domain_models.PROMO_API_SERVICE_PROVISION
import javax.inject.Inject

@AndroidEntryPoint
class GoCreateSummaryFragment :
    NoBottomNavViewBindingFragment<GoCreateSummaryFragmentBinding>(bindViewBy = {
        GoCreateSummaryFragmentBinding.inflate(it)
    }) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val generalViewModel: GoCreateGeneralViewModel by hiltNavGraphViewModels(R.id.go_create_subgraph)

    private val summaryViewModel: GoCreateSummaryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()

        with(viewBinding) {
            with(generalViewModel) {

                wfGoCreate.onBack {
                    findNavController().navigateUp()
                }

                vSelectAccount.setOnClickListener {
                    findNavController().safeNavigate(
                        GoCreateSummaryFragmentDirections.actionGoCreateSummaryFragmentToGoCreateSelectAccountFragment()
                    )
                }

                mobileNumber.observe(viewLifecycleOwner) { number ->
                    etMobileNumber.setText(number)
                }

                accountName.observe(viewLifecycleOwner) { name ->
                    tilMobileNumber.hint = name ?: getString(R.string.mobile_number)
                }

                btnTryAgainNextTime.setOnClickListener {
                    summaryViewModel.showTryAgainNextTimeDialog {
                        // Pop back stack to GoCreate entry point screen
                        findNavController().popBackStack(R.id.goCreateIntroFragment, true)
                    }
                }

                generalViewModel.selectedConfiguration.observe(viewLifecycleOwner) { configuration ->
                    configuration?.let {
                        tvValidityValue.text =
                            getString(R.string.valid_for_days, configuration.validity.toString())
                        tvAccessDataValue.text =
                            getString(R.string.selected_access_data_gb, configuration.accessData)

                        groupApps.isVisible = configuration.appBooster != null
                        configuration.appBooster?.let { booster ->
                            tvAppsValue.text = getString(
                                R.string.selected_apps_booster,
                                configuration.appBoosterData,
                                configuration.appBooster.title
                            )
                            for (icon in booster.icons) {
                                val chip =
                                    ChipImageLayoutBinding.inflate(LayoutInflater.from(context))
                                GlobeGlide.with(chip.ivChipImage).load(icon).into(chip.ivChipImage)
                                cgAppsImages.addView(chip.root)
                            }
                        }

                        groupUnlimitedCalls.isVisible = configuration.unlimitedCalls
                    }
                }

                generalViewModel.matchedGoCreateOffer.observe(viewLifecycleOwner) { offer ->
                    tvPromoPrice.text = offer.price.toFloat().toPezosFormattedDisplayBalance()

                    btnAction.apply {
                        text = getString(R.string.subscribe)
                        setOnClickListener {
                            with(offer) {
                                val paymentParams = checkoutToPaymentParams(
                                    paymentType = BUY_PROMO,
                                    mobileNumber = generalViewModel.mobileNumber.value ?: "",
                                    amount = price.toDouble(),
                                    promoChargeId = chargePromoId,
                                    promoNonChargeId = nonChargePromoId,
                                    chargePromoParam = chargeServiceParam,
                                    nonChargePromoParam = nonChargeServiceParam,
                                    paymentName = GO_CREATE,
                                    validity = validity,
                                    price = price.toDouble(),
                                    skelligWallet = skelligWallet,
                                    skelligCategory = skelligCategory,
                                    provisionByServiceId = apiSubscribe == PROMO_API_SERVICE_PROVISION
                                )
                                findNavController().safeNavigate(
                                    GoCreateSummaryFragmentDirections.actionGoCreateSummaryFragmentToPaymentSubgraph(
                                        paymentParams
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override val logTag = "GoCreateSummaryFragment"
}
