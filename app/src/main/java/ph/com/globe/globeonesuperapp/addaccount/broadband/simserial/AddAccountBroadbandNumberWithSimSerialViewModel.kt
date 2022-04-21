package ph.com.globe.globeonesuperapp.addaccount.broadband.simserial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.errors.auth.ValidateSimSerialPairingError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.auth.ValidateSimSerialParams
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AddAccountBroadbandNumberWithSimSerialViewModel  @Inject constructor(
    private val authDomainManager: AuthDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _validateSimSerialLivaData = MutableLiveData<OneTimeEvent<ValidateSimSerialResult>>()
    val validateSimSerialLivaData: LiveData<OneTimeEvent<ValidateSimSerialResult>> = _validateSimSerialLivaData

    fun validateSimSerial(msisdn: String, simSerial: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {

            authDomainManager.validateSimSerial(
                ValidateSimSerialParams(
                    mobileNumber = msisdn,
                    simSerial = simSerial
                )
            ).fold({
                dLog("Validate Sim Serial successfully.")
                _validateSimSerialLivaData.value =
                    OneTimeEvent(
                        ValidateSimSerialResult.ValidatedSimSerialSuccess(
                            simReferenceId = it.result.simReferenceId
                        )
                    )

            }, {
                dLog("Validate Sim Serial failed.")
                when(it) {
                    is ValidateSimSerialPairingError.NotValidBroadbandAccount -> {
                        _validateSimSerialLivaData.value =
                            OneTimeEvent(ValidateSimSerialResult.NotAValidBroadbandSimSerialPairing)
                    }
                    is ValidateSimSerialPairingError.General -> {
                        _validateSimSerialLivaData.value =
                            OneTimeEvent(ValidateSimSerialResult.General)
                    }
                    else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                }
            })
        }
    }

    sealed class ValidateSimSerialResult {

        object General : ValidateSimSerialResult()

        object NotAValidBroadbandSimSerialPairing : ValidateSimSerialResult()

        data class ValidatedSimSerialSuccess(
            val simReferenceId: String
        ) : ValidateSimSerialResult()
    }
    override val logTag = "AddAccountSimSerialViewModel"

}
