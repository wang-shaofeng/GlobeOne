/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.wificheck

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.domain.connectivity.ConnectivityDomainManager
import ph.com.globe.globeonesuperapp.databinding.AddAccountBroadbandWifiCheckFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.network.NetworkConnectionStatus
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountBroadbandWifiCheckFragment :
    NoBottomNavViewBindingFragment<AddAccountBroadbandWifiCheckFragmentBinding>(
        bindViewBy = {
            AddAccountBroadbandWifiCheckFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var connectivityDomainManager: ConnectivityDomainManager

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    private val addAccountBroadbandWifiCheckFragmentArgs by navArgs<AddAccountBroadbandWifiCheckFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {

            val hpwNumber = addAccountBroadbandWifiCheckFragmentArgs.msisdn

            wfWifiStatus.onBack {
                findNavController().navigateUp()
            }

            tvAddAccountViaOtp.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountBroadbandWifiCheckFragmentDirections.actionAddAccountBroadbandWifiCheckFragmentToAddAccountBroadbandNumberWithOtpFragment(
                        hpwNumber
                    )
                )
            }

            tvAddAccountViaSimSerial.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountBroadbandWifiCheckFragmentDirections.actionAddAccountBroadbandWifiCheckFragmentToAddAccountBroadbandNumberWithSimSerialFragment(
                        hpwNumber,
                        addAccountBroadbandWifiCheckFragmentArgs.brand,
                        addAccountBroadbandWifiCheckFragmentArgs.segment
                    )
                )
            }

            btnConnectWifi.setOnClickListener {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }

            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isWifiConnected()) {
            findNavController().safeNavigate(
                AddAccountBroadbandWifiCheckFragmentDirections.actionAddAccountBroadbandWifiCheckFragmentToAddAccountChooseModemFragment(
                    phoneNumber = addAccountBroadbandWifiCheckFragmentArgs.msisdn,
                    brand = addAccountBroadbandWifiCheckFragmentArgs.brand,
                    brandType = addAccountBroadbandWifiCheckFragmentArgs.brand.brandType,
                    segment = addAccountBroadbandWifiCheckFragmentArgs.segment
                )
            )
        }
    }

    private fun isWifiConnected(): Boolean {
        return connectivityDomainManager.getNetworkStatus() is NetworkConnectionStatus.ConnectedToWifi
    }

    override val analyticsScreenName: String = "enrollment.add_account_wifi_check"

    override val logTag = "AddAccountBroadbandWifiCheckFragment"

}
