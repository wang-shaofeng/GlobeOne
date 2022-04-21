/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.VoucherPocketFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class VoucherPocketFragment : NoBottomNavViewBindingFragment<VoucherPocketFragmentBinding>({
    VoucherPocketFragmentBinding.inflate(it)
}), AnalyticsScreen {
    private val vouchersViewModel: VouchersViewModel by hiltNavGraphViewModels(R.id.vouchers_subgraph)

    private val voucherPocketFragmentArgs by navArgs<VoucherPocketFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // must be set again, because we can navigate to and back from selectAccount fragment which makes it dark
        setLightStatusBar()

        // show content page firstly
        val isShowContentTab = voucherPocketFragmentArgs.isShowContentTab
        vouchersViewModel.setEnrolledAccount(
            voucherPocketFragmentArgs.enrolledAccount,
        )

        with(viewBinding) {

            vouchersViewModel.selectedAccountLiveData.observe(viewLifecycleOwner) { account ->
                with(account) {
                    tvAccountName.text = accountName
                    tvAccountNumber.text = msisdn
                }
            }

            wfVoucherPocket.onBack {
                findNavController().navigateUp()
            }

            ivEdit.setOnClickListener {
                findNavController().safeNavigate(VoucherPocketFragmentDirections.actionVoucherPocketFragmentToSelectAccountVouchersFragment())
            }

            vpVouchers.adapter = VoucherPocketPagerAdapter(this@VoucherPocketFragment)

            TabLayoutMediator(tlVouchers, vpVouchers) { tab, position ->
                tab.text = when (position) {

                    TAB_POSITION_REWARDS -> resources.getString(R.string.promo_voucher_rewards)

                    TAB_POSITION_CONTENT -> resources.getString(R.string.promo_voucher_content)

                    else -> throw IllegalStateException()
                }
            }.attach()

            if (isShowContentTab) {
                vpVouchers.setCurrentItem(TAB_POSITION_CONTENT, false)
            }
        }
    }

    override val logTag = "VoucherPocketFragment"

    override val analyticsScreenName: String = "vouchers.voucher_pocket_screen"
}

const val ENROLLED_ACCOUNT = "enrolledAccount"
const val IS_SHOW_CONTENT = "isShowContentTab"
