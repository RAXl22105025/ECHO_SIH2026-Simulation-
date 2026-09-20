package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AlertsLogTable
import com.example.ui.components.EventInjectorBar
import com.example.ui.components.NodeTelemetryGrid
import com.example.ui.components.OscilloscopeCanvas
import com.example.ui.components.StaLtaGauge
import com.example.ui.components.TacticalMapView
import com.example.ui.components.TacticalTopBar
import com.example.ui.components.WelchPsdView
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun ProjectEchoDashboard(
    viewModel: ProjectEchoViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val simulationSpeed by viewModel.simulationSpeed.collectAsStateWithLifecycle()
    val windNoiseGain by viewModel.windNoiseGain.collectAsStateWithLifecycle()
    val staLtaThreshold by viewModel.staLtaThreshold.collectAsStateWithLifecycle()
    val currentEvent by viewModel.currentEvent.collectAsStateWithLifecycle()
    val nodes by viewModel.nodes.collectAsStateWithLifecycle()
    val rawWaveform by viewModel.rawWaveform.collectAsStateWithLifecycle()
    val filteredWaveform by viewModel.filteredWaveform.collectAsStateWithLifecycle()
    val staLtaRatio by viewModel.staLtaRatio.collectAsStateWithLifecycle()
    val isTriggered by viewModel.isTriggered.collectAsStateWithLifecycle()
    val psdBins by viewModel.psdBins.collectAsStateWithLifecycle()
    val alertsLog by viewModel.alertsLog.collectAsStateWithLifecycle()
    val wavefrontRadius by viewModel.wavefrontRadius.collectAsStateWithLifecycle()
    val hyperbolicLines by viewModel.hyperbolicLines.collectAsStateWithLifecycle()
    val estimatedBearingDeg by viewModel.estimatedBearingDeg.collectAsStateWithLifecycle()
    val estimatedCepMeters by viewModel.estimatedCepMeters.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "ALL SYSTEMS" to Icons.Default.Dashboard,
        "OSCILLOSCOPE & PSD" to Icons.Default.Equalizer,
        "RADAR TDOA" to Icons.Default.Radar,
        "EVENT LOG" to Icons.Default.Security
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Tactical Topbar (SIH26144, Status, GPS-Disciplined PPS, LoRa Mesh, Controls)
        TacticalTopBar(
            isSimulationRunning = isRunning,
            onToggleSimulation = { viewModel.toggleSimulation() },
            onResetSimulation = { viewModel.resetSimulation() },
            simulationSpeed = simulationSpeed,
            onSpeedChange = { viewModel.setSimulationSpeed(it) }
        )

        // Module Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = TacticalSurface,
            contentColor = CyanTelemetry,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanTelemetry,
                        height = 2.dp
                    )
                }
            },
            divider = {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TacticalBorder)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        coroutineScope.launch {
                            val targetIndex = when (index) {
                                0 -> 0 // Top
                                1 -> 2 // Oscilloscope & PSD
                                2 -> 4 // Radar TDOA
                                3 -> 5 // Alerts
                                else -> 0
                            }
                            listState.animateScrollToItem(targetIndex)
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(13.dp),
                                tint = if (selectedTab == index) CyanTelemetry else TextMuted
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = if (selectedTab == index) TextPrimary else TextMuted
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_${index}")
                )
            }
        }

        // Main Tactical Content Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 1. Global Event Injector Bar
            item {
                EventInjectorBar(
                    currentEvent = currentEvent,
                    onInjectEvent = { viewModel.injectEvent(it) },
                    staLtaThreshold = staLtaThreshold,
                    onThresholdChange = { viewModel.setStaLtaThreshold(it) },
                    windNoiseGain = windNoiseGain,
                    onWindNoiseChange = { viewModel.setWindNoiseGain(it) }
                )
            }

            // 2. Multi-Node Telemetry Grid (3 Node Cards: Alpha, Bravo, Charlie)
            item {
                NodeTelemetryGrid(nodes = nodes)
            }

            // 3. Real-Time Dual-Trace Oscilloscope
            item {
                OscilloscopeCanvas(
                    rawWaveform = rawWaveform,
                    filteredWaveform = filteredWaveform,
                    isTriggered = isTriggered
                )
            }

            // 4. STA/LTA Ratio Gauge & Trigger Indicator
            item {
                StaLtaGauge(
                    currentRatio = staLtaRatio,
                    threshold = staLtaThreshold,
                    isTriggered = isTriggered
                )
            }

            // 5. Welch PSD Power Spectrum
            item {
                WelchPsdView(psdBins = psdBins)
            }

            // 6. Tactical Geospatial TDOA Map
            item {
                TacticalMapView(
                    nodes = nodes,
                    sourceXKm = viewModel.engine.sourceXKm,
                    sourceYKm = viewModel.engine.sourceYKm,
                    wavefrontRadiusKm = wavefrontRadius,
                    hyperbolicLines = hyperbolicLines,
                    estimatedBearingDeg = estimatedBearingDeg,
                    estimatedCepMeters = estimatedCepMeters,
                    isTriggered = isTriggered,
                    onMapTap = { xKm, yKm ->
                        viewModel.setSourceLocation(xKm, yKm)
                    }
                )
            }

            // 7. Verified Threat & Event Log Table
            item {
                AlertsLogTable(
                    alerts = alertsLog,
                    onClearAlerts = { viewModel.clearAlerts() }
                )
            }

            item {
                // Bottom padding for navigation bar
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
