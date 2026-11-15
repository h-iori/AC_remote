package com.ioristudios.ac.protocol

import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.model.AcState
import com.ioristudios.ac.model.SwingVMode

/**
 * Pure Kotlin port of the Gree 8-Byte A/C Protocol Encoder based on IRremoteESP8266.
 */
class GreeIrEncoder : AcProtocolEncoder {

    override val carrierFrequencyHz: Int = CARRIER_FREQ_HZ

    companion object {
        const val CARRIER_FREQ_HZ = 38000
        const val HDR_MARK = 9000
        const val HDR_SPACE = 4500
        const val BIT_MARK = 620
        const val ONE_SPACE = 1600
        const val ZERO_SPACE = 540
        const val GAP_SPACE = 19980
        const val CMD_FOOTER = 2
        const val CMD_FOOTER_BITS = 3
        const val STATE_LENGTH = 8
    }

    override fun encodeRawBytes(state: AcState): ByteArray {
        val raw = ByteArray(STATE_LENGTH)
        raw[3] = 0x50.toByte()

        val modeVal = state.mode.code and 0x07
        val powerVal = if (state.power) 1 else 0
        val fanVal = state.fanSpeed.basicCode and 0x03
        val swingAutoVal = if (state.swingVertical == SwingVMode.AUTO || state.swingHorizontal) 1 else 0
        val sleepVal = if (state.sleep) 1 else 0
        raw[0] = (modeVal or (powerVal shl 3) or (fanVal shl 4) or (swingAutoVal shl 6) or (sleepVal shl 7)).toByte()

        val clampedTemp = state.targetTemperature.coerceIn(AcState.MIN_TEMP, AcState.MAX_TEMP)
        val tempVal = (clampedTemp - AcState.MIN_TEMP) and 0x0F
        raw[1] = tempVal.toByte()

        var b2 = 0
        if (state.turbo) b2 = b2 or (1 shl 4)
        if (state.light) b2 = b2 or (1 shl 5)
        if (state.xFan && (state.mode == AcMode.COOL || state.mode == AcMode.DRY)) {
            b2 = b2 or (1 shl 7)
        }
        raw[2] = b2.toByte()

        val swingVVal = state.swingVertical.code and 0x0F
        val swingHVal = if (state.swingHorizontal) 1 else 0
        raw[4] = (swingVVal or (swingHVal shl 4)).toByte()
        raw[5] = 0x20.toByte()

        val sum = KelvinatorIrEncoder.calcBlockChecksum(raw, 0)
        raw[7] = ((raw[7].toInt() and 0x0F) or (sum shl 4)).toByte()

        return raw
    }

    override fun encodeToPulses(state: AcState): IntArray {
        val raw = encodeRawBytes(state)
        val pulses = ArrayList<Int>(140)

        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)
        for (i in 0 until 4) {
            appendBits(pulses, raw[i].toInt() and 0xFF, 8)
        }

        appendBits(pulses, CMD_FOOTER, CMD_FOOTER_BITS)
        pulses.add(BIT_MARK)
        pulses.add(GAP_SPACE)

        for (i in 4 until 8) {
            appendBits(pulses, raw[i].toInt() and 0xFF, 8)
        }
        pulses.add(BIT_MARK)
        pulses.add(GAP_SPACE)

        return pulses.toIntArray()
    }

    override fun validateChecksum(raw: ByteArray): Boolean {
        if (raw.size < STATE_LENGTH) return false
        val sum = KelvinatorIrEncoder.calcBlockChecksum(raw, 0)
        val expected = (raw[7].toInt() and 0xFF) ushr 4
        return sum == expected
    }

    private fun appendBits(pulses: MutableList<Int>, data: Int, numBits: Int) {
        for (b in 0 until numBits) {
            pulses.add(BIT_MARK)
            val isOne = ((data shr b) and 1) == 1
            pulses.add(if (isOne) ONE_SPACE else ZERO_SPACE)
        }
    }
}
