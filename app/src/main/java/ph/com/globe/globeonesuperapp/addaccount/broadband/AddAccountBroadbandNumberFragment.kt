/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.skydoves.expandablelayout.ExpandableLayout
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
import ph.com.globe.globeonesuperapp.databinding.AddAccountBroadbandNumberFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.closeKeyboard
import ph.com.globe.globeonesuperapp.utils.formatCountryCodeForBroadband
import ph.com.globe.globeonesuperapp.utils.hideError
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.showError
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment.Broadband
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountBroadbandNumberFragment :
    NoBottomNavViewBindingFragment<AddAccountBroadbandNumberFragmentBinding>(
        bindViewBy = {
            AddAccountBroadbandNumberFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }
    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    private var addBroadbandManually = false

    private var expandableLayouts: List<ExpandableLayout> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addAccountBroadbandNumberViewModel.updateAddBroadbandManuallyValue(true)

        with(viewBinding) {
            adjustStatus()

            expandableLayouts = listOf(elAccountNumber, elLandlineNumber, elHpwNumber)

            expandableLayouts.forEach { currentExpandableLayout ->
                currentExpandableLayout.setOnClickListener {
                    expandableLayouts.toggle(currentExpandableLayout)
                }
            }

            etAddAccount.addTextChangedListener {
                it.formatCountryCodeForBroadband()
                btnNext.isEnabled =
                    (addBroadbandManually || checkWifiOnAndConnected()) && !it.isNullOrBlank()
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

            addAccountMoreAccountsViewModel.numberToPrefill?.let {
                etAddAccount.setText(it)
            }

            btnNext.setOnClickListener {
                val phoneNumber = etAddAccount.text.toString()
                logUiActionEvent(
                    target = "Add account",
                    additionalParams = mapOf(
                        "type" to Broadband.toString(),
                        "number" to phoneNumber
                    )
                )
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, CLICKABLE_TEXT, ADD_ACCOUNT
                    )
                )
                adjustStatus()
                if (addBroadbandManually || checkWifiOnAndConnected())
                    addAccountMoreAccountsViewModel.checkNumber(
                        phoneNumber,
                        Broadband
                    )
            }

            btnDoItLater.setOnClickListener {
                logUiActionEvent(getString(R.string.do_it_later))
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, CLICKABLE_TEXT, I_WILL_DO_IT_LATER
                    )
                )
                addAccountBroadbandNumberViewModel.skipAddingAccount(
                    {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    },
                    {}
                )
            }

            addAccountBroadbandNumberViewModel.addManually.observe(viewLifecycleOwner) {
                it.handleEvent { addManually ->
                    addBroadbandManually = addManually
                    tvBroadbandTitle.isVisible = !addManually
                    tvBroadbandDescription.isVisible = !addManually
                    clModemStatus.isVisible = !addManually
                }
            }

            addAccountBroadbandNumberViewModel.checkBrandResult.observe(viewLifecycleOwner) {
                it.handleEvent { result ->
                    when (result) {
                        is CheckBrandResult.SuccessfulBrandCheck -> {
                            if (result.brandType == AccountBrandType.Postpaid) {
                                // if we are enrolling a postpaid broadband account there are multiple methods to do it
                                findNavController().safeNavigate(
                                    AddAccountNumberFragmentDirections.actionAddAccountNumberFragmentToAddAccountBroadbandEnrollMethodsFragment(
                                        msisdn = etAddAccount.text.toString(),
                                        alternativeMobileNumber = result.alternativeMobileNumber,
                                        accountNumber = result.accountNumber
                                            ?: etAddAccount.text.toString(),
                                        emailAddress = result.emailAddress,
                                        brand = result.brand,
                                    )
                                )
                            } else {
                                // else if we are enrolling a prepaid account we check wifi and proceed with hpw seamless enrollment (project hack)
                                findNavController().safeNavigate(
                                    AddAccountNumberFragmentDirections.actionAddAccountNumberFragmentToAddAccountBroadbandWifiCheckFragment(
                                        msisdn = etAddAccount.text.toString(),
                                        brand = result.brand,
                                        segment = Broadband
                                    )
                                )
                            }
                        }
                        is CheckBrandResult.NotGlobeNumber -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.not_valid_globe_number_broadband)
                            )
                        }

                        is CheckBrandResult.InactiveAccount -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.inactive_account_error)
                            )
                        }

                        is CheckBrandResult.NotABroadbandNumber -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.not_hpw_number)
                            )
                        }

                        is CheckBrandResult.NotAMobileNumber -> Unit

                        is CheckBrandResult.NoLongerInSystemAccount -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.no_longer_in_system)
                            )
                        }
                    }
                }
            }

            verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner) {
                it.handleEvent { result ->
                    when (result) {
                        is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                            findNavController().safeNavigate(
                                AddAccountNumberFragmentDirections.actionAddAccountNumberFragmentToAddAccountEnterOtpFragment(
                                    msisdn = result.msisdn,
                                    targetMobileNumber = result.msisdn,
                                    referenceId = result.referenceId,
                                    brand = addAccountBroadbandNumberViewModel.rawBrand.value!!,
                                    brandType = result.brandType,
                                    segment = Broadband
                                )
                            )
                        }
                        else -> Unit
                    }
                }
            }

            addAccountMoreAccountsViewModel.checkNumberResultBroadband.observe(viewLifecycleOwner) {
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
                            addAccountBroadbandNumberViewModel.checkBrand(
                                etAddAccount.text.toString(),
                                Broadband
                            )
                        }

                        is CheckNumberResult.InvalidNumberFormat -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.not_valid_a_number)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun adjustStatus() {
        with(viewBinding) {
            if (checkWifiOnAndConnected()) {
                tvWifiStatus.text = getString(R.string.connected)
                tvWifiStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success,
                        null
                    )
                )
                ivModemStatusIcon.setColorFilter(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success,
                        null
                    )
                )
            } else {
                tvWifiStatus.text = getString(R.string.not_connected)
                tvWifiStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.state_caution_orange,
                        null
                    )
                )
                ivModemStatusIcon.setColorFilter(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.state_caution_orange,
                        null
                    )
                )
            }
        }
    }

    private fun checkWifiOnAndConnected(): Boolean {
        val wifiMng =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return if (wifiMng?.isWifiEnabled == true) { // Wi-Fi adapter is ON
            // Connected to an access point if the networkId != -1
            wifiMng.connectionInfo.networkId != -1
        } else {
            false // Wi-Fi adapter is OFF
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

    override val logTag = "AddAccountBroadbandNumberFragment"

    override val analyticsScreenName: String = "enrollment.add_account"
}

private fun List<ExpandableLayout>.toggle(currentExpandableLayout: ExpandableLayout) {
    if (!currentExpandableLayout.isExpanded) {
        forEach { if (it != currentExpandableLayout && it.isExpanded) it.collapse() }
    }
    currentExpandableLayout.toggleLayout()
}
