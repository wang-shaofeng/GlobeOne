/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.broadband.AddAccountBroadbandNumberViewModel
import ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem.MODEM_HUAWEI_SMS_PATH
import ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem.MODEM_HUAWEI_V2_SMS_PATH
import ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem.ModemItem
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel.EnrollHPWAccount.BadModemInfoError
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel.EnrollHPWAccount.EnrollingSuccess
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.AddAccountEnterUsernamePasswordViewModel.ModemWebAppInterface
import ph.com.globe.globeonesuperapp.addaccount.broadband.enterusernamepassword.modemwebclients.*
import ph.com.globe.globeonesuperapp.addaccount.broadband.failurescreen.HpwBroadBandEnrollmentError
import ph.com.globe.globeonesuperapp.addaccount.confirmaccount.ConfirmAccountArgs
import ph.com.globe.globeonesuperapp.databinding.AddAccountEnterUsernamePasswordFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountEnterUsernamePasswordFragment :
    NoBottomNavViewBindingFragment<AddAccountEnterUsernamePasswordFragmentBinding>({
        AddAccountEnterUsernamePasswordFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val addAccountEnterUsernamePasswordViewModel: AddAccountEnterUsernamePasswordViewModel by viewModels()

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    private val addAccountEnterUsernamePasswordFragmentArgs by navArgs<AddAccountEnterUsernamePasswordFragmentArgs>()

    private var isDefaultLogin = true

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        val selectedModem = addAccountEnterUsernamePasswordFragmentArgs.selectedModem

        addAccountEnterUsernamePasswordViewModel.setInfo(
            addAccountEnterUsernamePasswordFragmentArgs.phoneNumber,
            addAccountEnterUsernamePasswordFragmentArgs.referenceId,
            addAccountEnterUsernamePasswordFragmentArgs.brandType,
            addAccountEnterUsernamePasswordFragmentArgs.segment,
        )

        with(viewBinding) {
            wvPingModem.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            ivClose.setOnClickListener {
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            val webAppInterface = addAccountEnterUsernamePasswordViewModel.getWebAppInterface()

            if (isDefaultLogin) {
                executeWebView(selectedModem, webAppInterface)
                clModemInfo.isVisible = false
                clLottieAnimation.isVisible = true
            }

            wvPingModem.addJavascriptInterface(
                webAppInterface,
                GLOBE_AT_HOME
            )

            etUsername.doOnTextChanged { text, _, _, _ ->
                addAccountEnterUsernamePasswordViewModel.usernameChanged(text?.toString())
            }

            etUsername.doAfterTextChanged {
                selectedModem.credential.username = it?.toString() ?: ""
            }

            etPassword.doOnTextChanged { text, _, _, _ ->
                addAccountEnterUsernamePasswordViewModel.passwordChanged(text?.toString())
            }

            etPassword.doAfterTextChanged {
                selectedModem.credential.password = it?.toString() ?: ""
            }

            tvFindWifiUsernamePassword.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, CLICKABLE_TEXT, FIND_MY_USERNAME_AND_PASSWORD
                    )
                )
                findNavController().safeNavigate(AddAccountEnterUsernamePasswordFragmentDirections.actionAddAccountEnterUsernamePasswordFragmentToAddAccountFindUsernamePasswordFragment())
            }

            btnNext.setOnClickListener {

                isDefaultLogin = false

                executeWebView(selectedModem, webAppInterface)

                clModemInfo.isVisible = false
                clLottieAnimation.isVisible = true

                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, BUTTON, NEXT
                    )
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

            addAccountEnterUsernamePasswordViewModel.enableNextButton.observe(viewLifecycleOwner) {
                it.handleEvent { enableButton ->
                    btnNext.isEnabled = enableButton
                }
            }

            addAccountEnterUsernamePasswordViewModel.enrollHPWResult.observe(viewLifecycleOwner) {
                it.handleEvent { result ->
                    when (result) {
                        is BadModemInfoError -> {
                            if (isDefaultLogin) {
                                clModemInfo.isVisible = true
                                clLottieAnimation.isVisible = false
                            } else {
                                findNavController().safeNavigate(
                                    AddAccountEnterUsernamePasswordFragmentDirections.actionAddAccountEnterUsernamePasswordFragmentToAddAccountEnrollBroadbandFailureFragment(
                                        HpwBroadBandEnrollmentError.BadModemInfo,
                                        hpwNumber = addAccountEnterUsernamePasswordFragmentArgs.phoneNumber,
                                        brand = addAccountEnterUsernamePasswordFragmentArgs.brand,
                                        segment = addAccountEnterUsernamePasswordFragmentArgs.segment
                                    )
                                )
                            }
                        }

                        is EnrollingSuccess -> {
                            with(addAccountEnterUsernamePasswordFragmentArgs) {
                                findNavController().safeNavigate(
                                    AddAccountEnterUsernamePasswordFragmentDirections.actionAddAccountEnterUsernamePasswordFragmentToAddAccountConfirmFragment(
                                        ConfirmAccountArgs(
                                            mobileNumber = phoneNumber,
                                            brand = brand,
                                            brandType = brandType,
                                            segment = segment,
                                            referenceId = referenceId
                                        ),
                                        true
                                    )
                                )
                            }
                        }
                        else -> {
                            findNavController().safeNavigate(
                                AddAccountEnterUsernamePasswordFragmentDirections.actionAddAccountEnterUsernamePasswordFragmentToAddAccountEnrollBroadbandFailureFragment(
                                    HpwBroadBandEnrollmentError.SomethingWentWrong
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupWebView(webViewClient: BaseModemWebViewClient, url: String) {
        viewBinding.wvPingModem.webViewClient = webViewClient
        viewBinding.wvPingModem.loadUrl(url)
    }

    private fun executeWebView(
        selectedModem: ModemItem,
        webAppInterface: ModemWebAppInterface
    ) {
        when (selectedModem.name) {
            HUAWEI_V2 -> {
                setupWebView(
                    HuaweiV2WebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        webAppInterface,
                    ),
                    MODEM_HUAWEI_V2_SMS_PATH
                )
            }

            HUAWEI_2CA -> {
                setupWebView(
                    HuaweiV2WebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        webAppInterface
                    ),
                    "${selectedModem.address}/html/overview.html"
                )
            }

            THE_BOX -> {
                setupWebView(
                    BoxWebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        webAppInterface
                    ),
                    "${selectedModem.address}/index.html"
                )
            }

            SHANGHAI_BOOST -> {
                setupWebView(
                    ShanghaiWebViewClient(
                        requireContext(),
                        webAppInterface
                    ),
                    selectedModem.address
                )
            }

            HUAWEI -> {
                setupWebView(
                    HuaweiWebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        MODEM_HUAWEI_SMS_PATH,
                        webAppInterface
                    ),
                    MODEM_HUAWEI_V2_SMS_PATH
                )
            }

            FENDI -> {
                setupWebView(
                    FendiWebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        webAppInterface
                    ),
                    "${selectedModem.address}/index.html"
                )
            }

            NOTION -> {
                setupWebView(
                    NotionWebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        webAppInterface
                    ),
                    "${selectedModem.address}/"
                )
            }

            TOZED -> {
                setupWebView(
                    TozedWebViewClient(
                        requireContext(),
                        selectedModem.credential.username,
                        selectedModem.credential.password,
                        webAppInterface
                    ),
                    "${selectedModem.address}/index.html"
                )
            }

            else -> {
                addAccountEnterUsernamePasswordViewModel.navigateToSomethingWentWrongScreen()
            }
        }
    }

    override val logTag = "AddAccountEnterUsernamePasswordFragment"

    override val analyticsScreenName = "enrollment.enter_username_and_password"
}
