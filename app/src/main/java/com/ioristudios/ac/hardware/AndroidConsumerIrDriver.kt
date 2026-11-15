package com.ioristudios.ac.hardware

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

/**
 * Low-level driver encapsulating Android's ConsumerIrManager system service.
 */
class AndroidConsumerIrDriver(context: Context) {

    private val tag = "ConsumerIrDriver"

    private val irManager: ConsumerIrManager? = try {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    } catch (e: Exception) {
        Log.e(tag, "Failed to get ConsumerIrManager service", e)
        null
    }

    fun hasIrEmitter(): Boolean {
        return try {
            irManager?.hasIrEmitter() == true
        } catch (e: Exception) {
            Log.e(tag, "Error checking hasIrEmitter()", e)
            false
        }
    }

    fun getCarrierFrequencyRanges(): List<Pair<Int, Int>> {
        val ranges = irManager?.carrierFrequencies ?: return emptyList()
        return ranges.map { Pair(it.minFrequency, it.maxFrequency) }
    }

    fun isFrequencySupported(freqHz: Int): Boolean {
        val ranges = getCarrierFrequencyRanges()
        if (ranges.isEmpty()) return true
        return ranges.any { freqHz in it.first..it.second }
    }

    fun transmit(carrierHz: Int, pattern: IntArray): Result<Unit> {
        val manager = irManager ?: return Result.failure(
            IllegalStateException("ConsumerIrManager is null (IR service unavailable)")
        )

        if (!hasIrEmitter()) {
            return Result.failure(
                IllegalStateException("Device does not possess an infrared emitter")
            )
        }

        if (pattern.isEmpty()) {
            return Result.failure(IllegalArgumentException("IR pulse pattern is empty"))
        }

        val totalDurationUs = pattern.fold(0L) { acc, pulse -> acc + pulse }
        if (totalDurationUs > 2_000_000L) {
            return Result.failure(
                IllegalArgumentException("Pattern duration ($totalDurationUs us) exceeds 2-second limit")
            )
        }

        return try {
            manager.transmit(carrierHz, pattern)
            Log.d(tag, "Transmitted ${pattern.size} pulses at ${carrierHz}Hz (Total: ${totalDurationUs}us)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "HAL exception during ConsumerIrManager.transmit()", e)
            Result.failure(e)
        }
    }
}
