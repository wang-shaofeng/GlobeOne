/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.NavHostFragment
import ph.com.globe.globeonesuperapp.utils.navigation.backstack.BackStack
import ph.com.globe.globeonesuperapp.utils.navigation.history.History

class NavHostFragmentScreenChanger(
    private val fragmentManager: FragmentManager,
    @param:IdRes private val rootId: Int,
    runnable: Runnable
) : AbstractScreenChanger<NavHostFragmentKey, NavHostFragment>() {

    private val transactionHelper =
        NavHostFragmentTransactionHelper(OPTIONS, fragmentManager, runnable)

    override fun getObjectForKey(key: NavHostFragmentKey): NavHostFragment? =
        fragmentManager.findFragmentByTag(key.fragmentTag) as? NavHostFragment

    override fun waitingForCommit(): Boolean {
        return transactionHelper.isWaitingForCommitToFinish
    }

    override fun onSaveInstanceState(
        history: History<NavHostFragmentKey>,
        outState: Bundle
    ) {
        // no-op - state is saved by fragment manager
    }

    override fun onReInit(
        history: History<NavHostFragmentKey>,
        savedState: Bundle
    ) {
        // no op - state is restored by fragment manager
    }

    override fun detach(screen: NavHostFragment) {
        transactionHelper.doInTransaction { ft -> ft.detach(screen) }
    }

    override fun attach(fragment: NavHostFragment) {
        transactionHelper.doInTransaction { ft -> ft.attach(fragment) }
    }

    override fun addObject(
        fragment: NavHostFragment,
        key: NavHostFragmentKey
    ) {
        transactionHelper.doInTransaction { ft -> ft.add(rootId, fragment, key.fragmentTag) }
    }

    override fun removeObject(
        fragment: NavHostFragment,
        key: NavHostFragmentKey
    ) {
        transactionHelper.doInTransaction { ft -> ft.remove(fragment) }
    }

    override fun instantiateObjectFromKey(key: NavHostFragmentKey): NavHostFragment =
        NavHostFragment.create(key.graphId)

    override fun navigate(
        previous: History<NavHostFragmentKey>,
        newState: History<NavHostFragmentKey>,
        direction: ScreenChanger.NavDirection,
        animOptions: BackStack.AnimOptions
    ) {
        transactionHelper.doInTransaction {
            super.navigate(
                previous,
                newState,
                direction,
                animOptions
            )
        }
    }

    override fun onInit(history: History<NavHostFragmentKey>) {
        transactionHelper.doInTransaction { super.onInit(history) }
    }

    companion object {
        /**
         * Disallows [FragmentManager] back stack, because we are using attach/detach to create our own stack.
         * Allows reordering so few subsequent transactions that cancel each other could be optimized
         * Apply these options to all transactions.
         */
        private val OPTIONS =
            TransactionHelper.DefaultTransactionOptions<FragmentTransaction> { ft ->
                ft.setReorderingAllowed(true)
                ft.disallowAddToBackStack()
            }

    }
}

