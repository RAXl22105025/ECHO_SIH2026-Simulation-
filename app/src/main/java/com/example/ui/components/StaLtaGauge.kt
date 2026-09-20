package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StaLtaGauge(
    currentRatio: Float,
    threshold: Float,
    isTriggered: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "strobe")
    val strobeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(
                1.dp,
                if (isTriggered) RoseAlert.copy(alpha = strobeAlpha) else TacticalBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .testTag("sta_lta_gauge_panel")
    ) {
        // Gauge Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "STA/LTA Ratio Gauge",
                    tint = if (isTriggered) RoseAlert else AmberWarning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STA/LTA TRIGGER RATIO",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isTriggered) RoseGlow else TacticalDarkBg)
                    .border(
                        1.dp,
                        if (isTriggered) RoseAlert else EmeraldTelemetry.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isTriggered) "TRIGGER ARMED [> $threshold]" else "STANDBY [< $threshold]",
                    color = if (isTriggered) RoseAlert else EmeraldTelemetry,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ratio Arc / Gauge Presentation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arc Display
            Box(
                modifier = Modifier
                    .size(110.dp, 75.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp, 65.dp)) {
                    val strokeW = 9.dp.toPx()
                    val diameter = size.width - strokeW
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset(strokeW / 2, strokeW / 2)

                    // Track arc (180 degrees from -180 to 0)
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Threshold marker arc
                    val maxRatio = 8.0f
                    val sweep = ((currentRatio / maxRatio).coerceIn(0f, 1f)) * 180f

                    val arcBrush = Brush.horizontalGradient(
                        colors = listOf(
                            EmeraldTelemetry,
                            AmberWarning,
                            RoseAlert
                        )
                    )

                    drawArc(
                        brush = arcBrush,
                        startAngle = 180f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Draw Threshold Tick mark
                    val threshAngle = 180f + ((threshold / maxRatio).coerceIn(0f, 1f)) * 180f
                    val rad = Math.toRadians(threshAngle.toDouble())
                    val cx = size.width / 2
                    val cy = (size.height + strokeW / 2)
                    val r1 = diameter / 2 - strokeW / 2
                    val r2 = diameter / 2 + strokeW / 2
                    val x1 = cx + (r1 * kotlin.math.cos(rad)).toFloat()
                    val y1 = cy + (r1 * kotlin.math.sin(rad)).toFloat()
                    val x2 = cx + (r2 * kotlin.math.cos(rad)).toFloat()
                    val y2 = cy + (r2 * kotlin.math.sin(rad)).toFloat()

                    drawLine(
                        color = Color.White,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 18.dp)
                ) {
                    Text(
                        text = String.format("%.2f", currentRatio),
                        color = if (isTriggered) RoseAlert else TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "RATIO",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Textual parameters Breakdown
            Column(modifier = Modifier.weight(1f)) {
                RatioInfoRow(label = "Short-Term Avg (STA)", value = "150 ms window (~15 Sa)")
                Spacer(modifier = Modifier.height(3.dp))
                RatioInfoRow(label = "Long-Term Avg (LTA)", value = "1.5 s baseline (~150 Sa)")
                Spacer(modifier = Modifier.height(3.dp))
                RatioInfoRow(
                    label = "Detection Threshold",
                    value = String.format("%.1f (User Defined)", threshold),
                    accentColor = AmberWarning
                )
                Spacer(modifier = Modifier.height(3.dp))
                RatioInfoRow(
                    label = "Threshold Delta",
                    value = if (currentRatio >= threshold)
                        String.format("+%.2f (EXCEEDED)", currentRatio - threshold)
                    else String.format("-%.2f (SAFE)", threshold - currentRatio),
                    accentColor = if (currentRatio >= threshold) RoseAlert else EmeraldTelemetry
                )
            }
        }
    }
}

@Composable
fun RatioInfoRow(
    label: String,
    value: String,
    accentColor: Color = TextSecondary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp
        )
        Text(
            text = value,
            color = accentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
