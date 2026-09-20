package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PsdBin
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.PurpleSpectrum
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.log10

@Composable
fun WelchPsdView(
    psdBins: List<PsdBin>,
    modifier: Modifier = Modifier
) {
    val peakBin = psdBins.maxByOrNull { it.powerDb }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // PSD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Welch PSD",
                    tint = CyanTelemetry,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WELCH PSD POWER SPECTRUM",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }

            // Peak Frequency Badge
            if (peakBin != null && peakBin.powerDb > -10.0f) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyanGlow)
                        .border(1.dp, CyanTelemetry, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = peakBin.label ?: String.format("PEAK: %.2f Hz (+%.1f dB)", peakBin.freqHz, peakBin.powerDb),
                        color = CyanTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Text(
                    text = "PASSBAND: 0.01 - 20.0 Hz",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Spectrum Plot Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalDarkBg)
                .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("welch_psd_canvas")
            ) {
                val width = size.width
                val height = size.height

                if (psdBins.isEmpty()) return@Canvas

                // dB range: -35 dB to +45 dB
                val minDb = -35.0f
                val maxDb = 45.0f
                val dbRange = maxDb - minDb

                // Draw Horizontal dB Reference Grids
                val dbLevels = listOf(-20f, 0f, 20f, 40f)
                for (db in dbLevels) {
                    val y = height - ((db - minDb) / dbRange) * height
                    drawLine(
                        color = Color(0x1A00F2FE),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 0.8f
                    )
                }

                // Draw spectrum bars / area
                val count = psdBins.size
                val barSpacing = 2.dp.toPx()
                val barWidth = (width - (count - 1) * barSpacing) / count

                val curvePath = Path()

                psdBins.forEachIndexed { i, bin ->
                    val normVal = ((bin.powerDb - minDb) / dbRange).coerceIn(0.02f, 1f)
                    val barHeight = normVal * height
                    val x = i * (barWidth + barSpacing)
                    val y = height - barHeight

                    val barColor = when {
                        bin.isPeak -> CyanTelemetry
                        bin.powerDb > 10.0f -> PurpleSpectrum
                        bin.powerDb > -5.0f -> EmeraldTelemetry
                        else -> Color(0xFF1E293B)
                    }

                    // Render Spectral Bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(barColor, barColor.copy(alpha = 0.35f)),
                            startY = y,
                            endY = height
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Track curve
                    val centerX = x + barWidth / 2.0f
                    if (i == 0) curvePath.moveTo(centerX, y) else curvePath.lineTo(centerX, y)
                }

                // Draw smooth top line
                drawPath(
                    path = curvePath,
                    color = CyanTelemetry.copy(alpha = 0.85f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Frequency ticks label at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.01 Hz", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("0.1 Hz", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("1.0 Hz", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("5.0 Hz", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("10 Hz", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("20 Hz", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
