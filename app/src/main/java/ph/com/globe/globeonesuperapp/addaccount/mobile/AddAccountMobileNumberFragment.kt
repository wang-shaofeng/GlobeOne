/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.AddAccountNumberFragmentDirections
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.CheckNumberResult
import ph.com.globe.globeonesuperapp.databinding.AddAccountMobileNumberFragmentBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.MOBILE_SEGMENT
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountMobileNumberFragment :
    NoBottomNavViewBindingFragment<AddAccountMobileNumberFragmentBinding>(
        bindViewBy = {
            AddAccountMobileNumberFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            etAddAccount.addTextChangedListener {
                it.formatCountryCodeIfExists()

                requireContext().hideError(tilAddAccount, etAddAccount)
            }

            etAddAccount.setOnFocusChangeListener { _, _ ->
                refactorCopiedNumber()
            }

            etAddAccount.setOnClickListener {
                refactorCopiedNumber()
            }

            etAddAccount.setOnEditorActionListener { v, _, _ ->
                closeKeyboard(v, requireContext())
                true
            }

            btnAddAccount.setOnClickListener {
                val phoneNumber = etAddAccount.text.toString()

                logUiActionEvent(
                    target = "Add account",
                    additionalParams = mapOf(
                        "type" to MOBILE_SEGMENT,
                        "number" to phoneNumber
                    )
                )
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, BUTTON, ADD_ACCOUNT
                    )
                )

                addAccountMoreAccountsViewModel.checkNumber(
                    phoneNumber.convertToPrefixNumberFormat(),
                    AccountSegment.Mobile
                )
            }

            btnDoItLater.setOnClickListener {

                logUiActionEvent(getString(R.string.do_it_later))
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, BUTTON, I_WILL_DO_IT_LATER
                    )
                )
                addAccountMoreAccountsViewModel.skipAddingAccount(
                    {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    },
                    {}
                )
            }

            addAccountMoreAccountsViewModel.checkNumberResultMobile.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is CheckNumberResult.NumberFieldEmpty -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.account_number_empty)
                            )
                        }

                        is CheckNumberResult.SameNumberExists -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.account_number_exists)
                            )
                        }

                        is CheckNumberResult.UniqueNumber -> {
                            verifyOtpViewModel.addAccountSendOtp(
                                msisdn = result.phoneNumber,
                                segment = AccountSegment.Mobile,
                            )
                        }
                    }
                }
            })

            verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                            findNavController().safeNavigate(
                                AddAccountNumberFragmentDirections.actionAddAccountNumberFragmentToAddAccountEnterOtpFragment(
                                    msisdn = result.msisdn,
                                    referenceId = result.referenceId,
                                    brand = result.brand,
                                    brandType = result.brandType,
                                    segment = AccountSegment.Mobile,
                                    targetMobileNumber = result.msisdn.convertToPrefixNumberFormat()
                                )
                            )
                        }

                        is VerifyOtpViewModel.SendOtpResult.NotGlobeNumber -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.not_valid_globe_number)
                            )
                        }

                        is VerifyOtpViewModel.SendOtpResult.InactiveAccount -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.inactive_account_error)
                            )
                        }

                        is VerifyOtpViewModel.SendOtpResult.NotAMobileNumber -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.not_ghp_tm_number)
                            )
                        }
                    }
                }
            })
        }
    }

    private fun refactorCopiedNumber() {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipDataItem = clipboardManager.primaryClip?.getItemAt(0)
        val pastedNumber = clipDataItem?.text.toString().formattedForPhilippines()
        if (pastedNumber.length == 10 || pastedNumber.length == 11)
            clipboardManager.setPrimaryClip(
                ClipData.newPlainText("Phone Number", pastedNumber)
            )
    }

    override val logTag = "AddAccountMobileNumberFragment"
    override val analyticsScreenName = "enrollment.add_account"
}
