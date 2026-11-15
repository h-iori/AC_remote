package com.ioristudios.ac.ui.components

import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.model.AcState
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.BorderGlassHighlight
import com.ioristudios.ac.ui.theme.ModeTheme
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.SurfaceGlassElevated
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CircularTemperatureDial(
    acState: AcState,
    modeTheme: ModeTheme,
    onTemperatureChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isAutoOrDry = (acState.mode == AcMode.AUTO || acState.mode == AcMode.DRY)
    val enabled = acState.power && !isAutoOrDry

    val animatedPrimaryColor by animateColorAsState(
        targetValue = if (acState.power) modeTheme.primary else TextMuted,
        animationSpec = tween(durationMillis = 350)
    )

    val animatedGlowColor by animateColorAsState(
        targetValue = if (acState.power) modeTheme.secondaryGlow else TextMuted,
        animationSpec = tween(durationMillis = 350)
    )

    val minTemp = AcState.MIN_TEMP // 16
    val maxTemp = AcState.MAX_TEMP // 30
    val totalSteps = maxTemp - minTemp // 14 steps
    val startAngle = 135f
    val sweepAngle = 270f

    // Fraction 0.0 to 1.0
    val tempFraction = ((acState.targetTemperature - minTemp).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    val activeSweep = sweepAngle * tempFraction

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .pointerInteropFilter { event ->
                    if (!enabled) return@pointerInteropFilter false
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            val center = 140f
                            val x = event.x - center
                            val y = event.y - center
                            var angle = (atan2(y.toDouble(), x.toDouble()) * (180 / PI)).toFloat()
                            if (angle < 0) angle += 360f

                            // Normalize relative to startAngle (135 deg)
                            var relativeAngle = angle - startAngle
                            if (relativeAngle < 0) relativeAngle += 360f

                            if (relativeAngle <= sweepAngle) {
                                val fraction = (relativeAngle / sweepAngle).coerceIn(0f, 1f)
                                val newTemp = (minTemp + (fraction * totalSteps).toInt()).coerceIn(minTemp, maxTemp)
                                if (newTemp != acState.targetTemperature) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTemperatureChange(newTemp)
                                }
                            }
                            true
                        }
                        else -> false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val diameter = size.minDimension - strokeWidth - 16.dp.toPx()
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val radius = diameter / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // 1. Inactive background track
                drawArc(
                    color = Color(0x18FFFFFF),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Active illuminated progress arc
                if (acState.power) {
                    val brush = Brush.sweepGradient(
                        colors = listOf(animatedPrimaryColor, animatedGlowColor),
                        center = center
                    )
                    drawArc(
                        brush = brush,
                        startAngle = startAngle,
                        sweepAngle = activeSweep.coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 3. Glowing indicator knob at arc end
                    val currentKnobAngleRad = ((startAngle + activeSweep) * (PI / 180)).toFloat()
                    val knobX = center.x + radius * cos(currentKnobAngleRad)
                    val knobY = center.y + radius * sin(currentKnobAngleRad)

                    // Outer glow
                    drawCircle(
                        color = animatedGlowColor.copy(alpha = 0.35f),
                        radius = strokeWidth * 1.3f,
                        center = Offset(knobX, knobY)
                    )
                    // Inner solid knob
                    drawCircle(
                        color = Color.White,
                        radius = strokeWidth * 0.65f,
                        center = Offset(knobX, knobY)
                    )
                }

                // 4. Subtle perimeter ticks
                for (i in 0..totalSteps) {
                    val tickFraction = i.toFloat() / totalSteps.toFloat()
                    val tickAngleRad = ((startAngle + (sweepAngle * tickFraction)) * (PI / 180)).toFloat()
                    val tickInnerRadius = radius - strokeWidth * 0.9f
                    val tickOuterRadius = radius - strokeWidth * 0.6f

                    val startX = center.x + tickInnerRadius * cos(tickAngleRad)
                    val startY = center.y + tickInnerRadius * sin(tickAngleRad)
                    val endX = center.x + tickOuterRadius * cos(tickAngleRad)
                    val endY = center.y + tickOuterRadius * sin(tickAngleRad)

                    val isPassed = tickFraction <= tempFraction && acState.power
                    drawLine(
                        color = if (isPassed) animatedPrimaryColor.copy(alpha = 0.6f) else Color(0x22FFFFFF),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isAutoOrDry) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGlassElevated)
                            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTO MANAGED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = animatedPrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = if (acState.power) "${acState.targetTemperature}" else "OFF",
                        fontSize = if (acState.power) 74.sp else 54.sp,
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.SansSerif,
                        color = if (acState.power) TextPrimary else TextMuted,
                        letterSpacing = (-2).sp
                    )
                    if (acState.power) {
                        Text(
                            text = "°C",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 14.dp, start = 2.dp)
                        )
                    }
                }

                Text(
                    text = if (acState.power) {
                        "${acState.mode.label.uppercase()} • ${acState.fanSpeed.label.uppercase()}"
                    } else {
                        "TAP POWER TO WAKE"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = if (acState.power) animatedPrimaryColor else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Discrete Tactile [-] and [+] Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileStepButton(
                symbol = "−",
                enabled = enabled && acState.targetTemperature > minTemp,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTemperatureChange(acState.targetTemperature - 1)
                }
            )

            TactileStepButton(
                symbol = "+",
                enabled = enabled && acState.targetTemperature < maxTemp,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTemperatureChange(acState.targetTemperature + 1)
                }
            )
        }
    }
}

@Composable
private fun TactileStepButton(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(if (enabled) SurfaceGlassElevated else SurfaceGlass)
            .border(
                1.dp,
                if (enabled) BorderGlassHighlight else BorderGlass,
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            color = if (enabled) TextPrimary else TextMuted
        )
    }
}
