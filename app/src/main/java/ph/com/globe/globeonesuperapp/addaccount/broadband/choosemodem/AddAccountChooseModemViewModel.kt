/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import javax.inject.Inject

@HiltViewModel
class AddAccountChooseModemViewModel @Inject constructor() : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private var modems = listOf(
        ModemItem("huawei_v2", R.drawable.huawei_v2, HUAWEI_V2_ADDRESS, false),
        ModemItem("thebox", R.drawable.thebox, THE_BOX_ADDRESS, false),
        ModemItem("shanghai_boost", R.drawable.shanghai_boost, OLD_SHANGHAI_ADDRESS, false),
        ModemItem("huawei_2ca", R.drawable.huawei_2ca, HUAWEI_V2_ADDRESS, false, ModemItem.Credential("user", "Globe@AEBD")),
        ModemItem("huawei", R.drawable.huawei, HUAWEI_ADDRESS, false),
        ModemItem("fendi", R.drawable.fendi, FENDI_ADDRESS, false),
        ModemItem("notion", R.drawable.notion, NOTION_ADDRESS, false),
        ModemItem("tozed", R.drawable.tozed, TOZED_ADDRESS, false)
    )

    private val _modemsLiveData = MutableLiveData(modems)
    val modemsLiveData: LiveData<List<ModemItem>> = _modemsLiveData

    private val _enableButton = MutableLiveData(OneTimeEvent(false))
    val enableButton: LiveData<OneTimeEvent<Boolean>> = _enableButton

    var selectedModem: ModemItem? = null

    private val _pingableModem = MutableLiveData<OneTimeEvent<Boolean>>()
    val pingableModem: LiveData<OneTimeEvent<Boolean>> = _pingableModem

    fun chooseModem(item: ModemItem) {
        modems = modems.map {
            it.copy(selected = item.name == it.name)
        }
        selectedModem = modems.find { it.selected }
        _modemsLiveData.value = modems
        _enableButton.value = OneTimeEvent(true)
    }

    fun pingModem() {
        selectedModem?.let {
            viewModelScope.launchWithLoadingOverlay(handler) {
                try {
                    val process =
                        Runtime.getRuntime().exec("ping -c 1 -w 1 ${it.address.removePrefix("http://")}", null, null)
                    _pingableModem.value = OneTimeEvent(process.waitFor() == 0)
                } catch (e: Exception) {
                    _pingableModem.value = OneTimeEvent(false)
                }
            }
        }
    }

    override val logTag = "AddAccountChooseModemViewModel"
}

const val MODEM_HUAWEI_SMS_PATH = "http://192.168.254.254/html/smsinbox.html"
const val MODEM_HUAWEI_V2_SMS_PATH = "http://192.168.254.254/html/content.html#sms"
const val OLD_SHANGHAI_ADDRESS = "http://192.168.1.1"
const val NEW_SHANGHAI_ADDRESS = "http://192.168.254.254"
const val HUAWEI_ADDRESS = "http://192.168.254.254"
const val HUAWEI_V2_ADDRESS = "http://192.168.254.254"
const val THE_BOX_ADDRESS = "http://192.168.254.254"
const val FENDI_ADDRESS = "http://192.168.254.254"
const val TOZED_ADDRESS = "http://192.168.254.254"
const val NOTION_ADDRESS = "http://192.168.254.254"
