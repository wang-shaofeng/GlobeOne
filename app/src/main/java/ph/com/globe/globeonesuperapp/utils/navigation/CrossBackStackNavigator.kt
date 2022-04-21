/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.navigation

import android.os.Bundle

/**
 * Navigator that handles 'crossover' navigation destinations.
 * 'Crossover' meaning that desired destination doesn't belong to the current vertical backstack,
 * so it has to slide down horizontally first, before navigating to the end destination.
 */
interface CrossBackstackNavigator {

    /**
     * Does 'crossover' navigation meaning that desired destination doesn't belong to the current vertical backstack,
     * so it has to slide down horizontally first, before navigating to the end destination.
     * @param shouldPopToStartDestinationFromCurrentGraph clears all fragments from current backstack
     * @param removeStartDestinationFromNextGraph removes the first fragment (app:startDestination) contained in destination graph.
     * [endDestinationId] corresponds to the id of the first fragment in stack after removal
     */
    fun crossNavigate(
        toBackStackKey: NavHostFragmentKey,
        endDestinationId: Int,
        args: Bundle? = null,
        shouldPopToStartDestinationFromCurrentGraph: Boolean = true,
        removeStartDestinationFromNextGraph: Boolean = false
    )

    /**
     * Does 'crossover' navigation meaning that desired destination doesn't belong to the current vertical backstack,
     * so it has to slide down horizontally first, before navigating to the end destination.
     * This method removes all history from backstack.
     * @param removeStartDestinationFromNextGraph removes the first fragment (app:startDestination) contained in destination graph.
     * [endDestinationId] corresponds to the id of the first fragment in stack after removal
     */
    fun crossNavigateWithoutHistory(
        toBackStackKey: NavHostFragmentKey,
        endDestinationId: Int,
        args: Bundle? = null,
        removeStartDestinationFromNextGraph: Boolean = false
    )

    fun navigateToPreviousBackstack(
        toBackStackKey: NavHostFragmentKey,
        returnToDestination: Int? = null
    )
}
