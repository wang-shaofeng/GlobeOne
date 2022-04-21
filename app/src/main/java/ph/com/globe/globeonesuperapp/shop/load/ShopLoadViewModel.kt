/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.load

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ph.com.globe.globeonesuperapp.utils.BaseViewModel

class ShopLoadViewModel : BaseViewModel() {

    private var _walletType = MutableLiveData<Wallet>(Wallet.Personal)
    val walletType: LiveData<Wallet> = _walletType

    var amount = 0

    var amountLimits = Pair(20, 1500)

    override val logTag = "ShopLoadViewModel"

    fun selectWallet(type: Wallet){
        _walletType.value = type
    }

    sealed class Wallet {
        object Retailer : Wallet()
        object Personal : Wallet()
    }
}
