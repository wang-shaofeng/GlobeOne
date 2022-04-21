/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ph.com.globe.globeonesuperapp.account.voucher_pocket.content.PromoVoucherFragment
import ph.com.globe.globeonesuperapp.account.voucher_pocket.rewards.RewardsVoucherFragment

class VoucherPocketPagerAdapter(
    voucherPocketFragment: VoucherPocketFragment,
) : FragmentStateAdapter(voucherPocketFragment) {

    override fun getItemCount() = VOUCHERS_ITEM_COUNT

    override fun createFragment(position: Int): Fragment {

        return when (position) {

            TAB_POSITION_REWARDS -> RewardsVoucherFragment()

            TAB_POSITION_CONTENT -> PromoVoucherFragment()

            else -> throw IllegalStateException()
        }
    }
}

private const val VOUCHERS_ITEM_COUNT = 2

const val TAB_POSITION_REWARDS = 0
const val TAB_POSITION_CONTENT = 1
