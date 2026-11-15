package com.ioristudios.ac.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ioristudios.ac.model.SmartPreset
import com.ioristudios.ac.model.SmartPresetRegistry
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.BorderGlassHighlight
import com.ioristudios.ac.ui.theme.ModeTheme
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary

@Composable
fun SmartPresetsRow(
    isPoweredOn: Boolean,
    modeTheme: ModeTheme,
    onApplyPreset: (SmartPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SMART PRESETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartPresetRegistry.presets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
                        .clickable(enabled = isPoweredOn) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onApplyPreset(preset)
                        }
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = preset.iconEmoji,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = preset.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPoweredOn) TextPrimary else TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = preset.description,
                            fontSize = 10.sp,
                            color = if (isPoweredOn) TextSecondary else TextMuted,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}
