/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.DataAsCurrencyFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.rewards.RewardsViewModel
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.ButtonAdapter
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.RewardsAdapter
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.model.rewards.RewardsCategory

@AndroidEntryPoint
class DataAsCurrencyFragment :
    NoBottomNavViewBindingFragment<DataAsCurrencyFragmentBinding>(bindViewBy = {
        DataAsCurrencyFragmentBinding.inflate(it)
    }) {

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val dataAsCurrencyViewModel: DataAsCurrencyViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val qualificationsAdapter by lazy { QualificationsAdapter(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        with(viewBinding) {
            with(dataAsCurrencyViewModel) {

                wfDataAsCurrency.onBack {
                    findNavController().navigateUp()
                }

                arguments?.let { bundle ->
                    if (bundle.getBoolean(ACCOUNTS_COUNT_UPDATE_REQUIRED)) {
                        rewardsViewModel.getEnrolledAccountsCount()
                    }
                }

                sQualifications.adapter = qualificationsAdapter
                sQualifications.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (position > 0) {
                                qualificationsAdapter.getItem(position)?.let {
                                    selectQualification(it)
                                    selectedAmount.value = it.min
                                    tvConversionRateSecond.text = resources.getQuantityString(
                                        R.plurals.reward_points,
                                        it.exchangeRate,
                                        it.exchangeRate
                                    )
                                    clConversionRate.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                qualifications.observe(viewLifecycleOwner) { list ->
                    list.sortedByDescending { it.dataRemaining }
                        // filtering out the postpaid broadband accounts
                        .filter { !it.enrolledAccount.isPostpaidBroadband() }
                        .let { sortedList ->
                            qualificationsAdapter.updateContents(sortedList)

                            selectedQualification.value?.let { selected ->
                                sQualifications.setSelection(sortedList.indexOf(selected) + 1)
                            }
                        }
                }

                etDataAmount.addTextChangedListener { editable ->
                    selectedAmount.value = editable.toString().toIntOrNull() ?: 0
                }

                selectedAmount.observe(viewLifecycleOwner, { amount ->

                    if (amount != 0 && etDataAmount.text.toString().toIntOrNull() != amount)
                        etDataAmount.setText(amount.toString())
                    else if (amount == 0 && etDataAmount.text.toString().isNotEmpty())
                        etDataAmount.setText("")

                    selectedQualification.value?.let {
                        when {
                            amount < it.min -> {
                                enableError(
                                    getString(R.string.error_below_min_title, it.min),
                                    getString(
                                        R.string.error_below_min_description,
                                        it.brand?.uiName,
                                        it.min
                                    )
                                )
                                btnConvertData.isEnabled = false
                                clYouWillGet.visibility = View.GONE
                            }
                            amount > it.max -> {
                                enableError(
                                    getString(R.string.error_above_max_title),
                                    getString(R.string.error_above_max_description)
                                )
                                btnConvertData.isEnabled = false
                                clYouWillGet.visibility = View.GONE
                            }
                            else -> {
                                disableError()
                                btnConvertData.isEnabled = true
                                selectedQualification.value?.exchangeRate?.let {
                                    val points = it * amount
                                    tvReceivingPoints.text = "$points"
                                    tvPoints.text = getString(
                                        if (points == 1) R.string.pt else R.string.pts
                                    )
                                }
                                clYouWillGet.visibility = View.VISIBLE
                            }
                        }

                        setMinusEnabled(amount > it.min)
                        setPlusEnabled(amount < it.max)

                        vAmountOverlay.visibility = View.GONE
                    }
                })
                ivPlus.setOnClickListener {
                    selectedAmount.value?.let {
                        selectedAmount.value = it + 1
                    }
                }
                ivMinus.setOnClickListener {
                    selectedAmount.value?.let {
                        selectedAmount.value = it - 1
                    }
                }

                val rewardsAdapter = RewardsAdapter {
                    findNavController().safeNavigate(
                        DataAsCurrencyFragmentDirections.actionDataAsCurrencyFragmentToRewardDetailsFragment(
                            it
                        )
                    )
                }
                val buttonAdapter = ButtonAdapter {
                    findNavController().safeNavigate(
                        DataAsCurrencyFragmentDirections.actionDataAsCurrencyFragmentToAllRewardsInnerFragment(
                            RewardsCategory.NONE
                        )
                    )
                }

                rvRewards.adapter = ConcatAdapter(
                    rewardsAdapter,
                    buttonAdapter
                )

                dataAsCurrencyViewModel.randomRewardsFromEachCategory.observe(viewLifecycleOwner) {
                    rewardsAdapter.submitList(it)
                }

                btnConvertData.setOnClickListener {
                    findNavController().safeNavigate(R.id.action_dataAsCurrencyFragment_to_dataAsCurrencyProcessingFragment)
                }

                tvLearnMore.setOnClickListener {
                    generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(REWARDS_DETAILS_URL)
                        )
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun setMinusEnabled(enabled: Boolean) {
        with(viewBinding) {
            ivMinus.isEnabled = enabled
            ivMinusBackground.isEnabled = enabled
        }
    }

    private fun setPlusEnabled(enabled: Boolean) {
        with(viewBinding) {
            ivPlus.isEnabled = enabled
            ivPlusBackground.isEnabled = enabled
        }
    }

    private fun enableError(title: String, description: String) {
        with(viewBinding) {
            GlobeSnackbarLayoutBinding.inflate(LayoutInflater.from(requireContext())).apply {
                ivGlobeSnackbarIcon.setImageResource(R.drawable.ic_snackbar_action_error)
                tvGlobeSnackbarTitle.text = title
                tvGlobeSnackbarDescription.text = description
            }.let { showSnackbar(it) }

            etDataAmount.setTextColor(
                AppCompatResources.getColorStateList(etDataAmount.context, R.color.red)
            )
            tvGbSuffix.setTextColor(
                AppCompatResources.getColorStateList(tvGbSuffix.context, R.color.red)
            )
        }
    }

    private fun disableError() {
        with(viewBinding) {
            etDataAmount.setTextColor(
                AppCompatResources.getColorStateList(etDataAmount.context, R.color.accent_dark)
            )
            tvGbSuffix.setTextColor(
                AppCompatResources.getColorStateList(tvGbSuffix.context, R.color.accent_dark)
            )
        }
    }

    override val logTag = "DataAsCurrencyFragment"
}

const val ACCOUNTS_COUNT_UPDATE_REQUIRED = "AccountsCountUpdateRequired"
