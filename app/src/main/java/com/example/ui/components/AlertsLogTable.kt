package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DetectionAlert
import com.example.model.EventType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.PurpleSpectrum
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
fun AlertsLogTable(
    alerts: List<DetectionAlert>,
    onClearAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAgencyFilter by remember { mutableStateOf("ALL") }

    val filteredAlerts = if (selectedAgencyFilter == "ALL") {
        alerts
    } else {
        alerts.filter { it.agencyRouting.contains(selectedAgencyFilter, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // Table Header & Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Verified Alerts",
                    tint = RoseAlert,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VERIFIED THREAT & EVENT LOG",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RoseGlow)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${alerts.size} DETECTIONS",
                        color = RoseAlert,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Export Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Export JSON button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TacticalDarkBg)
                        .border(1.dp, CyanTelemetry, RoundedCornerShape(4.dp))
                        .clickable { exportJson(context, alerts) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("export_json_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export JSON",
                            tint = CyanTelemetry,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "JSON",
                            color = CyanTelemetry,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))

                // Export CSV button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TacticalDarkBg)
                        .border(1.dp, EmeraldTelemetry, RoundedCornerShape(4.dp))
                        .clickable { exportCsv(context, alerts) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("export_csv_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
                            tint = EmeraldTelemetry,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "CSV",
                            color = EmeraldTelemetry,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))

                IconButton(
                    onClick = onClearAlerts,
                    modifier = Modifier.size(24.dp).testTag("clear_alerts_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Alerts",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Agency Filter Pills
        val agencies = listOf("ALL", "ISRO", "NTRO", "IMD", "NDMA")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            agencies.forEach { agency ->
                val isSelected = selectedAgencyFilter == agency
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) TacticalSurfaceElevated else TacticalDarkBg)
                        .border(
                            1.dp,
                            if (isSelected) CyanTelemetry else TacticalBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedAgencyFilter = agency }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = agency,
                        color = if (isSelected) CyanTelemetry else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Alerts Table Content
        if (filteredAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TacticalDarkBg)
                    .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO THREATS DETECTED YET",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Inject an acoustic event or lower STA/LTA threshold to trigger detection.",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredAlerts.take(6).forEach { alert ->
                    AlertRowCard(alert = alert)
                }
            }
        }
    }
}

@Composable
fun AlertRowCard(
    alert: DetectionAlert,
    modifier: Modifier = Modifier
) {
    val agencyColor = when {
        alert.agencyRouting.contains("ISRO") -> CyanTelemetry
        alert.agencyRouting.contains("NTRO") -> RoseAlert
        alert.agencyRouting.contains("IMD") -> AmberWarning
        alert.agencyRouting.contains("NDMA") -> PurpleSpectrum
        else -> TextSecondary
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TacticalDarkBg)
            .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
            .testTag("alert_row_${alert.id.lowercase()}")
    ) {
        // Top line: Classification & Agency Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(agencyColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = alert.eventType.title.uppercase(),
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(agencyColor.copy(alpha = 0.2f))
                    .border(1.dp, agencyColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = alert.agencyRouting,
                    color = agencyColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Second line: Key Metrics (Peak Amplitude, STA/LTA, Azimuth)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format("Peak: %.1f mPa", alert.peakAmplitudeMpa),
                color = CyanTelemetry,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("STA/LTA: %.2f", alert.staLtaRatio),
                color = RoseAlert,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("Azimuth: %03.1f°", alert.estimatedAzimuthDeg),
                color = AmberWarning,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("CEP: %.0fm", alert.confidenceCepMeters),
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Third line: Timestamp & TDOA Delays
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = alert.timestamp,
                color = TextMuted,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("Δt_AB: %+.1fms | Δt_AC: %+.1fms", alert.delayAlphaBravoMs, alert.delayAlphaCharlieMs),
                color = TextMuted,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun exportJson(context: Context, alerts: List<DetectionAlert>) {
    val sb = StringBuilder()
    sb.append("[\n")
    alerts.forEachIndexed { index, alert ->
        sb.append("  {\n")
        sb.append("    \"id\": \"${alert.id}\",\n")
        sb.append("    \"timestamp\": \"${alert.timestamp}\",\n")
        sb.append("    \"classification\": \"${alert.eventType.title}\",\n")
        sb.append("    \"agency\": \"${alert.agencyRouting}\",\n")
        sb.append("    \"peakAmplitudeMpa\": ${String.format("%.2f", alert.peakAmplitudeMpa)},\n")
        sb.append("    \"staLtaRatio\": ${String.format("%.2f", alert.staLtaRatio)},\n")
        sb.append("    \"estimatedAzimuthDeg\": ${String.format("%.1f", alert.estimatedAzimuthDeg)},\n")
        sb.append("    \"confidenceCepMeters\": ${String.format("%.1f", alert.confidenceCepMeters)},\n")
        sb.append("    \"sourceLat\": ${String.format("%.4f", alert.sourceLat)},\n")
        sb.append("    \"sourceLon\": ${String.format("%.4f", alert.sourceLon)},\n")
        sb.append("    \"delayAlphaBravoMs\": ${String.format("%.2f", alert.delayAlphaBravoMs)},\n")
        sb.append("    \"delayAlphaCharlieMs\": ${String.format("%.2f", alert.delayAlphaCharlieMs)}\n")
        sb.append("  }${if (index < alerts.size - 1) "," else ""}\n")
    }
    sb.append("]\n")

    val jsonString = sb.toString()
    copyToClipboard(context, "Project ECHO Alerts JSON", jsonString)
    shareText(context, "Project ECHO Detections (JSON)", jsonString)
}

private fun exportCsv(context: Context, alerts: List<DetectionAlert>) {
    val sb = StringBuilder()
    sb.append("ID,Timestamp,Classification,Agency,PeakAmplitude_mPa,STA_LTA_Ratio,EstimatedAzimuth_Deg,ConfidenceCEP_m,SourceLat,SourceLon,dtAB_ms,dtAC_ms\n")
    alerts.forEach { alert ->
        sb.append("${alert.id},")
        sb.append("\"${alert.timestamp}\",")
        sb.append("\"${alert.eventType.title}\",")
        sb.append("\"${alert.agencyRouting}\",")
        sb.append("${String.format("%.2f", alert.peakAmplitudeMpa)},")
        sb.append("${String.format("%.2f", alert.staLtaRatio)},")
        sb.append("${String.format("%.1f", alert.estimatedAzimuthDeg)},")
        sb.append("${String.format("%.1f", alert.confidenceCepMeters)},")
        sb.append("${String.format("%.4f", alert.sourceLat)},")
        sb.append("${String.format("%.4f", alert.sourceLon)},")
        sb.append("${String.format("%.2f", alert.delayAlphaBravoMs)},")
        sb.append("${String.format("%.2f", alert.delayAlphaCharlieMs)}\n")
    }

    val csvString = sb.toString()
    copyToClipboard(context, "Project ECHO Alerts CSV", csvString)
    shareText(context, "Project ECHO Detections (CSV)", csvString)
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied log to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, subject: String, body: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Export Detections Log")
    context.startActivity(shareIntent)
}
