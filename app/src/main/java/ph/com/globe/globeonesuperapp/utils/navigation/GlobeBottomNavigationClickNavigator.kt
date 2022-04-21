/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.navigation

import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import ph.com.globe.globeonesuperapp.utils.navigation.backstack.BackStack
import ph.com.globe.globeonesuperapp.utils.navigation.history.History
import ph.com.globe.globeonesuperapp.utils.navigation.keys.ScreenKey

class GlobeBottomNavigationClickNavigator<T : ScreenKey> private constructor(
    private val bottomNavigationView: BottomNavigationView,
    private val backStack: BackStack<T, *>,
    private val itemIdToAssociatedKey: SparseArrayCompat<T>,
    private val beforeNavigatingOnItemSelectedCallback: ((Int) -> Unit)? = null,
    private val afterNavigatingOnItemSelectedCallback: ((Int) -> Unit)? = null,
    private val navigationItemReselectedCallback: ((Int) -> Unit)? = null,
    private val activityLifecycle: Lifecycle
) : BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener, BackStack.NavigationListener<T> {

    private var selectedItemId: Int
        get() = bottomNavigationView.selectedItemId
        set(value) {
            bottomNavigationView.selectedItemId = value
        }

    init {
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.setOnNavigationItemReselectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (activityLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            if (backStack.isDispatchingTo(this)) {
                return true
            }

            val key = itemIdToAssociatedKey.get(item.itemId)
            if (key != null) {
                beforeNavigatingOnItemSelectedCallback?.invoke(selectedItemId)
                backStack.goTo(key)
                afterNavigatingOnItemSelectedCallback?.invoke(selectedItemId)
                return true
            }
        }

        return false
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        navigationItemReselectedCallback?.invoke(item.itemId) ?: run {
            if (backStack.isDispatchingTo(this)) {
                return
            }

            val replaceTop = BackStack.NavOptions(true)
            val key = itemIdToAssociatedKey.get(item.itemId)

            if (key != null) {
                backStack.goTo(key, replaceTop)
            }
        }
    }

    override fun onNavigated(previousState: History<T>?, history: History<T>) {
        if (!history.isEmpty) {
            val topKey = history.peek()
            val id = findIdFromKey(topKey)

            if (selectedItemId != id) {
                selectedItemId = id
            }
        }
    }

    private fun findIdFromKey(topKey: T?): Int {
        for (i in 0 until itemIdToAssociatedKey.size()) {
            val key = itemIdToAssociatedKey.valueAt(i)
            if (key == topKey) {
                return itemIdToAssociatedKey.keyAt(i)
            }
        }
        return 0
    }

    class Builder<T : ScreenKey>(
        private val backStack: BackStack<T, *>,
        private val bottomNavigationView: BottomNavigationView,
        private val activityLifecycle: Lifecycle,
        private val beforeNavigatingOnItemSelectedCallback: ((Int) -> Unit)? = null,
        private val afterNavigatingOnItemSelectedCallback: ((Int) -> Unit)? = null,
        private val navigationItemReselectedCallback: ((Int) -> Unit)? = null
    ) {
        private val itemIdToAssociatedKey = SparseArrayCompat<T>()

        /**
         * Maps item id as specified in menu resource file to key that is associated with that
         * item id
         */
        fun mapItemIdToKey(@IdRes itemId: Int, key: T): Builder<T> {
            itemIdToAssociatedKey.put(itemId, key)
            return this
        }

        fun build(): GlobeBottomNavigationClickNavigator<T> {
            return GlobeBottomNavigationClickNavigator(
                bottomNavigationView,
                backStack,
                itemIdToAssociatedKey,
                beforeNavigatingOnItemSelectedCallback,
                afterNavigatingOnItemSelectedCallback,
                navigationItemReselectedCallback,
                activityLifecycle
            )
        }
    }
}
