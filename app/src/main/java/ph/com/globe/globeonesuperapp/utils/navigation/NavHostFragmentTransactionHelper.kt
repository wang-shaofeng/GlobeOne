/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.navigation

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * Helper for [NavHostFragmentScreenChanger] that always uses [commitNow][FragmentTransaction.commitNow]
 * for commiting [FragmentTransaction]s.
 */
internal class NavHostFragmentTransactionHelper(
    defaultTransactionOptions: DefaultTransactionOptions<FragmentTransaction>,
    private val fragmentManager: FragmentManager,
    runnable: Runnable
) : TransactionHelper<FragmentTransaction>(runnable, defaultTransactionOptions) {

    @SuppressLint("CommitTransaction")
    override fun startTransaction(): FragmentTransaction {
        return fragmentManager.beginTransaction()
    }

    override fun shouldCommit(transaction: FragmentTransaction): Boolean {
        return !transaction.isEmpty
    }

    override fun setOnCommitActionToTransaction(
        transaction: FragmentTransaction,
        onCommitAction: Runnable
    ) {
        transaction.runOnCommit(onCommitAction)
    }

    override fun doCommit(tr: FragmentTransaction) {
        tr.commit()
    }
}
