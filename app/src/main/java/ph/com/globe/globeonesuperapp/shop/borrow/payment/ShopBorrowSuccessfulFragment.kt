/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.borrow.payment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopBorrowSuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.payment.intToPesos
import ph.com.globe.globeonesuperapp.utils.payment.setValidityText
import ph.com.globe.globeonesuperapp.utils.payment.setValidityTextEx
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.formattedForPhilippines
import javax.inject.Inject

@AndroidEntryPoint
class ShopBorrowSuccessfulFragment :
    NoBottomNavViewBindingFragment<ShopBorrowSuccessfulFragmentBinding>(
        bindViewBy = {
            ShopBorrowSuccessfulFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    private val shopBorrowSuccessfulFragmentArgs by navArgs<ShopBorrowSuccessfulFragmentArgs>()

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher = registerActivityResultForStoragePermission {
            takeScreenshotFlow(viewBinding)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            with(shopBorrowSuccessfulFragmentArgs) {
                tvSentToValue.text = sentTo.formattedForPhilippines().formatPhoneNumber()
                tvLoanNameValue.text = loanName
                tvValidityValue.text = resources.setValidityTextEx(validity)
                tvAmountValue.text = amount.stringToPesos()
                tvServiceFeeValue.text = serviceFee.stringToPesos()
                tvTotalAmount.text = (amount.toInt() + serviceFee.toInt()).intToPesos()
            }

            ivDownloadReceipt.setOnClickListener {

                logUiActionEvent("Download receipt option")
                if (requestStorageActivityLauncher != null) {
                    requestStoragePermissionsIfNeededAndPerformSuccessAction(
                        requestStorageActivityLauncher!!
                    )
                } else {
                    takeScreenshotFlow(viewBinding)
                }
            }

            btnDone.setOnClickListener {
                if (shopViewModel.isLoggedIn()) {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
                } else {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.AUTH_KEY,
                        R.id.selectSignMethodFragment
                    )
                }
            }

            // Adding a callback on back pressed to replace the standard up navigation with popBackStack
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        findNavController().popBackStack(R.id.shopFragment, false)
                    }
                }
            )
        }
    }

    override val logTag = "ShopBorrowSuccessfulFragment"
    override val analyticsScreenName: String= "borrow.success"
}
