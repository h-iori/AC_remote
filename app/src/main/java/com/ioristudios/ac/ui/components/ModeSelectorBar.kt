package com.ioristudios.ac.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.BorderGlassHighlight
import com.ioristudios.ac.ui.theme.ModeTheme
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.SurfaceGlassElevated
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary
import com.ioristudios.ac.ui.theme.getThemeForMode

@Composable
fun ModeSelectorBar(
    currentMode: AcMode,
    isPoweredOn: Boolean,
    onModeSelect: (AcMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceGlass)
            .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AcMode.values().forEach { mode ->
            val isSelected = currentMode == mode && isPoweredOn
            val modeTheme = getThemeForMode(mode)

            val animatedBg by animateColorAsState(
                targetValue = if (isSelected) modeTheme.primary.copy(alpha = 0.22f) else Color.Transparent
            )

            val animatedBorder by animateColorAsState(
                targetValue = if (isSelected) modeTheme.primary.copy(alpha = 0.6f) else Color.Transparent
            )

            val icon = getModeIcon(mode)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(animatedBg)
                    .border(1.dp, animatedBorder, RoundedCornerShape(18.dp))
                    .clickable(enabled = isPoweredOn) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onModeSelect(mode)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.label,
                        tint = if (isSelected) modeTheme.primary else if (isPoweredOn) TextSecondary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mode.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else if (isPoweredOn) TextSecondary else TextMuted
                    )
                }
            }
        }
    }
}

private fun getModeIcon(mode: AcMode): ImageVector {
    return when (mode) {
        AcMode.AUTO -> Icons.Default.AutoAwesome
        AcMode.COOL -> Icons.Default.AcUnit
        AcMode.DRY -> Icons.Default.WaterDrop
        AcMode.FAN -> Icons.Default.Air
        AcMode.HEAT -> Icons.Default.WbSunny
    }
}
