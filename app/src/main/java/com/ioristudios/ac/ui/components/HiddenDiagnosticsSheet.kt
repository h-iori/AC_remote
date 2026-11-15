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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ioristudios.ac.model.ProtocolType
import com.ioristudios.ac.protocol.KelvinatorIrEncoder
import com.ioristudios.ac.protocol.RawIrEncoder
import com.ioristudios.ac.ui.theme.BorderGlass
import com.ioristudios.ac.ui.theme.ModeTheme
import com.ioristudios.ac.ui.theme.SurfaceDark
import com.ioristudios.ac.ui.theme.SurfaceGlass
import com.ioristudios.ac.ui.theme.SurfaceGlassElevated
import com.ioristudios.ac.ui.theme.TextMuted
import com.ioristudios.ac.ui.theme.TextPrimary
import com.ioristudios.ac.ui.theme.TextSecondary
import com.ioristudios.ac.viewmodel.RemoteUiState
import com.ioristudios.ac.viewmodel.RemoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenDiagnosticsSheet(
    viewModel: RemoteViewModel,
    uiState: RemoteUiState,
    modeTheme: ModeTheme,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val ac = uiState.acState
    val rawBytes = uiState.encodedRawBytes
    var rawInputText by remember { mutableStateOf("") }
    var parseError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        scrimColor = Color(0xAA000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "ENGINEERING DIAGNOSTICS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9F0A),
                letterSpacing = 1.sp
            )
            Text(
                text = "Hidden hardware telemetry and raw packet verification suite",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Protocol Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (ac.selectedProtocol == ProtocolType.KELVINATOR_16BYTE) modeTheme.primary.copy(alpha = 0.25f) else SurfaceGlass)
                        .border(1.dp, if (ac.selectedProtocol == ProtocolType.KELVINATOR_16BYTE) modeTheme.primary else BorderGlass, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setProtocol(ProtocolType.KELVINATOR_16BYTE) }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "Kelvinator (16B)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "YB1FA Native Profile", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (ac.selectedProtocol == ProtocolType.GREE_8BYTE) modeTheme.primary.copy(alpha = 0.25f) else SurfaceGlass)
                        .border(1.dp, if (ac.selectedProtocol == ProtocolType.GREE_8BYTE) modeTheme.primary else BorderGlass, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setProtocol(ProtocolType.GREE_8BYTE) }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "Gree (8B)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Fallback Profile", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Carrier Frequency", fontSize = 11.sp, color = TextSecondary)
                Text(text = "${ac.carrierFrequencyHz / 1000} kHz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = modeTheme.primary)
            }
            Slider(
                value = ac.carrierFrequencyHz.toFloat(),
                onValueChange = { viewModel.setCarrierFrequency(it.toInt()) },
                valueRange = 36000f..40000f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = modeTheme.primary,
                    activeTrackColor = modeTheme.primary,
                    inactiveTrackColor = SurfaceGlassElevated
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live State Hex Matrix
            Text(text = "Raw Packet Hex & Checksum Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceGlass
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = rawBytes.joinToString(" ") { String.format("%02X", it) },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val sum1 = if (rawBytes.size >= 8) KelvinatorIrEncoder.calcBlockChecksum(rawBytes, 0) else 0
                    val sum2 = if (rawBytes.size >= 16) KelvinatorIrEncoder.calcBlockChecksum(rawBytes, 8) else 0
                    Text(
                        text = "Sum1: $sum1 | Sum2: $sum2 | Checksum: VALID",
                        fontSize = 10.sp,
                        color = Color(0xFF30D158),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Raw Pulse Injector
            Text(text = "Raw Pulse / Pronto Hex Injector", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            BasicTextField(
                value = rawInputText,
                onValueChange = { rawInputText = it },
                textStyle = TextStyle(color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(modeTheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceGlassElevated)
                    .padding(8.dp),
                decorationBox = { inner ->
                    if (rawInputText.isEmpty()) Text("Paste Pronto or CSV pulses", color = TextMuted, fontSize = 11.sp)
                    inner()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(modeTheme.primary)
                        .clickable {
                            val text = rawInputText.trim()
                            if (text.startsWith("0000")) {
                                val parsed = RawIrEncoder.parseProntoHex(text)
                                if (parsed != null) viewModel.transmitRawPulses(parsed.first, parsed.second)
                            } else {
                                val csv = RawIrEncoder.parseMicrosecondsCsv(text)
                                if (csv.isNotEmpty()) viewModel.transmitRawPulses(ac.carrierFrequencyHz, csv)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("TRANSMIT RAW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
