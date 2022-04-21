/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.DONE
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.REDEEM_REWARDS_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.PosRedeemPointsSuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.COPIED_ORDER_NUMBER
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.copyToClipboard
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.takeScreenshotFlow
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class POSRedeemPointsSuccessfulFragment :
    NoBottomNavViewBindingFragment<PosRedeemPointsSuccessfulFragmentBinding>({
        PosRedeemPointsSuccessfulFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val posViewModel by navGraphViewModels<POSViewModel>(R.id.pos_subgraph) { defaultViewModelProviderFactory }

    private val args by navArgs<POSRedeemPointsSuccessfulFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher = registerActivityResultForStoragePermission {
            takeScreenshotFlow(viewBinding)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}

        with(viewBinding) {

            btnDone.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        REDEEM_REWARDS_SCREEN, BUTTON, DONE
                    )
                )
                findNavController().popBackStack(R.id.payWithPointsFragment, true)
            }

            ivCopyToClipboard.setOnClickListener {
                requireContext().copyToClipboard(tvReferenceNumberValue.text.toString(), COPIED_ORDER_NUMBER)

                val snackbarViewBinding =
                    GlobeSnackbarLayoutBinding
                        .inflate(LayoutInflater.from(requireContext()))
                snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_order_number)

                showSnackbar(snackbarViewBinding)
            }

            ivDownloadReward.setOnClickListener {

                logUiActionEvent("Download receipt option")
                if (requestStorageActivityLauncher != null) {
                    requestStoragePermissionsIfNeededAndPerformSuccessAction(
                        requestStorageActivityLauncher!!
                    )
                } else {
                    takeScreenshotFlow(viewBinding)
                }
            }

            with(posViewModel) {
                tvAccountName.text = (chosenAccount
                    ?: selectedAccount?.enrolledAccountWithPoints)?.enrolledAccount?.accountAlias
                tvMoreInfoName.text = merchantDetails?.merchantName
                tvTotalPts.text = getString(R.string.pts_placeholder, totalPoints.toString())

                val remaining =
                    ((chosenAccount ?: selectedAccount?.enrolledAccountWithPoints)?.points
                        ?: 0f) - totalPoints
                val remainingFormatted = String.format("%.02f", remaining)
                tvRemainingPts.text = getString(R.string.pts_placeholder, remainingFormatted)
            }

            tvReferenceNumberValue.text = args.transactionId
        }
    }

    override val logTag: String = "POSRedeemPointsSuccessfulFragment"
    override val analyticsScreenName: String = "rewards.pos.success"
}
