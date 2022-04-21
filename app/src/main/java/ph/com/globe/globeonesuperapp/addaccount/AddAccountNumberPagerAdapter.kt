/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ph.com.globe.globeonesuperapp.addaccount.broadband.AddAccountBroadbandNumberFragment
import ph.com.globe.globeonesuperapp.addaccount.mobile.AddAccountMobileNumberFragment

class AddAccountNumberPagerAdapter(addAccountNumberFragment: AddAccountNumberFragment) :
    FragmentStateAdapter(addAccountNumberFragment) {

    override fun getItemCount() = PAGE_NUM

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> AddAccountBroadbandNumberFragment()

            1 -> AddAccountMobileNumberFragment()

            else -> throw IllegalStateException()
        }
    }
}

private const val PAGE_NUM = 2
