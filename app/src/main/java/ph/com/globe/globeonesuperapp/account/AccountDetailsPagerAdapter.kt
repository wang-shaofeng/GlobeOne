/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ph.com.globe.globeonesuperapp.account.calls.CallsUsageFragment
import ph.com.globe.globeonesuperapp.account.content.ContentSubscriptionsFragment
import ph.com.globe.globeonesuperapp.account.data.DataUsageFragment
import ph.com.globe.globeonesuperapp.account.text.TextUsageFragment

class AccountDetailsPagerAdapter(
    accountDetailsFragment: AccountDetailsFragment,
    private val isPostpaidMobile: Boolean
) :
    FragmentStateAdapter(accountDetailsFragment) {

    override fun getItemCount() = if (isPostpaidMobile) POSTPAID_ITEM_COUNT else PREPAID_ITEM_COUNT

    override fun createFragment(position: Int): Fragment {

        return when (position) {

            0 -> DataUsageFragment()

            1 -> ContentSubscriptionsFragment()

            2 -> CallsUsageFragment()

            3 -> TextUsageFragment()

            else -> throw IllegalStateException()
        }
    }
}

private const val POSTPAID_ITEM_COUNT = 3
private const val PREPAID_ITEM_COUNT = 4

const val TAB_POSITION_DATA = 0
const val TAB_POSITION_CONTENT = 1
const val TAB_POSITION_CALLS = 2
const val TAB_POSITION_TEXT = 3
