package ph.com.globe.globeonesuperapp.account_activities

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ph.com.globe.globeonesuperapp.account_activities.bill_statements.AccountBillStatementsFragment
import ph.com.globe.globeonesuperapp.account_activities.payments.BillPaymentsFragment
import ph.com.globe.globeonesuperapp.account_activities.rewards.AccountRewardsFragment

class AccountPagerAdapter(
    fragment: AccountActivitiesFragment,
    isPrepaid: Boolean,
    isMobile: Boolean
) : FragmentStateAdapter(fragment) {

    private val screenTabs =
        when {
            //02/04/2022 - Temporarily hide Prepaid Ledger tab until further notice
            //isPrepaid -> listOf(PrepaidLedgerFragment(), AccountRewardsFragment())
            isPrepaid -> listOf(AccountRewardsFragment())
            // only for postpaid mobile
            isMobile -> listOf(
                AccountBillStatementsFragment(),
                BillPaymentsFragment(),
                AccountRewardsFragment()
            )
            else -> listOf(AccountRewardsFragment())
        }

    override fun getItemCount() = screenTabs.size

    override fun createFragment(position: Int): Fragment = screenTabs[position]
}
