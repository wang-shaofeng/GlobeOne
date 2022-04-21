package ph.com.globe.globeonesuperapp.addaccount.broadband.otp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
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
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.CheckNumberResult
import ph.com.globe.globeonesuperapp.addaccount.broadband.AddAccountBroadbandNumberViewModel
import ph.com.globe.globeonesuperapp.addaccount.broadband.CheckBrandResult
import ph.com.globe.globeonesuperapp.addaccount.broadband.failurescreen.HpwBroadBandEnrollmentError
import ph.com.globe.globeonesuperapp.databinding.AddAccountBroadbandNumberWithOtpFragmentBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.model.util.brand.BROADBAND_SEGMENT
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountBroadbandNumberWithOtpFragment :
    NoBottomNavViewBindingFragment<AddAccountBroadbandNumberWithOtpFragmentBinding>(
        bindViewBy = {
            AddAccountBroadbandNumberWithOtpFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    private val addAccountBroadbandNumberWithOtpFragmentArgs by navArgs<AddAccountBroadbandNumberWithOtpFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()

        with(viewBinding) {

            wfAddAccount.onBack {
                findNavController().popBackStack(
                    R.id.addAccountBroadbandNumberWithSimSerialFragment,
                    false
                )
            }

            addAccountBroadbandNumberWithOtpFragmentArgs.hpwNumber?.let {
                etAddAccount.setText(it)
                btnNext.isEnabled = it.isNotBlank()
            }

            etAddAccount.addTextChangedListener {
                it.formatCountryCodeForBroadband()
                btnNext.isEnabled = !it.isNullOrBlank()
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

            tvFindWifiNumber.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountBroadbandNumberWithOtpFragmentDirections.actionAddAccountBroadbandNumberWithOtpFragmentToAddAccountFindModemNumberFragment()
                )
            }

            btnNext.setOnClickListener {
                val phoneNumber = etAddAccount.text.toString()
                logUiActionEvent(
                    target = "Add account",
                    additionalParams = mapOf(
                        "type" to BROADBAND_SEGMENT,
                        "number" to phoneNumber
                    )
                )
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, CLICKABLE_TEXT, ADD_ACCOUNT
                    )
                )

                addAccountMoreAccountsViewModel.checkNumber(
                    phoneNumber.convertToPrefixNumberFormat(),
                    AccountSegment.Broadband
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

            addAccountBroadbandNumberViewModel.checkBrandResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is CheckBrandResult.SuccessfulBrandCheck -> {
                            if (result.brandType == AccountBrandType.Prepaid) {
                                // if we are enrolling a prepaid account(HPW) we immediately try to send the OTP to the number
                                verifyOtpViewModel.addAccountSendOtp(
                                    msisdn = etAddAccount.text.toString(),
                                    segment = AccountSegment.Broadband,
                                    rawBrand = result.brand
                                )
                            } else {
                                // postpaid broadband is not allowed here this flow is only for HPW, to add postpaid broadband go to AddAccountBroadbandNumberFragment
                                requireContext().showError(
                                    tilAddAccount,
                                    etAddAccount,
                                    getString(R.string.not_valid_hpw_number)
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

                        is CheckBrandResult.NoLongerInSystemAccount ->{
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.no_longer_in_system)
                            )
                        }
                    }
                }
            })

            verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner) {
                it.handleEvent { result ->
                    when (result) {
                        is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                            findNavController().safeNavigate(
                                AddAccountBroadbandNumberWithOtpFragmentDirections.actionAddAccountBroadbandNumberWithOtpFragmentToAddAccountEnterOtpFragment(
                                    msisdn = result.msisdn,
                                    targetMobileNumber = result.msisdn,
                                    referenceId = result.referenceId,
                                    brand = addAccountBroadbandNumberViewModel.rawBrand.value!!,
                                    brandType = result.brandType,
                                    segment = AccountSegment.Broadband
                                )
                            )
                        }
                        else -> {
                            findNavController().safeNavigate(
                                AddAccountBroadbandNumberWithOtpFragmentDirections.actionAddAccountBroadbandNumberWithOtpFragmentToAddAccountEnrollBroadbandFailureFragment(
                                    HpwBroadBandEnrollmentError.SomethingWentWrong
                                )
                            )
                        }
                    }
                }
            }

            addAccountMoreAccountsViewModel.checkNumberResultBroadband.observe(viewLifecycleOwner, {
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
                                AccountSegment.Broadband
                            )
                        }

                        is CheckNumberResult.InvalidNumberFormat -> {
                            requireContext().showError(
                                tilAddAccount,
                                etAddAccount,
                                getString(R.string.not_valid_globe_number_broadband)
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

    override val logTag = "AddAccountBroadbandNumberWithOtpFragment"

    override val analyticsScreenName: String = "enrollment.add_account"
}
