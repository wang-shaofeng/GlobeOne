/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FreebiesPagerAdapter(
    fm: Fragment,
    private val showVoucher: Boolean,
) :
    FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return if (showVoucher) 2 else 1
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TAB_FREEBIE_ENTERTAINMENT -> FreebiesEntertainmentFragment()
            TAB_FREEBIE_HEALTH_AND_SAVING -> FreebiesHealthFragment()
            else -> throw IllegalStateException()
        }
    }

    companion object {
        const val TAB_FREEBIE_ENTERTAINMENT = 0
        const val TAB_FREEBIE_HEALTH_AND_SAVING = 1
    }
}
