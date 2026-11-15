package com.ioristudios.ac.protocol

import com.ioristudios.ac.model.AcState

/**
 * Utility for parsing raw IR timings and Pronto Hex strings into ConsumerIrManager pulse patterns.
 */
class RawIrEncoder : AcProtocolEncoder {

    override val carrierFrequencyHz: Int = 38000

    override fun encodeRawBytes(state: AcState): ByteArray = ByteArray(0)
    override fun encodeToPulses(state: AcState): IntArray = IntArray(0)
    override fun validateChecksum(raw: ByteArray): Boolean = true

    companion object {
        fun parseMicrosecondsCsv(rawText: String): IntArray {
            val tokens = rawText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
            return tokens.mapNotNull { token ->
                val clean = token.replace("+", "").replace("-", "").trim()
                clean.toIntOrNull()
            }.toIntArray()
        }

        fun parseProntoHex(pronto: String): Pair<Int, IntArray>? {
            val words = pronto.trim().split(Regex("\\s+")).mapNotNull {
                it.toIntOrNull(16)
            }
            if (words.size < 4) return null
            if (words[0] != 0x0000) return null

            val freqCode = words[1]
            if (freqCode == 0) return null
            val carrierHz = (1000000.0 / (freqCode * 0.241246)).toInt()

            val onceLength = words[2] * 2
            val repeatLength = words[3] * 2
            val totalPairs = onceLength + repeatLength

            if (words.size < 4 + totalPairs) return null

            val timeBaseUs = 1000000.0 / carrierHz
            val pulses = ArrayList<Int>(totalPairs)
            val startIndex = 4
            val count = if (onceLength > 0) onceLength else repeatLength

            for (i in 0 until count) {
                val durationUs = (words[startIndex + i] * timeBaseUs).toInt()
                pulses.add(durationUs)
            }

            return Pair(carrierHz, pulses.toIntArray())
        }
    }
}
