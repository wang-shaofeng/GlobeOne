/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard.maintenance

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.domain.maintenance.MaintenanceDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.maintenance.MaintenanceUIModel
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val maintenanceDomainManager: MaintenanceDomainManager
) : BaseViewModel() {

    private val _dashboardMaintenance: MutableLiveData<MaintenanceUIModel> = MutableLiveData()
    val dashboardMaintenance = _dashboardMaintenance

    init {
        getDashboardMaintenance()
    }

    fun getDashboardMaintenance() {
        viewModelScope.launch {
            maintenanceDomainManager.getDashboardMaintenance()
                .fold({
                    _dashboardMaintenance.value = it.outerMaintenance
                }, {

                })
        }
    }

    override val logTag: String = "MaintenanceViewModel"
}
