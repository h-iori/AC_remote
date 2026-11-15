package com.ioristudios.ac.hardware

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed class IrTransmissionStatus {
    object Idle : IrTransmissionStatus()
    object Transmitting : IrTransmissionStatus()
    data class Success(val pulseCount: Int, val timestamp: Long) : IrTransmissionStatus()
    data class Error(val message: String, val timestamp: Long) : IrTransmissionStatus()
}

/**
 * Service managing thread-safe, non-blocking queuing and dispatching of IR signals.
 */
class IrTransmissionService(private val driver: AndroidConsumerIrDriver) {

    private val mutex = Mutex()

    private val _status = MutableStateFlow<IrTransmissionStatus>(IrTransmissionStatus.Idle)
    val status: StateFlow<IrTransmissionStatus> = _status.asStateFlow()

    fun hasHardware(): Boolean = driver.hasIrEmitter()

    suspend fun transmitPattern(carrierHz: Int, pattern: IntArray): Result<Unit> = withContext(Dispatchers.Default) {
        mutex.withLock {
            _status.value = IrTransmissionStatus.Transmitting
            val result = driver.transmit(carrierHz, pattern)
            val now = System.currentTimeMillis()

            if (result.isSuccess) {
                _status.value = IrTransmissionStatus.Success(pulseCount = pattern.size, timestamp = now)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Transmission error"
                _status.value = IrTransmissionStatus.Error(message = err, timestamp = now)
            }
            result
        }
    }
}
