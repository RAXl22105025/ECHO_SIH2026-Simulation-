package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EventType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.PurpleSpectrum
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalBorderBright
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EventInjectorBar(
    currentEvent: EventType,
    onInjectEvent: (EventType) -> Unit,
    staLtaThreshold: Float,
    onThresholdChange: (Float) -> Unit,
    windNoiseGain: Float,
    onWindNoiseChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTuners by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CrisisAlert,
                    contentDescription = "Event Injector",
                    tint = RoseAlert,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GLOBAL EVENT INJECTOR",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[${currentEvent.agency}]",
                    color = if (currentEvent == EventType.AMBIENT_BASELINE) TextMuted else CyanTelemetry,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Tuners toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (showTuners) CyanTelemetry.copy(alpha = 0.2f) else TacticalDarkBg)
                    .border(1.dp, if (showTuners) CyanTelemetry else TacticalBorder, RoundedCornerShape(4.dp))
                    .clickable { showTuners = !showTuners }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("toggle_tuners_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tune Parameters",
                        tint = if (showTuners) CyanTelemetry else TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showTuners) "HIDE TUNING" else "TUNING PARAMS",
                        color = if (showTuners) CyanTelemetry else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal list of event injector buttons
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventButton(
                title = "Rocket Launch",
                agency = "ISRO / DRDO",
                band = "0.5–4 Hz Chirp",
                icon = Icons.Default.RocketLaunch,
                accentColor = CyanTelemetry,
                isSelected = currentEvent == EventType.ROCKET_LAUNCH,
                onClick = { onInjectEvent(EventType.ROCKET_LAUNCH) },
                testTag = "inject_rocket_button"
            )

            EventButton(
                title = "Surface Explosion",
                agency = "NTRO",
                band = "1.2 Hz Shock Blast",
                icon = Icons.Default.Bolt,
                accentColor = RoseAlert,
                isSelected = currentEvent == EventType.SURFACE_EXPLOSION,
                onClick = { onInjectEvent(EventType.SURFACE_EXPLOSION) },
                testTag = "inject_explosion_button"
            )

            EventButton(
                title = "Storm Front",
                agency = "IMD",
                band = "0.1–0.3 Hz Microbarom",
                icon = Icons.Default.Air,
                accentColor = AmberWarning,
                isSelected = currentEvent == EventType.STORM_FRONT,
                onClick = { onInjectEvent(EventType.STORM_FRONT) },
                testTag = "inject_storm_button"
            )

            EventButton(
                title = "Landslide / Avalanche",
                agency = "NDMA",
                band = "1–8 Hz Rumble",
                icon = Icons.Default.Landscape,
                accentColor = PurpleSpectrum,
                isSelected = currentEvent == EventType.LANDSLIDE,
                onClick = { onInjectEvent(EventType.LANDSLIDE) },
                testTag = "inject_landslide_button"
            )

            EventButton(
                title = "Ambient Baseline",
                agency = "BASELINE",
                band = "Pink Noise <5 mPa",
                icon = Icons.Default.Waves,
                accentColor = EmeraldTelemetry,
                isSelected = currentEvent == EventType.AMBIENT_BASELINE,
                onClick = { onInjectEvent(EventType.AMBIENT_BASELINE) },
                testTag = "inject_baseline_button"
            )
        }

        // Expanded tuning panel
        if (showTuners) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TacticalDarkBg)
                    .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                // STA/LTA Threshold Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STA/LTA TRIGGER THRESHOLD",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%.1f", staLtaThreshold),
                        color = AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = staLtaThreshold,
                    onValueChange = onThresholdChange,
                    valueRange = 2.0f..7.0f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberWarning,
                        activeTrackColor = AmberWarning,
                        inactiveTrackColor = TacticalBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("sta_lta_threshold_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Wind Turbulence Gain Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WIND TURBULENCE NOISE GAIN",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%.2f× (±%.1f Pa)", windNoiseGain, 2.5f * windNoiseGain),
                        color = RoseAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = windNoiseGain,
                    onValueChange = onWindNoiseChange,
                    valueRange = 0.2f..2.5f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = RoseAlert,
                        activeTrackColor = RoseAlert,
                        inactiveTrackColor = TacticalBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("wind_gain_slider")
                )
            }
        }
    }
}

@Composable
fun EventButton(
    title: String,
    agency: String,
    band: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = if (isSelected) accentColor.copy(alpha = 0.18f) else TacticalDarkBg
    val border = if (isSelected) accentColor else TacticalBorder

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = if (isSelected) accentColor else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• $agency",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = band,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
