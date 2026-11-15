package com.ioristudios.ac.protocol

import com.ioristudios.ac.model.AcState

/**
 * Common interface for AC infrared protocol encoders.
 */
interface AcProtocolEncoder {
    val carrierFrequencyHz: Int

    fun encodeRawBytes(state: AcState): ByteArray
    fun encodeToPulses(state: AcState): IntArray
    fun validateChecksum(raw: ByteArray): Boolean
}
