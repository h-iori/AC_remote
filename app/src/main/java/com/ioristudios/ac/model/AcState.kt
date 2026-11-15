package com.ioristudios.ac.model

/**
 * Operating modes supported by the air conditioner.
 */
enum class AcMode(val code: Int, val label: String) {
    AUTO(0, "Auto"),
    COOL(1, "Cool"),
    DRY(2, "Dry"),
    FAN(3, "Fan"),
    HEAT(4, "Heat")
}

/**
 * Fan speeds. Maps to both basic fan (0..3) in Byte 0 and extended fan (0..5) in Byte 14.
 */
enum class FanSpeed(val basicCode: Int, val extendedCode: Int, val label: String, val level: Int) {
    AUTO(0, 0, "Auto", 0),
    SPEED_1(1, 1, "Level 1", 1),
    SPEED_2(2, 2, "Level 2", 2),
    SPEED_3(2, 3, "Level 3", 3),
    SPEED_4(3, 4, "Level 4", 4),
    SPEED_5(3, 5, "Level 5", 5)
}

/**
 * Vertical swing vane positions.
 */
enum class SwingVMode(val code: Int, val label: String, val isAuto: Boolean) {
    OFF(0b0000, "Fixed Off", false),
    AUTO(0b0001, "Full Auto Sweep", true),
    HIGHEST(0b0010, "Highest Angle", false),
    UPPER_MIDDLE(0b0011, "Upper-Mid Angle", false),
    MIDDLE(0b0100, "Middle Angle", false),
    LOWER_MIDDLE(0b0101, "Lower-Mid Angle", false),
    LOWEST(0b0110, "Lowest Angle", false),
    LOW_AUTO(0b0111, "Lower Auto Sweep", true),
    MIDDLE_AUTO(0b1001, "Middle Auto Sweep", true),
    HIGH_AUTO(0b1011, "Upper Auto Sweep", true)
}

/**
 * Supported underlying protocol engines.
 */
enum class ProtocolType(val label: String, val stateBytes: Int) {
    KELVINATOR_16BYTE("Kelvinator 16-Byte (Default)", 16),
    GREE_8BYTE("Gree 8-Byte (Alternative)", 8),
    RAW_INJECTION("Raw Timing Injection", 0)
}

/**
 * Immutable AC State representation holding all configuration values.
 */
data class AcState(
    val power: Boolean = true,
    val targetTemperature: Int = 24, // 16 to 30 C
    val mode: AcMode = AcMode.COOL,
    val fanSpeed: FanSpeed = FanSpeed.AUTO,
    val swingVertical: SwingVMode = SwingVMode.AUTO,
    val swingHorizontal: Boolean = false,
    val turbo: Boolean = false,
    val light: Boolean = true,
    val xFan: Boolean = false,
    val ionFilter: Boolean = false,
    val quiet: Boolean = false,
    val sleep: Boolean = false,
    val timerRemainingSeconds: Long = 0L,
    val selectedProtocol: ProtocolType = ProtocolType.KELVINATOR_16BYTE,
    val carrierFrequencyHz: Int = 38000
) {
    companion object {
        const val MIN_TEMP = 16
        const val MAX_TEMP = 30
        const val DEFAULT_AUTO_TEMP = 25
    }

    /**
     * Safely increments or decrements temperature within 16..30 range.
     */
    fun withTemperatureDelta(delta: Int): AcState {
        if (mode == AcMode.AUTO || mode == AcMode.DRY) return this
        val newTemp = (targetTemperature + delta).coerceIn(MIN_TEMP, MAX_TEMP)
        return copy(targetTemperature = newTemp)
    }

    /**
     * Enforces hardware domain rules and mutual exclusions:
     * - Auto & Dry lock temp to 25°C and disable Turbo.
     * - X-Fan is only valid in Cool or Dry modes.
     * - Turbo and Quiet are mutually exclusive.
     */
    fun validated(): AcState {
        val isAutoOrDry = (mode == AcMode.AUTO || mode == AcMode.DRY)
        val validTemp = if (isAutoOrDry) DEFAULT_AUTO_TEMP else targetTemperature.coerceIn(MIN_TEMP, MAX_TEMP)
        val validXFan = xFan && (mode == AcMode.COOL || mode == AcMode.DRY)
        val validTurbo = if (isAutoOrDry) false else turbo
        val validQuiet = if (validTurbo) false else quiet

        return copy(
            targetTemperature = validTemp,
            xFan = validXFan,
            turbo = validTurbo,
            quiet = validQuiet
        )
    }
}
