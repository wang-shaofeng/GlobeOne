/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ph.com.globe.globeonesuperapp.shop.borrow.ShopBorrowFragment
import ph.com.globe.globeonesuperapp.shop.content.ShopContentFragment
import ph.com.globe.globeonesuperapp.shop.load.ShopLoadFragmentFull
import ph.com.globe.globeonesuperapp.shop.load.ShopLoadFragmentInitial
import ph.com.globe.globeonesuperapp.shop.promo.ShopPromoFragment

class ShopPagerAdapter(shopFragment: ShopFragment) :
    FragmentStateAdapter(shopFragment) {

    private var currentLoadFragment = LOAD_INITIAL_ID

    override fun getItemCount() = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {

        return when (position) {

            PROMO_ID -> ShopPromoFragment()

            LOAD_ID -> when (currentLoadFragment) {

                LOAD_INITIAL_ID -> ShopLoadFragmentInitial()

                LOAD_FULL_ID -> ShopLoadFragmentFull()

                else -> throw IllegalStateException()
            }

            BORROW_ID -> ShopBorrowFragment()

            CONTENT_ID -> ShopContentFragment()

            else -> throw IllegalStateException()
        }
    }

    override fun getItemId(position: Int): Long {
        return when (position) {
            LOAD_ID -> when (currentLoadFragment) {
                LOAD_INITIAL_ID -> LOAD_INITIAL_ID
                LOAD_FULL_ID -> LOAD_FULL_ID
                else -> throw IllegalStateException()
            }
            else -> position.toLong()
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        return when (itemId) {
            LOAD_INITIAL_ID, LOAD_FULL_ID, PROMO_ID.toLong(), BORROW_ID.toLong(), CONTENT_ID.toLong() -> true
            else -> false
        }
    }

    fun selectLoadFragment(fragmentId: Long) {
        currentLoadFragment = fragmentId
        notifyItemChanged(LOAD_ID)
    }

    companion object {
        private const val PAGE_COUNT = 4
        const val PROMO_ID = 0
        const val LOAD_ID = 1
        const val LOAD_INITIAL_ID = 10L
        const val LOAD_FULL_ID = 11L
        const val BORROW_ID = 2
        const val CONTENT_ID = 3
    }
}
