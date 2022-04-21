/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.confirmaccount

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.CheckNameResult
import ph.com.globe.globeonesuperapp.databinding.AddAccountConfirmFragmentBinding
import ph.com.globe.model.util.ACCOUNT_STATUS_ACTIVE
import ph.com.globe.model.util.ACCOUNT_STATUS_DISCONNECTED
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.formatLandlineNumber
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.brand.GLOBE_PLATINUM
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountConfirmFragment : NoBottomNavViewBindingFragment<AddAccountConfirmFragmentBinding>(
    bindViewBy = {
        AddAccountConfirmFragmentBinding.inflate(it)
    }
), AnalyticsScreen {
    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    private val confirmFragmentArgs by navArgs<AddAccountConfirmFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()

        val confirmAccountArgs = confirmFragmentArgs.confirmAccountArgs
        with(viewBinding) {

            tvBrand.text =
                if (confirmAccountArgs.isPremiumAccount) GLOBE_PLATINUM else confirmAccountArgs.brand.toUserFriendlyBrandName(
                    confirmAccountArgs.segment
                )

            confirmAccountArgs.mobileNumber?.let {
                tvMobileNumber.text = it.formattedForPhilippines().formatPhoneNumber()
                tvMobileNumberTitle.visibility = View.VISIBLE
                tvMobileNumber.visibility = View.VISIBLE
                vMobileNumberLine.visibility = View.VISIBLE
            }

            confirmAccountArgs.accountStatus?.let {
                when (it) {
                    ACCOUNT_STATUS_ACTIVE -> tvStatus.backgroundTintList =
                        AppCompatResources.getColorStateList(requireContext(), R.color.success)
                    ACCOUNT_STATUS_DISCONNECTED -> tvStatus.backgroundTintList =
                        AppCompatResources.getColorStateList(requireContext(), R.color.caution)
                }
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = it
            }

            confirmAccountArgs.landlineNumber?.let {
                val formattedLandlineNumber = it.formatLandlineNumber()
                val isVisible = formattedLandlineNumber.isNotEmpty()
                vLandlineNumberLine.isVisible = isVisible
                tvLandlineNumberValue.isVisible = isVisible
                tvLandlineNumberTitle.isVisible = isVisible
                tvLandlineNumberValue.text = formattedLandlineNumber
            }

            confirmAccountArgs.accountNumber?.let {
                vAccountNumberLine.visibility = View.VISIBLE
                tvAccountNumberValue.visibility = View.VISIBLE
                tvAccountNumberTitle.visibility = View.VISIBLE
                tvAccountNumberValue.text = it
            }

            confirmAccountArgs.accountName?.let {
                vAccountNameLine.visibility = View.VISIBLE
                tvAccountName.visibility = View.VISIBLE
                tvAccountNameTitle.visibility = View.VISIBLE
                tvAccountName.text = it
            }

            etAccountName.addTextChangedListener {
                tvAccountNameError.visibility = View.GONE
                btnAddAccount.isEnabled = !it.isNullOrBlank()
            }

            ivBack.setOnClickListener {
                findNavController().popBackStack(R.id.addAccountNumberFragment, false)
            }

            vStepLine4.isVisible = confirmFragmentArgs.enrollWithoutOtp

            with(addAccountMoreAccountsViewModel) {
                ivClose.setOnClickListener {
                    skipAddingAccount(
                        {
                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                BaseActivity.DASHBOARD_KEY,
                                R.id.dashboardFragment
                            )
                        },
                        {}
                    )
                }

                // TODO add edit nickname event once that is implemented

                btnCancel.setOnClickListener {

                    logUiActionEvent("Skip this account")
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            ADD_ACCOUNT_SCREEN, BUTTON, CANCEL
                        )
                    )
                    skipAddingAccount(
                        {
                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                BaseActivity.DASHBOARD_KEY,
                                R.id.dashboardFragment
                            )
                        },
                        {}
                    )
                }

                btnAddAccount.setOnClickListener {
                    val accountAlias = etAccountName.text.toString()

                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            ADD_ACCOUNT_SCREEN, BUTTON, ADD_ACCOUNT
                        )
                    )
                    addAccountMoreAccountsViewModel.checkName(accountAlias)
                }

                addAccountMoreAccountsViewModel.checkNameResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        when (result) {
                            is CheckNameResult.NameFieldEmpty -> {
                                tvAccountNameError.apply {
                                    visibility = View.VISIBLE
                                    text = getString(R.string.account_name_empty)
                                }
                            }

                            is CheckNameResult.SameNameExists -> {
                                tvAccountNameError.apply {
                                    visibility = View.VISIBLE
                                    text = getString(R.string.account_name_exists)
                                }
                            }

                            is CheckNameResult.UniqueName -> {
                                enrollAccount(
                                    confirmAccountArgs.pickMsisdnForEnrolment(),
                                    confirmAccountArgs.brand,
                                    confirmAccountArgs.brandType,
                                    confirmAccountArgs.segment,
                                    confirmAccountArgs.referenceId,
                                    result.accountName,
                                    confirmAccountArgs.verificationType
                                )
                            }
                        }
                    }
                })

                addAccountMoreAccountsViewModel.enrollAccountsResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        if (result is AddAccountMoreAccountsViewModel.EnrollAccountsResult.EnrollAccountsSuccess) {
                            findNavController().safeNavigate(AddAccountConfirmFragmentDirections.actionAddAccountConfirmFragmentToAddAccountMoreAccountsFragment())
                        }
                    }
                })
            }
        }
    }

    override val logTag = "AddAccountConfirmFragment"

    override val analyticsScreenName: String = "enrollment.confirm_account"
}
