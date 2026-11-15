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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ioristudios.ac.model.FanSpeed
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.BorderGlassHighlight
import com.ioristudios.ac.ui.theme.ModeTheme
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.SurfaceGlassElevated
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary

@Composable
fun FanSpeedStepper(
    currentSpeed: FanSpeed,
    modeTheme: ModeTheme,
    isPoweredOn: Boolean,
    onSpeedChange: (FanSpeed) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceGlass)
            .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FAN VELOCITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            // Auto Fan Toggle Pill
            val isAuto = currentSpeed == FanSpeed.AUTO && isPoweredOn
            val autoBg by animateColorAsState(
                targetValue = if (isAuto) modeTheme.primary.copy(alpha = 0.25f) else Color.Transparent
            )
            val autoBorder by animateColorAsState(
                targetValue = if (isAuto) modeTheme.primary else BorderGlass
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(autoBg)
                    .border(1.dp, autoBorder, RoundedCornerShape(14.dp))
                    .clickable(enabled = isPoweredOn) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSpeedChange(FanSpeed.AUTO)
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AUTO",
                    fontSize = 10.sp,
                    fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAuto) modeTheme.primary else if (isPoweredOn) TextSecondary else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5-Stage Stepped Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val speeds = listOf(
                FanSpeed.SPEED_1,
                FanSpeed.SPEED_2,
                FanSpeed.SPEED_3,
                FanSpeed.SPEED_4,
                FanSpeed.SPEED_5
            )

            speeds.forEachIndexed { index, speed ->
                val level = index + 1
                val isActive = isPoweredOn && (
                    (currentSpeed != FanSpeed.AUTO && currentSpeed.level >= level) ||
                    (currentSpeed == FanSpeed.AUTO && level <= 3) // Preview middle for auto
                )

                val barHeight = (18 + (index * 7)).dp
                val barColor by animateColorAsState(
                    targetValue = when {
                        !isPoweredOn -> Color(0x10FFFFFF)
                        currentSpeed == FanSpeed.AUTO && isActive -> modeTheme.primary.copy(alpha = 0.35f)
                        isActive -> modeTheme.primary
                        else -> Color(0x15FFFFFF)
                    }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(barColor)
                        .clickable(enabled = isPoweredOn) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSpeedChange(speed)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Level caption
        Text(
            text = if (!isPoweredOn) "Powered Off" else if (currentSpeed == FanSpeed.AUTO) "Smart Dynamic Speed" else "Speed Level ${currentSpeed.level} of 5",
            fontSize = 11.sp,
            color = if (isPoweredOn) TextSecondary else TextMuted
        )
    }
}
