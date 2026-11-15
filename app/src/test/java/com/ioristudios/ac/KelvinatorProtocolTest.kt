package com.ioristudios.ac

import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.model.AcState
import com.ioristudios.ac.model.FanSpeed
import com.ioristudios.ac.model.SwingVMode
import com.ioristudios.ac.protocol.GreeIrEncoder
import com.ioristudios.ac.protocol.KelvinatorIrEncoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KelvinatorProtocolTest {

    private val encoder = KelvinatorIrEncoder()
    private val greeEncoder = GreeIrEncoder()

    @Test
    fun testDefaultStateStructure() {
        val state = AcState(
            power = true,
            targetTemperature = 24,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO,
            swingVertical = SwingVMode.AUTO
        )

        val raw = encoder.encodeRawBytes(state)
        assertEquals(16, raw.size)

        // Mode is COOL (1), Power is ON (1 shl 3 = 8), BasicFan is AUTO (0), SwingAuto is 1 (1 shl 6 = 64)
        assertEquals(0x49.toByte(), raw[0])
        assertEquals(0x08.toByte(), raw[1])
        assertEquals(0x20.toByte(), raw[2])
        assertEquals(0x50.toByte(), raw[3])
        assertEquals(0x01.toByte(), raw[4])

        // Duplication check
        assertEquals(raw[0], raw[8])
        assertEquals(raw[1], raw[9])
        assertEquals(raw[2], raw[10])
        assertEquals(0x70.toByte(), raw[11])

        assertTrue(encoder.validateChecksum(raw))
    }

    @Test
    fun testExact280PulseCount() {
        val state = AcState(
            power = true,
            targetTemperature = 22,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.SPEED_3,
            swingVertical = SwingVMode.AUTO,
            turbo = true
        )

        val pulses = encoder.encodeToPulses(state)
        assertEquals(280, pulses.size)
        assertEquals(9010, pulses[0])
        assertEquals(4505, pulses[1])

        for (i in pulses.indices step 2) {
            assertTrue("Pulse at even index $i must be positive mark", pulses[i] > 0)
        }
        for (i in 1 until pulses.size step 2) {
            assertTrue("Pulse at odd index $i must be positive space", pulses[i] > 0)
        }
    }

    @Test
    fun testChecksumAlgorithmAccuracy() {
        val raw = ByteArray(16)
        raw[0] = 0x49.toByte()
        raw[1] = 0x08.toByte()
        raw[2] = 0x20.toByte()
        raw[3] = 0x50.toByte()
        raw[4] = 0x01.toByte()
        raw[5] = 0x00.toByte()
        raw[6] = 0x00.toByte()

        val sum = KelvinatorIrEncoder.calcBlockChecksum(raw, 0)
        assertEquals(11, sum)
    }

    @Test
    fun testGreeFallbackEncoder() {
        val state = AcState(
            power = true,
            targetTemperature = 25,
            mode = AcMode.COOL
        )

        val raw = greeEncoder.encodeRawBytes(state)
        assertEquals(8, raw.size)
        assertTrue(greeEncoder.validateChecksum(raw))

        val pulses = greeEncoder.encodeToPulses(state)
        assertTrue(pulses.isNotEmpty())
        assertEquals(9000, pulses[0])
        assertEquals(4500, pulses[1])
    }
}
