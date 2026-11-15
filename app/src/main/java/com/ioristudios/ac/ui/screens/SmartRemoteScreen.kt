package com.ioristudios.ac.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ioristudios.ac.hardware.IrTransmissionStatus
import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.ui.components.CircularTemperatureDial
import com.ioristudios.ac.ui.components.FanSpeedStepper
import com.ioristudios.ac.ui.components.HiddenDiagnosticsSheet
import com.ioristudios.ac.ui.components.LouverSwingSheet
import com.ioristudios.ac.ui.components.ModeSelectorBar
import com.ioristudios.ac.ui.components.SmartPresetsRow
import com.ioristudios.ac.ui.components.TimerBottomSheet
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.OledBlack
import com.ioristudios.ac.ui.theme.SurfaceDark
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.SurfaceGlassElevated
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary
import com.ioristudios.ac.ui.theme.getThemeForMode
import com.ioristudios.ac.viewmodel.RemoteUiState
import com.ioristudios.ac.viewmodel.RemoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartRemoteScreen(
    viewModel: RemoteViewModel,
    uiState: RemoteUiState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val ac = uiState.acState
    val modeTheme = getThemeForMode(ac.mode)

    val animatedAuraColor by animateColorAsState(
        targetValue = if (ac.power) modeTheme.aura else Color.Transparent,
        animationSpec = tween(durationMillis = 400)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Dynamic Ambient Light Aura in the background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(animatedAuraColor, Color.Transparent),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- STICKY TOP APP BAR (Pinned, never scrolls off) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = OledBlack.copy(alpha = 0.90f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: App Title & Status (5-tap secret easter egg for diagnostics)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.onTitleTapped() }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "AC REMOTE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp,
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isHardwareAvailable) Color(0xFF30D158) else Color(0xFFFF453A))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (uiState.isHardwareAvailable) "IR READY" else "NO IR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.isHardwareAvailable) TextSecondary else Color(0xFFFF453A)
                                )
                                Text(
                                    text = " • VIDEOCON VITA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = TextMuted
                                )
                            }
                        }

                        // Right: Actions (Timer pill + Sticky Power Button)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Timer Shortcut Pill
                            val isTimerActive = ac.timerRemainingSeconds > 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isTimerActive) modeTheme.primary.copy(alpha = 0.22f) else SurfaceGlass)
                                    .border(1.dp, if (isTimerActive) modeTheme.primary else BorderGlass, RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setTimerSheetVisible(true)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Timer",
                                        tint = if (isTimerActive) modeTheme.primary else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val timerText = if (isTimerActive) {
                                        val mins = (ac.timerRemainingSeconds + 59) / 60
                                        "${mins}m"
                                    } else {
                                        "TIMER"
                                    }
                                    Text(
                                        text = timerText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTimerActive) modeTheme.primary else TextSecondary
                                    )
                                }
                            }

                            // --- STICKY POWER ON/OFF BUTTON ---
                            val powerColor = if (ac.power) Color(0xFF30D158) else Color(0xFFFF453A)
                            val powerBg = if (ac.power) Color(0x3330D158) else Color(0x28FF453A)
                            val powerBorder = if (ac.power) Color(0xFF30D158) else Color(0xFFFF453A).copy(alpha = 0.7f)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(powerBg)
                                    .border(1.5.dp, powerBorder, RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.togglePower()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Power Toggle",
                                        tint = powerColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (ac.power) "ON" else "OFF",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = powerColor
                                    )
                                }
                            }
                        }
                    }

                    // Transmission Pulse Indicator (directly under sticky header)
                    AnimatedVisibility(
                        visible = uiState.transmissionStatus is IrTransmissionStatus.Transmitting,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(modeTheme.primary.copy(alpha = 0.22f))
                                .border(1.dp, modeTheme.primary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = modeTheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TRANSMITTING 38kHz SIGNAL...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeTheme.primary
                            )
                        }
                    }
                }
            }

            // Hairline separator under sticky top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(BorderGlass)
            )

            // --- SCROLLABLE REMOTE CONTROLS BODY ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // --- Hero: Circular Temperature Dial ---
                CircularTemperatureDial(
                    acState = ac,
                    modeTheme = modeTheme,
                    onTemperatureChange = { newTemp ->
                        viewModel.setTemperature(newTemp)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- Segmented Mode Bar ---
                ModeSelectorBar(
                    currentMode = ac.mode,
                    isPoweredOn = ac.power,
                    onModeSelect = { newMode ->
                        viewModel.setMode(newMode)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 5-Stage Stepped Fan Speed Bar ---
                FanSpeedStepper(
                    currentSpeed = ac.fanSpeed,
                    modeTheme = modeTheme,
                    isPoweredOn = ac.power,
                    onSpeedChange = { newSpeed ->
                        viewModel.setFanSpeed(newSpeed)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Louver & Airflow Card ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                        .clickable(enabled = ac.power) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setLouverSheetVisible(true)
                        },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceGlassElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Louver",
                                    tint = if (ac.power) modeTheme.primary else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LOUVER & AIRFLOW DIRECTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ac.power) TextPrimary else TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ac.swingVertical.label} • ${if (ac.swingHorizontal) "Horizontal Sweep ON" else "Fixed"}",
                                    fontSize = 12.sp,
                                    color = if (ac.power) modeTheme.primary else TextMuted
                                )
                            }
                        }

                        Text(
                            text = "CUSTOMIZE ›",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ac.power) modeTheme.primary else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Smart Comfort Presets ---
                SmartPresetsRow(
                    isPoweredOn = ac.power,
                    modeTheme = modeTheme,
                    onApplyPreset = { preset ->
                        viewModel.applyPreset(preset)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Comfort Toggles (Turbo, Light, X-Fan, Quiet) ---
                Text(
                    text = "COMFORT FEATURES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeaturePill(
                        label = "TURBO",
                        isActive = ac.turbo && ac.power,
                        enabled = ac.power && ac.mode != AcMode.DRY && ac.mode != AcMode.AUTO,
                        accentColor = modeTheme.primary,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleTurbo()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePill(
                        label = "LIGHT",
                        isActive = ac.light && ac.power,
                        enabled = ac.power,
                        accentColor = modeTheme.primary,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleLight()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePill(
                        label = "X-FAN",
                        isActive = ac.xFan && ac.power,
                        enabled = ac.power && (ac.mode == AcMode.COOL || ac.mode == AcMode.DRY),
                        accentColor = modeTheme.primary,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleXFan()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FeaturePill(
                        label = "QUIET",
                        isActive = ac.quiet && ac.power,
                        enabled = ac.power,
                        accentColor = modeTheme.primary,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleQuiet()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Subtle Footer Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIDEOCON VITA • MODEL YB1FA • CONSUMER IR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = TextMuted.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- Modals & Sheets ---
        if (uiState.isLouverSheetVisible) {
            LouverSwingSheet(
                currentSwingV = ac.swingVertical,
                isSwingHOn = ac.swingHorizontal,
                modeTheme = modeTheme,
                onSwingVChange = { newSwing ->
                    viewModel.setSwingVertical(newSwing)
                },
                onSwingHToggle = {
                    viewModel.toggleSwingHorizontal()
                },
                onDismiss = {
                    viewModel.setLouverSheetVisible(false)
                }
            )
        }

        if (uiState.isTimerSheetVisible) {
            TimerBottomSheet(
                remainingSeconds = ac.timerRemainingSeconds,
                modeTheme = modeTheme,
                onSetTimerMinutes = { minutes ->
                    viewModel.setTimerMinutes(minutes)
                },
                onCancelTimer = {
                    viewModel.cancelTimer()
                },
                onDismiss = {
                    viewModel.setTimerSheetVisible(false)
                }
            )
        }

        if (uiState.isHiddenDiagnosticsVisible) {
            HiddenDiagnosticsSheet(
                viewModel = viewModel,
                uiState = uiState,
                modeTheme = modeTheme,
                onDismiss = {
                    viewModel.setHiddenDiagnosticsVisible(false)
                }
            )
        }
    }
}

@Composable
private fun FeaturePill(
    label: String,
    isActive: Boolean,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) accentColor.copy(alpha = 0.22f) else SurfaceGlass)
            .border(
                1.dp,
                if (isActive) accentColor else BorderGlass,
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) accentColor else if (enabled) TextSecondary else TextMuted
        )
    }
}
