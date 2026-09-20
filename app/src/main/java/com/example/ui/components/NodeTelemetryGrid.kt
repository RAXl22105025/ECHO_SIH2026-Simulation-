package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NodeTelemetry
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueMems
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalBorderBright
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NodeTelemetryGrid(
    nodes: List<NodeTelemetry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // Grid Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "Sensor Node Telemetry",
                    tint = CyanTelemetry,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MULTI-NODE TELEMETRY GRID",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = "ARRAY APERTURE: 15.8 KM",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal scrollable node cards
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            nodes.forEach { node ->
                NodeCard(node = node)
            }
        }
    }
}

@Composable
fun NodeCard(
    node: NodeTelemetry,
    modifier: Modifier = Modifier
) {
    val isAlert = node.isWaveArrived
    val cardBorder = if (isAlert) RoseAlert else TacticalBorderBright
    val cardBg = if (isAlert) TacticalSurfaceElevated else TacticalDarkBg

    Column(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("node_card_${node.id.lowercase()}")
    ) {
        // Top Header of Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isAlert) RoseAlert else EmeraldTelemetry)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = node.name.uppercase(),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Wave arrival status tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isAlert) RoseGlow else EmeraldGlow)
                    .border(
                        1.dp,
                        if (isAlert) RoseAlert else EmeraldTelemetry.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isAlert) "WAVE ARRIVED" else "MONITORING",
                    color = if (isAlert) RoseAlert else EmeraldTelemetry,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Coordinates & Array distance
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = String.format("GEO: %.4f°N, %.4f°E | d=%.1f km", node.lat, node.lon, node.distanceToSourceKm),
            color = TextMuted,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Health Indicators Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalSurface)
                .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            // Voltage (LiFePO4 3.2 - 3.4V)
            val voltNorm = ((node.voltage - 3.0f) / (3.45f - 3.0f)).coerceIn(0f, 1f)
            HealthMetricRow(
                icon = Icons.Default.BatteryChargingFull,
                label = "LiFePO4 Cell",
                value = String.format("%.2f V", node.voltage),
                unit = "(92% SoC)",
                progress = voltNorm,
                progressColor = EmeraldTelemetry
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Solar Harvest (W)
            val solarNorm = (node.solarWatts / 6.0f).coerceIn(0f, 1f)
            HealthMetricRow(
                icon = Icons.Default.SolarPower,
                label = "Solar Harvest",
                value = String.format("%.1f W", node.solarWatts),
                unit = "MPPT Active",
                progress = solarNorm,
                progressColor = AmberWarning
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Core Temp (°C) and SD Fill (%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeviceThermostat,
                        contentDescription = "Core Temp",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format("Temp: %.1f°C", node.coreTempC),
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SdCard,
                        contentDescription = "Buffer SD",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format("SD: %.1f%%", node.sdFillPercent),
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transducer Status: Tri-sensor MEMS differential coherence index (0.00 - 1.00)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalSurface)
                .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "MEMS Coherence",
                        tint = BlueMems,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MEMS Differential Coherence",
                        color = TextSecondary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = String.format("%.2f", node.coherenceIndex),
                    color = if (node.coherenceIndex > 0.90f) CyanTelemetry else AmberWarning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { node.coherenceIndex },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = CyanTelemetry,
                trackColor = TacticalBorder,
                strokeCap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Uplink: Primary LoRa Mesh RSSI/SNR with automatic GSM/Sat fallback status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalSurface)
                .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "LoRa Uplink",
                    tint = EmeraldTelemetry,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = node.uplinkStatus,
                        color = EmeraldTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "GSM/Sat Auto-Fallback Standby",
                        color = TextMuted,
                        fontSize = 8.5.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${node.loraRssi} dBm",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SNR +${String.format("%.1f", node.loraSnr)} dB",
                    color = TextSecondary,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Current Local Pressure Reading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format("Raw Patm: %+.2f Pa", node.rawPressurePa),
                color = RoseAlert.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("Sig: %+.1f mPa", node.filteredPressureMpa),
                color = CyanTelemetry,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun HealthMetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    progress: Float,
    progressColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 9.5.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = TextMuted,
                    fontSize = 8.5.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = progressColor,
            trackColor = TacticalBorder,
            strokeCap = StrokeCap.Round
        )
    }
}
