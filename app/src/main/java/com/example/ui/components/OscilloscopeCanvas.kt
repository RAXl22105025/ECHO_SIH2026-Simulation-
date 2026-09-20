package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

enum class OscilloscopeDisplayMode {
    DUAL,
    RAW_ONLY,
    FILTERED_ONLY
}

@Composable
fun OscilloscopeCanvas(
    rawWaveform: FloatArray,
    filteredWaveform: FloatArray,
    isTriggered: Boolean,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(OscilloscopeDisplayMode.DUAL) }

    val latestRaw = if (rawWaveform.isNotEmpty()) rawWaveform.last() else 0f
    val latestFiltered = if (filteredWaveform.isNotEmpty()) filteredWaveform.last() else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // Top Oscilloscope Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Oscilloscope",
                    tint = CyanTelemetry,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DUAL-TRACE INFRASOUND OSCILLOSCOPE",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }

            // Channel Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChannelChip(
                    title = "DUAL",
                    isSelected = displayMode == OscilloscopeDisplayMode.DUAL,
                    color = CyanTelemetry,
                    onClick = { displayMode = OscilloscopeDisplayMode.DUAL },
                    testTag = "scope_mode_dual"
                )
                ChannelChip(
                    title = "CH1 RAW",
                    isSelected = displayMode == OscilloscopeDisplayMode.RAW_ONLY,
                    color = RoseAlert,
                    onClick = { displayMode = OscilloscopeDisplayMode.RAW_ONLY },
                    testTag = "scope_mode_raw"
                )
                ChannelChip(
                    title = "CH2 FILT",
                    isSelected = displayMode == OscilloscopeDisplayMode.FILTERED_ONLY,
                    color = CyanTelemetry,
                    onClick = { displayMode = OscilloscopeDisplayMode.FILTERED_ONLY },
                    testTag = "scope_mode_filt"
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Telemetry readout bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(TacticalDarkBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(RoseAlert)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = String.format("CH1 RAW (Patm): %+.2f Pa", latestRaw),
                    color = RoseAlert,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CyanTelemetry)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = String.format("CH2 COHERENT (Pinf): %+.1f mPa", latestFiltered),
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "100 Sa/s | 200 ms/DIV",
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Phosphor CRT Scope Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF040810))
                .border(1.dp, if (isTriggered) RoseAlert else Color(0xFF1E293B), RoundedCornerShape(6.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("oscilloscope_canvas")
            ) {
                val width = size.width
                val height = size.height
                val midY = height / 2.0f

                // 1. Draw Grid Lines (8 horizontal divisions, 6 vertical divisions)
                val gridColor = Color(0x1A00F2FE)
                val majorAxisColor = Color(0x3300F2FE)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                // Vertical Grid Lines
                val xDivisions = 10
                for (i in 0..xDivisions) {
                    val x = (width / xDivisions) * i
                    drawLine(
                        color = if (i == xDivisions / 2) majorAxisColor else gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = if (i == xDivisions / 2) 1.2f else 0.8f,
                        pathEffect = dashEffect
                    )
                }

                // Horizontal Grid Lines
                val yDivisions = 6
                for (i in 0..yDivisions) {
                    val y = (height / yDivisions) * i
                    drawLine(
                        color = if (i == yDivisions / 2) majorAxisColor else gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = if (i == yDivisions / 2) 1.5f else 0.8f,
                        pathEffect = dashEffect
                    )
                }

                // 2. Draw Trace 1: Raw Pressure (Wind Turbulence swings ±2.5 Pa)
                if (displayMode == OscilloscopeDisplayMode.DUAL || displayMode == OscilloscopeDisplayMode.RAW_ONLY) {
                    val maxPa = 3.0f // ±3 Pa scale
                    val rawPath = Path()
                    val count = rawWaveform.size
                    if (count > 1) {
                        for (i in 0 until count) {
                            val x = (i.toFloat() / (count - 1)) * width
                            // Map -maxPa..+maxPa to height..0
                            val normY = (rawWaveform[i] / maxPa).coerceIn(-1f, 1f)
                            val y = midY - (normY * (height * 0.42f))

                            if (i == 0) rawPath.moveTo(x, y) else rawPath.lineTo(x, y)
                        }

                        // Glow layer
                        drawPath(
                            path = rawPath,
                            color = RoseGlow,
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                        // Core trace
                        drawPath(
                            path = rawPath,
                            color = RoseAlert,
                            style = Stroke(width = 1.6f, cap = StrokeCap.Round)
                        )
                    }
                }

                // 3. Draw Trace 2: Filtered Coherent Infrasound Signal (Millipascal scale ±50 mPa)
                if (displayMode == OscilloscopeDisplayMode.DUAL || displayMode == OscilloscopeDisplayMode.FILTERED_ONLY) {
                    val maxMpa = 60.0f // ±60 mPa scale
                    val filtPath = Path()
                    val count = filteredWaveform.size
                    if (count > 1) {
                        for (i in 0 until count) {
                            val x = (i.toFloat() / (count - 1)) * width
                            // Map -maxMpa..+maxMpa to height..0
                            val normY = (filteredWaveform[i] / maxMpa).coerceIn(-1f, 1f)
                            val y = midY - (normY * (height * 0.42f))

                            if (i == 0) filtPath.moveTo(x, y) else filtPath.lineTo(x, y)
                        }

                        // Glow layer
                        drawPath(
                            path = filtPath,
                            color = CyanGlow,
                            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                        )
                        // Core trace
                        drawPath(
                            path = filtPath,
                            color = CyanTelemetry,
                            style = Stroke(width = 2.0f, cap = StrokeCap.Round)
                        )
                    }
                }

                // Zero-Level Reference Indicator on left edge
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(0f, midY),
                    end = Offset(10f, midY),
                    strokeWidth = 2f
                )
            }

            // CRT corner labels
            Text(
                text = "+2.5 Pa / +50 mPa",
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )

            Text(
                text = "-2.5 Pa / -50 mPa",
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            )

            if (isTriggered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(RoseAlert.copy(alpha = 0.25f))
                        .border(1.dp, RoseAlert, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "STA/LTA TRIGGER DETECTED",
                        color = RoseAlert,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelChip(
    title: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else TacticalDarkBg)
            .border(1.dp, if (isSelected) color else TacticalBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Text(
            text = title,
            color = if (isSelected) color else TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
