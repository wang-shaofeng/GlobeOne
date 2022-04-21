/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket.rewards

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VoucherPocketFragmentDirections
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VouchersViewModel
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.RewardsVoucherFragmentBinding
import ph.com.globe.globeonesuperapp.utils.COPIED_VOUCHER_CODE
import ph.com.globe.globeonesuperapp.utils.copyToClipboard
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class RewardsVoucherFragment : NoBottomNavViewBindingFragment<RewardsVoucherFragmentBinding>({
    RewardsVoucherFragmentBinding.inflate(it)
}), AnalyticsScreen {

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val vouchersViewModel: VouchersViewModel by hiltNavGraphViewModels(R.id.vouchers_subgraph)

    private val rewardsVoucherViewModel: RewardsVoucherViewModel by viewModels()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vouchersViewModel.selectedAccountLiveData.observe(viewLifecycleOwner, {
            rewardsVoucherViewModel.accountChanged(it)
        })

        with(viewBinding) {

            val rewardsVouchersViewAdapter = RewardsVouchersRecyclerViewAdapter(
                revealVoucher = {
                    rewardsVoucherViewModel.revealVoucher(it)
                },
                copyVoucherNumber = {
                    logUiActionEvent("Voucher code copy option")
                    requireContext().copyToClipboard(it, COPIED_VOUCHER_CODE)

                    val snackbarViewBinding =
                        GlobeSnackbarLayoutBinding
                            .inflate(LayoutInflater.from(requireContext()))
                    snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                    snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_code)

                    showSnackbar(snackbarViewBinding)
                },
                openLink = {
                    generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        startActivity(intent)
                    }
                },
                reachEnd = {
                    rewardsVoucherViewModel.loadMore()
                },
                somethingWentWrongOnClick = {
                    rewardsVoucherViewModel.reload()
                },
                redeemRewards = {
                    findNavController().safeNavigate(VoucherPocketFragmentDirections.actionVoucherPocketFragmentToRewardsSubgraph())
                },
                backToTopOnCLick = {
                    rvVouchers.scrollToPosition(0)
                }
            )
            rvVouchers.adapter = rewardsVouchersViewAdapter

            rewardsVoucherViewModel.vouchers.observe(viewLifecycleOwner, {
                rewardsVouchersViewAdapter.submitList(it)
            })
        }
    }

    override val logTag: String = "RewardsVoucherFragment"

    override val analyticsScreenName: String = "vouchers.voucher_pocket_screen"
}
