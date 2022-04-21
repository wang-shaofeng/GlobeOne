/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.broadband.failurescreen.HpwBroadBandEnrollmentError
import ph.com.globe.globeonesuperapp.databinding.AddAccountChooseModemFragmentBinding
import ph.com.globe.globeonesuperapp.utils.OTP_KEY_SET_ENROLL_ACCOUNT
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.util.HUAWEI_2CA
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountChooseModemFragment :
    NoBottomNavViewBindingFragment<AddAccountChooseModemFragmentBinding>({
        AddAccountChooseModemFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val addAccountChooseModemViewModel: AddAccountChooseModemViewModel by viewModels()

    private val addAccountChooseModemFragmentArgs by navArgs<AddAccountChooseModemFragmentArgs>()

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {
            val addAccountModemRecyclerViewAdapter = AddAccountModemRecyclerViewAdapter {
                addAccountChooseModemViewModel.chooseModem(it)
            }
            rvModems.adapter = addAccountModemRecyclerViewAdapter
            rvModems.itemAnimator = null

            btnCancel.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        MODEM_PRODUCTS_SCREEN, BUTTON, CANCEL
                    )
                )
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            ivBack.setOnClickListener {
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            ivClose.setOnClickListener {
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
                }
            })

            btnNext.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        MODEM_PRODUCTS_SCREEN, BUTTON, NEXT,
                        productName = addAccountChooseModemViewModel.selectedModem?.name
                    )
                )
                //Handling for Sprint19 HPW Project Hack supported modem Huawei2CA
                addAccountChooseModemViewModel.selectedModem?.let {
                    if(it.name == HUAWEI_2CA) {
                        addAccountChooseModemViewModel.pingModem()
                    } else {
                        with(addAccountChooseModemFragmentArgs) {
                            findNavController().safeNavigate(
                                AddAccountChooseModemFragmentDirections.actionAddAccountChooseModemFragmentToAddAccountBroadbandNumberWithSimSerialFragment(
                                    msisdn = phoneNumber,
                                    brand = brand,
                                    segment = segment
                                )
                            )
                        }
                    }
                }
            }

            addAccountChooseModemViewModel.modemsLiveData.observe(viewLifecycleOwner) {
                addAccountModemRecyclerViewAdapter.submitList(it)
            }

            addAccountChooseModemViewModel.enableButton.observe(viewLifecycleOwner) {
                it.handleEvent { enable ->
                    btnNext.isEnabled = enable
                }
            }

            addAccountChooseModemViewModel.pingableModem.observe(viewLifecycleOwner) {
                it.handleEvent { pingable ->
                    if (pingable) {
                        verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner) { event ->
                            event.handleEvent { result ->
                                when (result) {
                                    is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                                        addAccountChooseModemViewModel.selectedModem?.let {
                                            with(addAccountChooseModemFragmentArgs) {
                                                findNavController().safeNavigate(
                                                    AddAccountChooseModemFragmentDirections.actionAddAccountChooseModemFragmentToAddAccountEnterUsernamePasswordFragment(
                                                        it,
                                                        phoneNumber,
                                                        result.referenceId,
                                                        brand,
                                                        brandType,
                                                        segment
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        findNavController().safeNavigate(
                                            AddAccountChooseModemFragmentDirections.actionAddAccountChooseModemFragmentToAddAccountEnrollBroadbandFailureFragment(
                                                HpwBroadBandEnrollmentError.SomethingWentWrong
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        //execute send otp
                        verifyOtpViewModel.sendOtp(
                            addAccountChooseModemFragmentArgs.phoneNumber,
                            OTP_KEY_SET_ENROLL_ACCOUNT
                        )
                    } else {
                        findNavController().safeNavigate(
                            AddAccountChooseModemFragmentDirections.actionAddAccountChooseModemFragmentToAddAccountEnrollBroadbandFailureFragment(
                                errorType = HpwBroadBandEnrollmentError.NotPingable,
                                hpwNumber = addAccountChooseModemFragmentArgs.phoneNumber
                            )
                        )
                    }
                }
            }
        }
    }

    override val logTag = "AddAccountChooseModemFragment"

    override val analyticsScreenName = "modem_products"
}
