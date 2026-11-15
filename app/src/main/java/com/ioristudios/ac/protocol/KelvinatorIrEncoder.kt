package com.ioristudios.ac.protocol

import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.model.AcState
import com.ioristudios.ac.model.SwingVMode

/**
 * Pure Kotlin port of the Kelvinator / YB1FA IR Protocol Encoder based on IRremoteESP8266.
 *
 * Implements 16-byte state serialization, block checksums, and pulse-distance modulation.
 */
class KelvinatorIrEncoder : AcProtocolEncoder {

    override val carrierFrequencyHz: Int = CARRIER_FREQ_HZ

    companion object {
        const val CARRIER_FREQ_HZ = 38000
        const val TICK = 85
        const val HDR_MARK = 106 * TICK      // 9010 us
        const val HDR_SPACE = 53 * TICK      // 4505 us
        const val BIT_MARK = 8 * TICK        // 680 us
        const val ONE_SPACE = 18 * TICK      // 1530 us
        const val ZERO_SPACE = 6 * TICK      // 510 us
        const val GAP_SPACE = 235 * TICK     // 19975 us
        const val FRAME_GAP = GAP_SPACE * 2  // 39950 us
        const val CMD_FOOTER = 2             // 0b010 in 3 bits
        const val CMD_FOOTER_BITS = 3
        const val STATE_LENGTH = 16
        const val CHECKSUM_START = 10

        const val TOTAL_PULSE_COUNT = 280

        fun calcBlockChecksum(block: ByteArray, offset: Int): Int {
            var sum = CHECKSUM_START
            for (i in 0 until 4) {
                sum += (block[offset + i].toInt() and 0x0F)
            }
            for (i in 4 until 7) {
                sum += ((block[offset + i].toInt() and 0xFF) ushr 4)
            }
            return sum and 0x0F
        }
    }

    override fun encodeRawBytes(state: AcState): ByteArray {
        val raw = ByteArray(STATE_LENGTH)
        raw[3] = 0x50.toByte() // Block 1 marker
        raw[11] = 0x70.toByte() // Block 2 marker

        // Byte 0: Mode (0..2), Power (3), BasicFan (4..5), SwingAuto (6), Sleep (7)
        val modeVal = state.mode.code and 0x07
        val powerVal = if (state.power) 1 else 0
        val basicFanVal = state.fanSpeed.basicCode and 0x03
        val swingAutoVal = if (state.swingVertical == SwingVMode.AUTO || state.swingHorizontal) 1 else 0
        val sleepVal = if (state.sleep) 1 else 0
        raw[0] = (modeVal or (powerVal shl 3) or (basicFanVal shl 4) or (swingAutoVal shl 6) or (sleepVal shl 7)).toByte()

        // Byte 1: Temp (0..3) = temp - 16
        val clampedTemp = state.targetTemperature.coerceIn(AcState.MIN_TEMP, AcState.MAX_TEMP)
        val tempVal = (clampedTemp - AcState.MIN_TEMP) and 0x0F
        raw[1] = tempVal.toByte()

        // Byte 2: Turbo (4), Light (5), IonFilter (6), XFan (7)
        var b2 = 0
        if (state.turbo) b2 = b2 or (1 shl 4)
        if (state.light) b2 = b2 or (1 shl 5)
        if (state.ionFilter) b2 = b2 or (1 shl 6)
        if (state.xFan && (state.mode == AcMode.COOL || state.mode == AcMode.DRY)) {
            b2 = b2 or (1 shl 7)
        }
        raw[2] = b2.toByte()

        // Byte 4: SwingV (0..3), SwingH (4)
        val swingVVal = state.swingVertical.code and 0x0F
        val swingHVal = if (state.swingHorizontal) 1 else 0
        raw[4] = (swingVVal or (swingHVal shl 4)).toByte()

        // Duplicate Bytes 0..2 into Bytes 8..10
        raw[8] = raw[0]
        raw[9] = raw[1]
        raw[10] = raw[2]

        // Byte 12: Quiet (7), Sleep2 (0)
        var b12 = 0
        if (state.quiet) b12 = b12 or (1 shl 7)
        raw[12] = b12.toByte()

        // Byte 14: Extended Fan Speed (4..6)
        val fanExtVal = (state.fanSpeed.extendedCode and 0x07) shl 4
        raw[14] = (raw[14].toInt() or fanExtVal).toByte()

        // Checksums
        val sum1 = calcBlockChecksum(raw, offset = 0)
        raw[7] = ((raw[7].toInt() and 0x0F) or (sum1 shl 4)).toByte()

        val sum2 = calcBlockChecksum(raw, offset = 8)
        raw[15] = ((raw[15].toInt() and 0x0F) or (sum2 shl 4)).toByte()

        return raw
    }

    override fun encodeToPulses(state: AcState): IntArray {
        val raw = encodeRawBytes(state)
        val pulses = ArrayList<Int>(TOTAL_PULSE_COUNT)

        // 1. Command Block #1 (Bytes 0..3)
        appendBlock(pulses, raw, offset = 0, length = 4, hasHeader = true, hasFooter = false, gap = 0)

        // 2. Footer #1 (3 bits: 0b010) + Footer Mark + 20ms Gap
        appendBits(pulses, CMD_FOOTER, CMD_FOOTER_BITS)
        pulses.add(BIT_MARK)
        pulses.add(GAP_SPACE)

        // 3. Data Block #1 (Bytes 4..7) + Footer Mark + 40ms Gap
        appendBlock(pulses, raw, offset = 4, length = 4, hasHeader = false, hasFooter = true, gap = FRAME_GAP)

        // 4. Command Block #2 (Bytes 8..11)
        appendBlock(pulses, raw, offset = 8, length = 4, hasHeader = true, hasFooter = false, gap = 0)

        // 5. Footer #2 (3 bits: 0b010) + Footer Mark + 20ms Gap
        appendBits(pulses, CMD_FOOTER, CMD_FOOTER_BITS)
        pulses.add(BIT_MARK)
        pulses.add(GAP_SPACE)

        // 6. Data Block #2 (Bytes 12..15) + Footer Mark + 40ms Gap
        appendBlock(pulses, raw, offset = 12, length = 4, hasHeader = false, hasFooter = true, gap = FRAME_GAP)

        return pulses.toIntArray()
    }

    override fun validateChecksum(raw: ByteArray): Boolean {
        if (raw.size < STATE_LENGTH) return false
        val sum1 = calcBlockChecksum(raw, 0)
        val expectedSum1 = (raw[7].toInt() and 0xFF) ushr 4
        if (sum1 != expectedSum1) return false

        val sum2 = calcBlockChecksum(raw, 8)
        val expectedSum2 = (raw[15].toInt() and 0xFF) ushr 4
        return sum2 == expectedSum2
    }

    private fun appendBlock(
        pulses: MutableList<Int>,
        data: ByteArray,
        offset: Int,
        length: Int,
        hasHeader: Boolean,
        hasFooter: Boolean,
        gap: Int
    ) {
        if (hasHeader) {
            pulses.add(HDR_MARK)
            pulses.add(HDR_SPACE)
        }
        for (i in 0 until length) {
            val byteVal = data[offset + i].toInt() and 0xFF
            appendBits(pulses, byteVal, 8)
        }
        if (hasFooter) {
            pulses.add(BIT_MARK)
            pulses.add(gap)
        }
    }

    private fun appendBits(pulses: MutableList<Int>, data: Int, numBits: Int) {
        for (b in 0 until numBits) {
            pulses.add(BIT_MARK)
            val isOne = ((data shr b) and 1) == 1
            pulses.add(if (isOne) ONE_SPACE else ZERO_SPACE)
        }
    }
}
