package ph.com.globe.globeonesuperapp.addaccount.broadband.simserial

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.ADD_ACCOUNT_SCREEN
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.I_WILL_DO_IT_LATER
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.data.network.util.VERIFICATION_TYPE_SIM_SERIAL
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.broadband.AddAccountBroadbandNumberViewModel
import ph.com.globe.globeonesuperapp.addaccount.broadband.failurescreen.HpwBroadBandEnrollmentError
import ph.com.globe.globeonesuperapp.addaccount.confirmaccount.ConfirmAccountArgs
import ph.com.globe.globeonesuperapp.databinding.AddAccountBroadbandNumberWithSimSerialFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.closeKeyboard
import ph.com.globe.globeonesuperapp.utils.hideError
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.util.brand.AccountBrandType
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountBroadbandNumberWithSimSerialFragment :
    NoBottomNavViewBindingFragment<AddAccountBroadbandNumberWithSimSerialFragmentBinding>(
        bindViewBy = {
            AddAccountBroadbandNumberWithSimSerialFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    private val addAccountBroadbandNumberWithSimSerialFragmentArgs by navArgs<AddAccountBroadbandNumberWithSimSerialFragmentArgs>()

    private val addAccountBroadbandNumberWithSimSerialViewModel: AddAccountBroadbandNumberWithSimSerialViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()

        val hpwNumber = addAccountBroadbandNumberWithSimSerialFragmentArgs.msisdn

        with(viewBinding) {
            ivClose.setOnClickListener {
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

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            tvHpwAccountNumber.text = hpwNumber

            etSimSerial.addTextChangedListener {
                btnNext.isEnabled = !it.isNullOrBlank()
                requireContext().hideError(tilSimSerial, etSimSerial)
            }

            etSimSerial.setOnEditorActionListener { v, _, _ ->
                closeKeyboard(v, requireContext())
                true
            }

            tvFindSimSerial.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountBroadbandNumberWithSimSerialFragmentDirections.actionAddAccountBroadbandNumberWithSimSerialFragmentToAddAccountFindSimSerialFragment(
                        hpwNumber
                    )
                )
            }

            tvAddAccountViaOtp.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountBroadbandNumberWithSimSerialFragmentDirections.actionAddAccountBroadbandNumberWithSimSerialFragmentToAddAccountBroadbandNumberWithOtpFragment(
                        hpwNumber
                    )
                )
            }

            btnNext.setOnClickListener {
                val simSerial = etSimSerial.text.toString()
                addAccountBroadbandNumberWithSimSerialViewModel.validateSimSerial(
                    msisdn = hpwNumber,
                    simSerial = simSerial
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

            addAccountBroadbandNumberWithSimSerialViewModel.validateSimSerialLivaData.oneTimeEventObserve(
                viewLifecycleOwner
            ) { result ->
                when (result) {
                    is AddAccountBroadbandNumberWithSimSerialViewModel.ValidateSimSerialResult.ValidatedSimSerialSuccess -> {
                        findNavController().safeNavigate(
                            AddAccountBroadbandNumberWithSimSerialFragmentDirections.actionAddAccountBroadbandNumberWithSimSerialFragmentToAddAccountConfirmFragment(
                                ConfirmAccountArgs(
                                    mobileNumber = hpwNumber,
                                    brand = addAccountBroadbandNumberWithSimSerialFragmentArgs.brand,
                                    brandType = AccountBrandType.Prepaid,
                                    segment = addAccountBroadbandNumberWithSimSerialFragmentArgs.segment,
                                    referenceId = result.simReferenceId,
                                    verificationType = VERIFICATION_TYPE_SIM_SERIAL
                                )
                            )
                        )
                    }
                    is AddAccountBroadbandNumberWithSimSerialViewModel.ValidateSimSerialResult.NotAValidBroadbandSimSerialPairing -> {
                        findNavController().safeNavigate(
                            AddAccountBroadbandNumberWithSimSerialFragmentDirections.actionAddAccountBroadbandNumberWithSimSerialFragmentToAddAccountEnrollBroadbandFailureFragment(
                                errorType = HpwBroadBandEnrollmentError.HpwSimSerialPairingMismatch,
                                hpwNumber = hpwNumber
                            )
                        )
                    }
                    else -> {
                        findNavController().safeNavigate(
                            AddAccountBroadbandNumberWithSimSerialFragmentDirections.actionAddAccountBroadbandNumberWithSimSerialFragmentToAddAccountEnrollBroadbandFailureFragment(
                                errorType = HpwBroadBandEnrollmentError.SomethingWentWrong,
                                hpwNumber = hpwNumber
                            )
                        )
                    }
                }
            }
        }
    }

    override val logTag = "AddAccountBroadbandNumberWithSimSerialFragment"

    override val analyticsScreenName: String = "enrollment.add_account"
}
