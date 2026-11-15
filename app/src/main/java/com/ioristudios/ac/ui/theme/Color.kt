package com.ioristudios.ac.ui.theme

import androidx.compose.ui.graphics.Color
import com.ioristudios.ac.model.AcMode

val OledBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF101014)
val SurfaceGlass = Color(0x18FFFFFF)
val SurfaceGlassElevated = Color(0x22FFFFFF)
val BorderGlass = Color(0x26FFFFFF)
val BorderGlassHighlight = Color(0x40FFFFFF)

val TextPrimary = Color(0xFFFBFBFD)
val TextSecondary = Color(0xFF9898A0)
val TextMuted = Color(0xFF5A5A62)

// Mode Themes (Primary Accent, Secondary Glow, Aura Color)
data class ModeTheme(
    val primary: Color,
    val secondaryGlow: Color,
    val aura: Color
)

fun getThemeForMode(mode: AcMode): ModeTheme {
    return when (mode) {
        AcMode.COOL -> ModeTheme(
            primary = Color(0xFF0A84FF),
            secondaryGlow = Color(0xFF00C7BE),
            aura = Color(0x300A84FF)
        )
        AcMode.HEAT -> ModeTheme(
            primary = Color(0xFFFF9F0A),
            secondaryGlow = Color(0xFFFF453A),
            aura = Color(0x30FF9F0A)
        )
        AcMode.DRY -> ModeTheme(
            primary = Color(0xFF30D158),
            secondaryGlow = Color(0xFF00C7BE),
            aura = Color(0x3030D158)
        )
        AcMode.FAN -> ModeTheme(
            primary = Color(0xFFBF5AF2),
            secondaryGlow = Color(0xFF5E5CE6),
            aura = Color(0x30BF5AF2)
        )
        AcMode.AUTO -> ModeTheme(
            primary = Color(0xFF5E5CE6),
            secondaryGlow = Color(0xFF0A84FF),
            aura = Color(0x305E5CE6)
        )
    }
}
