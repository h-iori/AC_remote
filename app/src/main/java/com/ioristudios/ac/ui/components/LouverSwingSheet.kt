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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.ioristudios.ac.model.SwingVMode
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.BorderGlassHighlight
import com.ioristudios.ac.ui.theme.ModeTheme
import com.ioristudios.ac.ui.theme.OledBlack
import com.ioristudios.ac.ui.theme.SurfaceDark
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.SurfaceGlassElevated
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LouverSwingSheet(
    currentSwingV: SwingVMode,
    isSwingHOn: Boolean,
    modeTheme: ModeTheme,
    onSwingVChange: (SwingVMode) -> Unit,
    onSwingHToggle: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "AIRFLOW DIRECTION & LOUVERS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextPrimary
            )
            Text(
                text = "Configure vertical vane angles and horizontal oscillation sweep",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Horizontal Swing Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceGlass)
                    .border(1.dp, if (isSwingHOn) modeTheme.primary else BorderGlass, RoundedCornerShape(18.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSwingHToggle()
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Horizontal Swing",
                        tint = if (isSwingHOn) modeTheme.primary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Horizontal Louver Sweep",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isSwingHOn) "Oscillating left to right" else "Stationary center flow",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSwingHOn) modeTheme.primary.copy(alpha = 0.25f) else SurfaceGlassElevated)
                        .border(1.dp, if (isSwingHOn) modeTheme.primary else BorderGlass, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSwingHOn) "ACTIVE" else "OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSwingHOn) modeTheme.primary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vertical Swing Positions
            Text(
                text = "VERTICAL VANE ANGLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val verticalModes = listOf(
                Pair(SwingVMode.AUTO, "Full Continuous Sweep"),
                Pair(SwingVMode.HIGHEST, "Highest (Ceiling Flow)"),
                Pair(SwingVMode.UPPER_MIDDLE, "Upper-Middle"),
                Pair(SwingVMode.MIDDLE, "Center Level"),
                Pair(SwingVMode.LOWER_MIDDLE, "Lower-Middle"),
                Pair(SwingVMode.LOWEST, "Lowest (Floor Flow)"),
                Pair(SwingVMode.OFF, "Fixed / Last Position")
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                verticalModes.forEach { (mode, description) ->
                    val isSelected = currentSwingV == mode

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) modeTheme.primary.copy(alpha = 0.2f) else SurfaceGlass)
                            .border(
                                1.dp,
                                if (isSelected) modeTheme.primary else BorderGlass,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwingVChange(mode)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mode.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                            Text(
                                text = description,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        if (isSelected) {
                            Text(
                                text = "✓",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = modeTheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
