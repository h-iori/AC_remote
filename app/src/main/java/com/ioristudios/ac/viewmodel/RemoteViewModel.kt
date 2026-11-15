package com.ioristudios.ac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ioristudios.ac.data.AcStateRepository
import com.ioristudios.ac.hardware.IrTransmissionService
import com.ioristudios.ac.hardware.IrTransmissionStatus
import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.model.AcState
import com.ioristudios.ac.model.FanSpeed
import com.ioristudios.ac.model.ProtocolType
import com.ioristudios.ac.model.SmartPreset
import com.ioristudios.ac.model.SwingVMode
import com.ioristudios.ac.protocol.GreeIrEncoder
import com.ioristudios.ac.protocol.KelvinatorIrEncoder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RemoteUiState(
    val acState: AcState = AcState(),
    val isHardwareAvailable: Boolean = false,
    val isLouverSheetVisible: Boolean = false,
    val isTimerSheetVisible: Boolean = false,
    val isHiddenDiagnosticsVisible: Boolean = false,
    val transmissionStatus: IrTransmissionStatus = IrTransmissionStatus.Idle,
    val encodedRawBytes: ByteArray = ByteArray(0),
    val lastPulseCount: Int = 0,
    val titleTapCount: Int = 0,
    val logMessages: List<String> = emptyList()
)

class RemoteViewModel(
    private val repository: AcStateRepository,
    private val transmissionService: IrTransmissionService
) : ViewModel() {

    private val kelvinatorEncoder = KelvinatorIrEncoder()
    private val greeEncoder = GreeIrEncoder()

    private var transmitDebounceJob: Job? = null
    private var timerCountdownJob: Job? = null

    private val _uiState = MutableStateFlow(
        RemoteUiState(
            isHardwareAvailable = transmissionService.hasHardware()
        )
    )
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    init {
        // Collect persistent state
        viewModelScope.launch {
            repository.stateFlow.collect { savedState ->
                updateStateInternal(savedState, transmit = false)
            }
        }

        // Collect hardware transmission status
        viewModelScope.launch {
            transmissionService.status.collect { status ->
                _uiState.update { it.copy(transmissionStatus = status) }
            }
        }
    }

    private fun updateStateInternal(newState: AcState, transmit: Boolean = true) {
        val validated = newState.validated()
        val encoder = when (validated.selectedProtocol) {
            ProtocolType.KELVINATOR_16BYTE -> kelvinatorEncoder
            ProtocolType.GREE_8BYTE -> greeEncoder
            ProtocolType.RAW_INJECTION -> kelvinatorEncoder
        }

        val rawBytes = encoder.encodeRawBytes(validated)
        val pulses = encoder.encodeToPulses(validated)

        _uiState.update {
            it.copy(
                acState = validated,
                encodedRawBytes = rawBytes,
                lastPulseCount = pulses.size
            )
        }

        if (transmit) {
            // Debounce rapid adjustments by 250ms
            transmitDebounceJob?.cancel()
            transmitDebounceJob = viewModelScope.launch {
                delay(250)
                repository.saveState(validated)
                transmissionService.transmitPattern(validated.carrierFrequencyHz, pulses)
            }
        }
    }

    fun togglePower() {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(power = !current.power))
    }

    fun setTemperature(temperature: Int) {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(targetTemperature = temperature))
    }

    fun adjustTemperature(delta: Int) {
        val current = _uiState.value.acState
        updateStateInternal(current.withTemperatureDelta(delta))
    }

    fun setMode(mode: AcMode) {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(mode = mode))
    }

    fun setFanSpeed(speed: FanSpeed) {
        val current = _uiState.value.acState
        // Changing fan speed clears turbo mode
        updateStateInternal(current.copy(fanSpeed = speed, turbo = false))
    }

    fun setSwingVertical(swing: SwingVMode) {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(swingVertical = swing))
    }

    fun toggleSwingHorizontal() {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(swingHorizontal = !current.swingHorizontal))
    }

    fun toggleTurbo() {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(turbo = !current.turbo))
    }

    fun toggleLight() {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(light = !current.light))
    }

    fun toggleXFan() {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(xFan = !current.xFan))
    }

    fun toggleQuiet() {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(quiet = !current.quiet))
    }

    fun applyPreset(preset: SmartPreset) {
        val current = _uiState.value.acState
        val updated = preset.apply(current)
        updateStateInternal(updated)
    }

    fun setTimerMinutes(minutes: Int) {
        timerCountdownJob?.cancel()
        val totalSecs = minutes.toLong() * 60L
        _uiState.update { it.copy(acState = it.acState.copy(timerRemainingSeconds = totalSecs)) }

        timerCountdownJob = viewModelScope.launch {
            var remaining = totalSecs
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(acState = it.acState.copy(timerRemainingSeconds = remaining)) }
            }
            // Timer expired: trigger power off
            if (_uiState.value.acState.power) {
                togglePower()
            }
        }
    }

    fun cancelTimer() {
        timerCountdownJob?.cancel()
        _uiState.update { it.copy(acState = it.acState.copy(timerRemainingSeconds = 0L)) }
    }

    fun setLouverSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isLouverSheetVisible = visible) }
    }

    fun setTimerSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isTimerSheetVisible = visible) }
    }

    fun setHiddenDiagnosticsVisible(visible: Boolean) {
        _uiState.update { it.copy(isHiddenDiagnosticsVisible = visible) }
    }

    fun onTitleTapped() {
        val count = _uiState.value.titleTapCount + 1
        if (count >= 5) {
            _uiState.update { it.copy(titleTapCount = 0, isHiddenDiagnosticsVisible = true) }
        } else {
            _uiState.update { it.copy(titleTapCount = count) }
        }
    }

    fun setProtocol(protocol: ProtocolType) {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(selectedProtocol = protocol))
    }

    fun setCarrierFrequency(freqHz: Int) {
        val current = _uiState.value.acState
        updateStateInternal(current.copy(carrierFrequencyHz = freqHz), transmit = false)
    }

    fun transmitRawPulses(carrierHz: Int, pattern: IntArray) {
        viewModelScope.launch {
            transmissionService.transmitPattern(carrierHz, pattern)
        }
    }
}

class RemoteViewModelFactory(
    private val repository: AcStateRepository,
    private val transmissionService: IrTransmissionService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemoteViewModel(repository, transmissionService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
