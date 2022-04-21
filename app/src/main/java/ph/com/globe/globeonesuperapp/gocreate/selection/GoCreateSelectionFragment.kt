/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.selection

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeDarkSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.GoCreateSelectionFragmentBinding
import ph.com.globe.globeonesuperapp.gocreate.GoCreateGeneralViewModel
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class GoCreateSelectionFragment :
    NoBottomNavViewBindingFragment<GoCreateSelectionFragmentBinding>(bindViewBy = {
        GoCreateSelectionFragmentBinding.inflate(it)
    }) {

    private val selectionViewModel by viewModels<GoCreateSelectionViewModel>()

    private val generalViewModel: GoCreateGeneralViewModel by hiltNavGraphViewModels(R.id.go_create_subgraph)

    private val validityOptionValues: List<Int> = listOf(3, 7, 15)
    private val dataAmountOptionValues: List<Int> = listOf(1, 3, 5, 10, 15, 20)
    private val boosterDataOptionValues: List<Int> = listOf(1, 5, 10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectionViewModel.setGoCreateOffers(generalViewModel.goCreateOffers)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            generalViewModel.entryPointTitle.observe(viewLifecycleOwner) {
                wfGoCreate.setLabel(it)
            }
            wfGoCreate.onBack {
                findNavController().navigateUp()
            }

            // GoCreate validity section
            sibvValiditySelection.optionValues = validityOptionValues
            sibvValiditySelection.optionDescription = getString(R.string.days_sufix)
            sibvValiditySelection.setOnAmountSelectedListener {
                selectionViewModel.selectValidity(it)
                tvSelectedValidity.text = getString(R.string.valid_for_days, it.toString())
                tvSelectedValidity.visibility = View.VISIBLE
            }
            flValidityContent.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clValidityUnopened, clValidityOpened)
                if (clDataOpened.isVisible) {
                    changeSectionExpandedState(clDataUnopened, clDataOpened)
                }
                if (!sibvValiditySelection.anySelected()) {
                    sibvValiditySelection.selectOption(
                        validityOptionValues.indexOf(DEFAULT_VALIDITY)
                    )
                }
            }

            // GoCreate All-Access Data section
            sibvDataSelection.optionValues = dataAmountOptionValues
            sibvDataSelection.optionDescription = getString(R.string.gb_suffix)
            sibvDataSelection.setOnAmountSelectedListener {
                selectionViewModel.selectAllAccessData(it)
                tvSelectedData.text = getString(R.string.data_in_gb, it)
                tvSelectedData.visibility = View.VISIBLE
            }
            flDataContent.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clDataUnopened, clDataOpened)
                if (clValidityOpened.isVisible) {
                    changeSectionExpandedState(clValidityUnopened, clValidityOpened)
                }
                if (!sibvDataSelection.anySelected()) {
                    sibvDataSelection.selectOption(
                        dataAmountOptionValues.indexOf(DEFAULT_ALL_ACCESS_DATA_AMOUNT)
                    )
                }
            }

            // GoCreate Booster Data section (App Data)
            sibvBoosterDataSelection.optionValues = boosterDataOptionValues
            sibvBoosterDataSelection.optionDescription = getString(R.string.gb_suffix)
            sibvBoosterDataSelection.setOnAmountSelectedListener {
                selectionViewModel.selectAppData(it)
            }
            val selectBoosterAdapter = BoosterSelectRecyclerViewAdapter { item ->
                selectionViewModel.selectGoCreateBooster(item)
            }
            flBoostersContent.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clBoostersUnopened, clBoostersOpened)
                if (!sibvBoosterDataSelection.anySelected()) {
                    sibvBoosterDataSelection.selectOption(
                        boosterDataOptionValues.indexOf(DEFAULT_APP_DATA_AMOUNT)
                    )
                }

                // In case if section was collapsed, update selected configuration with Booster 'null' value
                // Note:
                // Previously selected list item will have 'selected' state as true,
                // and this state will be used to display list when user opens this section again
                if (clBoostersUnopened.isVisible) {
                    selectionViewModel.selectGoCreateBooster(null)
                } else {
                    selectionViewModel.selectGoCreateBooster(selectBoosterAdapter.currentList.first { it.selected })
                }
            }
            rvSingleSelectItems.apply {
                adapter = selectBoosterAdapter
                itemAnimator = null
            }
            selectionViewModel.goCreateBoosters.observe(viewLifecycleOwner) {
                selectBoosterAdapter.submitList(it)
            }

            // GoCreate Calls section
            clCalls.setOnClickListener {
                selectionViewModel.changeUnlimitedCallsState().let { isSelected ->
                    ivCallsSelection.setImageResource(
                        if (isSelected)
                            R.drawable.ic_checkbox_selected
                        else
                            R.drawable.ic_checkbox_empty
                    )
                }
            }

            //GoCreate Texts section is hardcoded

            // Amount and Proceed logic
            tvHowToProceed.setOnClickListener {
                val snackbarViewBinding =
                    GlobeDarkSnackbarLayoutBinding
                        .inflate(LayoutInflater.from(requireContext()))
                showSnackbar(snackbarViewBinding)
            }

            // Proceed
            btnProceed.setOnClickListener {
                with(selectionViewModel) {
                    matchedOffer.value?.let { offer ->
                        generalViewModel.setMatchedOfferConfiguration(
                            offer, getSelectedConfiguration()
                        )
                    }
                }
                findNavController().safeNavigate(
                    GoCreateSelectionFragmentDirections.actionGoCreateSelectionFragmentToGoCreateSummaryFragment()
                )
            }

            // Observe matched offer and update bottom section UI
            selectionViewModel.matchedOffer.observe(viewLifecycleOwner) { matchedOffer ->
                tvAmountValue.apply {
                    text = matchedOffer?.let {
                        it.price.toFloat().toPezosFormattedDisplayBalance()
                    } ?: getString(R.string.empty_price_text)
                    setTextColor(
                        AppCompatResources.getColorStateList(
                            requireContext(),
                            if (matchedOffer != null) R.color.neutral_A_0 else R.color.neutral_A_4
                        )
                    )
                }

                tvHowToProceed.isVisible = matchedOffer == null
                btnProceed.isEnabled = matchedOffer != null
            }

            // Restore selected configuration
            generalViewModel.selectedConfiguration.observe(viewLifecycleOwner) { configuration ->
                configuration?.let {
                    // Validity
                    sibvValiditySelection.selectOption(
                        validityOptionValues.indexOf(configuration.validity)
                    )
                    // Access data
                    sibvDataSelection.selectOption(
                        dataAmountOptionValues.indexOf(configuration.accessData)
                    )
                    // App booster
                    if (configuration.appBooster != null) {
                        // Expand boosters section
                        changeSectionExpandedState(clBoostersUnopened, clBoostersOpened)
                        sibvBoosterDataSelection.selectOption(
                            boosterDataOptionValues.indexOf(configuration.appBoosterData)
                        )
                    }
                    // Unlimited calls
                    ivCallsSelection.setImageResource(
                        if (configuration.unlimitedCalls)
                            R.drawable.ic_checkbox_selected
                        else
                            R.drawable.ic_checkbox_empty
                    )
                }
            }
        }
    }

    private fun changeSectionExpandedState(unopened: View, opened: View) {
        unopened.isVisible = !unopened.isVisible
        opened.isVisible = !opened.isVisible
    }

    companion object {
        const val DEFAULT_VALIDITY = 7
        const val DEFAULT_ALL_ACCESS_DATA_AMOUNT = 3
        const val DEFAULT_APP_DATA_AMOUNT = 1
    }

    override val logTag = "GoCreateSelectionFragment"
}
