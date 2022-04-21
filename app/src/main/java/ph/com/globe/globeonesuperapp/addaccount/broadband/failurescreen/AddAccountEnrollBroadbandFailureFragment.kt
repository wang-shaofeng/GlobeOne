/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.failurescreen

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.broadband.AddAccountBroadbandNumberViewModel
import ph.com.globe.globeonesuperapp.databinding.AddAccountEnrollBroadbandFailureFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountSegment

@AndroidEntryPoint
class AddAccountEnrollBroadbandFailureFragment :
    NoBottomNavViewBindingFragment<AddAccountEnrollBroadbandFailureFragmentBinding>({
        AddAccountEnrollBroadbandFailureFragmentBinding.inflate(it)
    }) {

    private val addAccountEnrollBroadbandFailureFragmentArgs by navArgs<AddAccountEnrollBroadbandFailureFragmentArgs>()

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            when (addAccountEnrollBroadbandFailureFragmentArgs.errorType) {
                HpwBroadBandEnrollmentError.NotPingable -> {
                    tvErrorTitle.text = getString(R.string.connect_to_your_HPW_modem)
                    tvErrorDescription.text = getString(R.string.check_wifi_settings)

                    btnTryAgain.setOnClickListener {
                        findNavController().popBackStack(R.id.addAccountNumberFragment, false)
                    }

                    btnTryOtherOption.text = getString(R.string.try_adding_manually)
                    btnTryOtherOption.setOnClickListener {
                        findNavController().safeNavigate(
                            AddAccountEnrollBroadbandFailureFragmentDirections.actionAddAccountEnrollBroadbandFailureFragmentToAddAccountBroadbandNumberWithOtpFragment(
                                hpwNumber = addAccountEnrollBroadbandFailureFragmentArgs.hpwNumber
                            )
                        )
                    }
                }

                HpwBroadBandEnrollmentError.BadModemInfo -> {
                    tvErrorTitle.text = getString(R.string.modem_info_incorrect_credentials)
                    tvErrorDescription.text = getString(R.string.double_check_modem_credentials)

                    btnTryAgain.setOnClickListener {
                        findNavController().navigateUp()
                    }

                    btnTryOtherOption.text = getString(R.string.try_adding_manually)
                    btnTryOtherOption.setOnClickListener {
                        with(addAccountEnrollBroadbandFailureFragmentArgs) {
                            findNavController().safeNavigate(
                                AddAccountEnrollBroadbandFailureFragmentDirections.actionAddAccountEnrollBroadbandFailureFragmentToAddAccountBroadbandNumberWithOtpFragment(
                                    hpwNumber = hpwNumber
                                )
                            )
                        }
                    }
                }

                HpwBroadBandEnrollmentError.HpwSimSerialPairingMismatch -> {
                    tvErrorTitle.text = getString(R.string.broadband_sim_serial_mismatch)
                    tvErrorDescription.text = getString(R.string.double_check_broadband_sim_serial)

                    btnTryAgain.setOnClickListener {
                        findNavController().popBackStack(R.id.addAccountNumberFragment, false)
                    }

                    btnTryOtherOption.text = getString(R.string.try_adding_manually)
                    btnTryOtherOption.setOnClickListener {
                        findNavController().safeNavigate(
                            AddAccountEnrollBroadbandFailureFragmentDirections.actionAddAccountEnrollBroadbandFailureFragmentToAddAccountBroadbandNumberWithOtpFragment(
                                hpwNumber = addAccountEnrollBroadbandFailureFragmentArgs.hpwNumber
                            )
                        )
                    }
                }

                else -> {
                    tvErrorTitle.text = getString(R.string.sorry_about_that)
                    tvErrorDescription.text = getString(R.string.something_went_wrong)

                    btnTryAgain.setOnClickListener {
                        findNavController().popBackStack(R.id.addAccountNumberFragment, false)
                    }

                    btnTryOtherOption.text = getString(R.string.try_adding_manually)
                    btnTryOtherOption.setOnClickListener {
                        addAccountBroadbandNumberViewModel.updateAddBroadbandManuallyValue(true)
                        findNavController().safeNavigate(
                            AddAccountEnrollBroadbandFailureFragmentDirections.actionAddAccountEnrollBroadbandFailureFragmentToAddAccountBroadbandNumberWithOtpFragment(
                                hpwNumber = addAccountEnrollBroadbandFailureFragmentArgs.hpwNumber
                            )
                        )
                    }
                }
            }
        }
    }

    override val logTag = "AddAccountEnrollBroadbandFailureFragment"
}

sealed class HpwBroadBandEnrollmentError : Parcelable {

    @Parcelize
    object NotPingable : HpwBroadBandEnrollmentError()

    @Parcelize
    object BadModemInfo : HpwBroadBandEnrollmentError()

    @Parcelize
    object SomethingWentWrong : HpwBroadBandEnrollmentError()

    @Parcelize
    object HpwSimSerialPairingMismatch : HpwBroadBandEnrollmentError()
}
