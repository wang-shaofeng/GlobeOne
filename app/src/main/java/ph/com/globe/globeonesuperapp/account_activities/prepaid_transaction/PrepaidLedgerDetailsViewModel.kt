/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Load
import javax.inject.Inject

@HiltViewModel
class PrepaidLedgerDetailsViewModel @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager
) : BaseViewModel(){

    private val _channelNameResult = MutableLiveData<OneTimeEvent<ChannelNameResult>>()
    val channelNameResult: LiveData<OneTimeEvent<ChannelNameResult>> = _channelNameResult

    fun getChannelName(channel: String? = "", type: PrepaidLedgerTransactionItem.PrepaidType) {
        viewModelScope.launch {
            remoteConfigManager.getChannelMapConfig()?.let {
                val channelName = it[channel] ?: OTHERS
                if(type is Load && (type.type) == Load.LoadType.LOAD_BOUGHT) {
                    _channelNameResult.value = OneTimeEvent(
                        ChannelNameResult.LoadBoughtChannelName(channelName)
                    )
                } else {
                    _channelNameResult.value = OneTimeEvent(
                        ChannelNameResult.PromoReceivedOthersChannelName(channelName)
                    )
                }
            }
        }
    }

    sealed class ChannelNameResult {
        data class LoadBoughtChannelName(
            val channelName: String
        ) : ChannelNameResult()
        data class PromoReceivedOthersChannelName(
            val channelName: String
        ) : ChannelNameResult()
    }

    override val logTag = "PrepaidLedgerDetailsViewModel"
}

const val OTHERS = "Others"
