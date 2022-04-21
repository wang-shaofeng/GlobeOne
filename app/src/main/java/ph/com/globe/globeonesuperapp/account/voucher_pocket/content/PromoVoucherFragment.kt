/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VouchersViewModel
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.PromoVoucherFragmentBinding
import ph.com.globe.globeonesuperapp.utils.COPIED_VOUCHER_CODE
import ph.com.globe.globeonesuperapp.utils.copyToClipboard
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class PromoVoucherFragment : NoBottomNavViewBindingFragment<PromoVoucherFragmentBinding>({
    PromoVoucherFragmentBinding.inflate(it)
}) {

    private val vouchersViewModel: VouchersViewModel by hiltNavGraphViewModels(R.id.vouchers_subgraph)

    private val promoVoucherViewModel: PromoVoucherViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vouchersViewModel.selectedAccountLiveData.observe(viewLifecycleOwner) {
            promoVoucherViewModel.accountChanged(it)
        }

        with(viewBinding) {

            val promoVouchersViewAdapter = PromoVouchersRecyclerViewAdapter(
                revealVoucher = {
                    promoVoucherViewModel.revealVoucher(it)
                },
                copyVoucherNumber = {
                    requireContext().copyToClipboard(it, COPIED_VOUCHER_CODE)

                    val snackbarViewBinding =
                        GlobeSnackbarLayoutBinding
                            .inflate(LayoutInflater.from(requireContext()))
                    snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                    snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_code)

                    showSnackbar(snackbarViewBinding)
                },
                reachEnd = {
                    promoVoucherViewModel.loadMore()
                },
                somethingWentWrongOnClick = {
                    promoVoucherViewModel.reload()
                },
                backToTopOnCLick = {
                    rvVouchers.scrollToPosition(0)
                }
            )
            rvVouchers.adapter = promoVouchersViewAdapter

            promoVoucherViewModel.vouchers.observe(viewLifecycleOwner) {
                promoVouchersViewAdapter.submitList(it)
            }
        }
    }

    override val logTag: String = "PromoVoucherFragment"

}
