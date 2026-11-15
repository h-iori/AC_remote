package com.ioristudios.ac.model

data class SmartPreset(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String,
    val apply: (AcState) -> AcState
)

object SmartPresetRegistry {
    val presets = listOf(
        SmartPreset(
            id = "rapid_chill",
            name = "Rapid Chill",
            iconEmoji = "❄️",
            description = "16°C • Turbo Fan • Immediate Cooling",
            apply = { current ->
                current.copy(
                    power = true,
                    mode = AcMode.COOL,
                    targetTemperature = 16,
                    turbo = true,
                    quiet = false,
                    fanSpeed = FanSpeed.SPEED_5,
                    swingVertical = SwingVMode.AUTO
                ).validated()
            }
        ),
        SmartPreset(
            id = "night_sleep",
            name = "Night Sleep",
            iconEmoji = "🌙",
            description = "25°C • Whisper Quiet • Display Off",
            apply = { current ->
                current.copy(
                    power = true,
                    mode = AcMode.COOL,
                    targetTemperature = 25,
                    turbo = false,
                    quiet = true,
                    light = false,
                    fanSpeed = FanSpeed.SPEED_1,
                    swingVertical = SwingVMode.MIDDLE
                ).validated()
            }
        ),
        SmartPreset(
            id = "eco_comfort",
            name = "Eco Comfort",
            iconEmoji = "🌿",
            description = "26°C • Balanced Efficiency • Auto Fan",
            apply = { current ->
                current.copy(
                    power = true,
                    mode = AcMode.COOL,
                    targetTemperature = 26,
                    turbo = false,
                    quiet = false,
                    fanSpeed = FanSpeed.AUTO,
                    swingVertical = SwingVMode.AUTO
                ).validated()
            }
        ),
        SmartPreset(
            id = "dehumidify",
            name = "Dehumidify",
            iconEmoji = "💧",
            description = "Dry Mode • Low Humidity • X-Fan On",
            apply = { current ->
                current.copy(
                    power = true,
                    mode = AcMode.DRY,
                    targetTemperature = 25,
                    turbo = false,
                    fanSpeed = FanSpeed.SPEED_1,
                    xFan = true
                ).validated()
            }
        )
    )
}
