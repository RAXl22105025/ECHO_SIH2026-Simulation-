package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TacticalTopBar(
    isSimulationRunning: Boolean,
    onToggleSimulation: () -> Unit,
    onResetSimulation: () -> Unit,
    simulationSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(TacticalSurfaceElevated, TacticalSurface)
                )
            )
            .border(1.dp, TacticalBorder, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(12.dp)
    ) {
        // Main Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TacticalDarkBg)
                        .border(1.dp, CyanTelemetry, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Project ECHO Infrasound Radar",
                        tint = CyanTelemetry,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PROJECT ECHO",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " // ARRAY LOCALIZER",
                            color = CyanTelemetry,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Atmospheric Pressure Monitoring & TDOA Triangulation",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Play / Pause / Reset Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Speed pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TacticalDarkBg)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (simulationSpeed == 1.0f) "1X" else "2X",
                            color = CyanTelemetry,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = { onSpeedChange(if (simulationSpeed == 1.0f) 2.0f else 1.0f) },
                    modifier = Modifier.testTag("speed_toggle_button")
                ) {
                    Text(
                        text = "SPD",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onToggleSimulation,
                    modifier = Modifier.testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isSimulationRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isSimulationRunning) "Pause Simulation" else "Play Simulation",
                        tint = if (isSimulationRunning) EmeraldTelemetry else RoseAlert
                    )
                }

                IconButton(
                    onClick = onResetSimulation,
                    modifier = Modifier.testTag("reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Simulation",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Metadata Badges Row (SIH26144, Status, GPS-Disciplined PPS, LoRa Mesh)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // SIH26144 Badge
            TacticalChip(
                label = "SIH26144",
                textColor = CyanTelemetry,
                borderColor = CyanTelemetry.copy(alpha = 0.6f),
                bgColor = CyanGlow
            )

            // Live Stream Status
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmeraldGlow)
                    .border(1.dp, EmeraldTelemetry.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSimulationRunning)
                                    EmeraldTelemetry.copy(alpha = pulseAlpha)
                                else RoseAlert
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isSimulationRunning) "SIMULATED LIVE STREAM" else "STREAM PAUSED",
                        color = if (isSimulationRunning) EmeraldTelemetry else RoseAlert,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // GPS PPS Lock
            TacticalChip(
                label = "GPS PPS LOCK: ±0.4 µs",
                textColor = TextPrimary,
                borderColor = TacticalBorder,
                bgColor = TacticalDarkBg
            )

            // LoRa Mesh
            TacticalChip(
                label = "MESH: LoRa Multi-Hop ACTIVE",
                textColor = EmeraldTelemetry,
                borderColor = EmeraldTelemetry.copy(alpha = 0.4f),
                bgColor = TacticalDarkBg
            )
        }
    }
}

@Composable
fun TacticalChip(
    label: String,
    textColor: Color,
    borderColor: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
