package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HyperbolicBranch
import com.example.model.NodeTelemetry
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldTelemetry
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalBorderBright
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sqrt

@Composable
fun TacticalMapView(
    nodes: List<NodeTelemetry>,
    sourceXKm: Float,
    sourceYKm: Float,
    wavefrontRadiusKm: Float,
    hyperbolicLines: List<HyperbolicBranch>,
    estimatedBearingDeg: Float,
    estimatedCepMeters: Float,
    isTriggered: Boolean,
    onMapTap: (xKm: Float, yKm: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Range scale in km (±25 km viewport)
    val mapSpanKm = 25.0f

    val nodeAlpha = nodes.getOrNull(0)
    val nodeBravo = nodes.getOrNull(1)
    val nodeCharlie = nodes.getOrNull(2)

    val dtABMs = if (nodeAlpha != null && nodeBravo != null) {
        (nodeAlpha.arrivalDelaySec - nodeBravo.arrivalDelaySec) * 1000.0f
    } else 0f

    val dtACMs = if (nodeAlpha != null && nodeCharlie != null) {
        (nodeAlpha.arrivalDelaySec - nodeCharlie.arrivalDelaySec) * 1000.0f
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TacticalSurface)
            .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        // Map Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "TDOA Radar Map",
                    tint = CyanTelemetry,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TACTICAL GEOSPATIAL TDOA MAP",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }

            Text(
                text = "POKHRAN TEST RANGE [27.02°N, 71.75°E]",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Coordinates & TDOA Delays bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(TacticalDarkBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("Δt(A-B): %+.1f ms", dtABMs),
                color = CyanTelemetry,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("Δt(A-C): %+.1f ms", dtACMs),
                color = EmeraldTelemetry,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("BEARING: %03.0f° | CEP: %.0f m", estimatedBearingDeg, estimatedCepMeters),
                color = AmberWarning,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Radar Canvas (Interactive Tap to reposition threat epicenter)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF040810))
                .border(1.dp, TacticalBorderBright, RoundedCornerShape(6.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cx = size.width / 2.0f
                            val cy = size.height / 2.0f
                            val pixelsPerKm = (size.width / 2.0f) / mapSpanKm
                            val tappedXKm = (offset.x - cx) / pixelsPerKm
                            val tappedYKm = -(offset.y - cy) / pixelsPerKm
                            onMapTap(tappedXKm, tappedYKm)
                        }
                    }
                    .testTag("tactical_radar_canvas")
            ) {
                val cx = size.width / 2.0f
                val cy = size.height / 2.0f
                val pixelsPerKm = (size.width / 2.0f) / mapSpanKm

                // Helper to map (xKm, yKm) to Canvas Pixels
                fun kmToOffset(xKm: Float, yKm: Float): Offset {
                    return Offset(cx + xKm * pixelsPerKm, cy - yKm * pixelsPerKm)
                }

                // 1. Draw Range Rings (5 km, 10 km, 15 km, 20 km)
                val ringColor = Color(0x2200F2FE)
                val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                val rings = listOf(5f, 10f, 15f, 20f)
                for (rKm in rings) {
                    val radiusPx = rKm * pixelsPerKm
                    drawCircle(
                        color = ringColor,
                        radius = radiusPx,
                        center = Offset(cx, cy),
                        style = Stroke(width = 0.8f, pathEffect = dash)
                    )
                }

                // Crosshairs Axes
                drawLine(
                    color = Color(0x3300F2FE),
                    start = Offset(cx, 0f),
                    end = Offset(cx, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0x3300F2FE),
                    start = Offset(0f, cy),
                    end = Offset(size.width, cy),
                    strokeWidth = 1f
                )

                // 2. Draw Sensor Array Baseline Triangle
                if (nodes.size >= 3) {
                    val ptA = kmToOffset(nodes[0].xKm, nodes[0].yKm)
                    val ptB = kmToOffset(nodes[1].xKm, nodes[1].yKm)
                    val ptC = kmToOffset(nodes[2].xKm, nodes[2].yKm)

                    val trianglePath = Path().apply {
                        moveTo(ptA.x, ptA.y)
                        lineTo(ptB.x, ptB.y)
                        lineTo(ptC.x, ptC.y)
                        close()
                    }

                    // Fill subtle array area
                    drawPath(
                        path = trianglePath,
                        color = CyanGlow
                    )
                    // Stroke array boundary
                    drawPath(
                        path = trianglePath,
                        color = CyanTelemetry.copy(alpha = 0.5f),
                        style = Stroke(width = 1.2f, pathEffect = dash)
                    )
                }

                // 3. Draw Hyperbolic TDOA Bearing Lines radiating to source
                hyperbolicLines.forEach { branch ->
                    if (branch.pointsX.size > 1) {
                        val hypPath = Path()
                        branch.pointsX.forEachIndexed { i, px ->
                            val py = branch.pointsY[i]
                            val offset = kmToOffset(px, py)
                            if (i == 0) hypPath.moveTo(offset.x, offset.y) else hypPath.lineTo(offset.x, offset.y)
                        }

                        drawPath(
                            path = hypPath,
                            color = AmberWarning.copy(alpha = 0.6f),
                            style = Stroke(width = 1.8f)
                        )
                    }
                }

                // 4. Draw Expanding Acoustic Wavefront Rings from source
                if (wavefrontRadiusKm > 0f) {
                    val sourcePt = kmToOffset(sourceXKm, sourceYKm)
                    val waveRadiusPx = wavefrontRadiusKm * pixelsPerKm

                    // Shockwave circle
                    drawCircle(
                        color = RoseAlert.copy(alpha = 0.7f),
                        radius = waveRadiusPx,
                        center = sourcePt,
                        style = Stroke(width = 2.2f)
                    )

                    // Secondary echo circle
                    if (wavefrontRadiusKm > 2f) {
                        drawCircle(
                            color = RoseAlert.copy(alpha = 0.35f),
                            radius = (wavefrontRadiusKm - 2f) * pixelsPerKm,
                            center = sourcePt,
                            style = Stroke(width = 1.2f)
                        )
                    }
                }

                // 5. Draw Sensor Nodes markers (Alpha, Bravo, Charlie)
                nodes.forEach { node ->
                    val pt = kmToOffset(node.xKm, node.yKm)
                    val nodeColor = when (node.id) {
                        "ALPHA" -> CyanTelemetry
                        "BRAVO" -> EmeraldTelemetry
                        else -> Color(0xFF38BDF8)
                    }

                    // Outer ping ring
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = pt
                    )
                    // Inner node dot
                    drawCircle(
                        color = nodeColor,
                        radius = 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }

                // 6. Draw Estimated Threat Epicenter & CEP Circle
                val epicenterPt = kmToOffset(sourceXKm, sourceYKm)
                val cepRadiusPx = (estimatedCepMeters / 1000.0f) * pixelsPerKm

                // CEP Error Circle
                drawCircle(
                    color = RoseAlert.copy(alpha = 0.2f),
                    radius = cepRadiusPx.coerceAtLeast(8.dp.toPx()),
                    center = epicenterPt
                )
                drawCircle(
                    color = RoseAlert,
                    radius = cepRadiusPx.coerceAtLeast(8.dp.toPx()),
                    center = epicenterPt,
                    style = Stroke(width = 1.2f, pathEffect = dash)
                )

                // Epicenter Crosshair Target
                drawCircle(
                    color = RoseAlert,
                    radius = 3.dp.toPx(),
                    center = epicenterPt
                )
                // Crosshair lines
                val crossArm = 8.dp.toPx()
                drawLine(
                    color = RoseAlert,
                    start = Offset(epicenterPt.x - crossArm, epicenterPt.y),
                    end = Offset(epicenterPt.x + crossArm, epicenterPt.y),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = RoseAlert,
                    start = Offset(epicenterPt.x, epicenterPt.y - crossArm),
                    end = Offset(epicenterPt.x, epicenterPt.y + crossArm),
                    strokeWidth = 1.5f
                )
            }

            // Tactical Overlay Badges
            // Range Scale Tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TacticalDarkBg.copy(alpha = 0.8f))
                    .border(1.dp, TacticalBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "RADAR RANGE: ±25 KM | TAP TO RELOCATE THREAT",
                    color = TextSecondary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Compass Orientation
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(TacticalDarkBg.copy(alpha = 0.8f))
                    .border(1.dp, TacticalBorder, CircleShape)
                    .padding(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "North",
                        tint = RoseAlert,
                        modifier = Modifier.size(12.dp)
                    )
                    Text("N", color = TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
