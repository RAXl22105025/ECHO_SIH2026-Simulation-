package com.example.simulation

import com.example.model.DetectionAlert
import com.example.model.EventType
import com.example.model.HyperbolicBranch
import com.example.model.NodeTelemetry
import com.example.model.PsdBin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class InfrasoundEngine {

    companion object {
        const val SOUND_SPEED_M_S = 343.0f // Speed of sound at 20°C in dry air
        const val SAMPLE_RATE_HZ = 100
        const val BUFFER_SIZE = 180 // ~1.8 seconds waveform window
        const val BASE_LAT = 27.0200
        const val BASE_LON = 71.7500
    }

    // Node Positions in relative km [xKm = East(+), yKm = North(+)]
    // Triangular 15 km aperture array (Pokhran Infrasound Test Range)
    val defaultNodes = listOf(
        NodeTelemetry(
            id = "ALPHA",
            name = "Node-Alpha",
            lat = 27.0850,
            lon = 71.7500,
            xKm = 0.0f,
            yKm = 7.2f,
            voltage = 3.34f,
            solarWatts = 4.4f,
            coreTempC = 26.8f,
            sdFillPercent = 38.2f,
            coherenceIndex = 0.95f,
            loraRssi = -79,
            loraSnr = 9.2f,
            uplinkStatus = "LoRa Mesh PRIMARY"
        ),
        NodeTelemetry(
            id = "BRAVO",
            name = "Node-Bravo",
            lat = 26.9600,
            lon = 71.6700,
            xKm = -7.9f,
            yKm = -6.6f,
            voltage = 3.31f,
            solarWatts = 4.1f,
            coreTempC = 27.4f,
            sdFillPercent = 41.5f,
            coherenceIndex = 0.93f,
            loraRssi = -84,
            loraSnr = 7.8f,
            uplinkStatus = "LoRa Mesh PRIMARY"
        ),
        NodeTelemetry(
            id = "CHARLIE",
            name = "Node-Charlie",
            lat = 26.9650,
            lon = 71.8300,
            xKm = 7.9f,
            yKm = -6.1f,
            voltage = 3.36f,
            solarWatts = 4.6f,
            coreTempC = 26.2f,
            sdFillPercent = 35.8f,
            coherenceIndex = 0.96f,
            loraRssi = -81,
            loraSnr = 8.6f,
            uplinkStatus = "LoRa Mesh PRIMARY"
        )
    )

    // Simulation State
    private var currentTimeSec = 0.0f
    private var eventStartTimeSec = 0.0f
    private var activeEvent: EventType = EventType.AMBIENT_BASELINE
    private var isEventActive = false

    // Synthetic Threat Epicenter [xKm, yKm] relative to array center
    var sourceXKm = 6.5f
    var sourceYKm = 10.2f
        private set

    var staLtaThreshold = 3.8f
    var windNoiseGain = 1.0f
    var simulationSpeed = 1.0f

    // Internal Signal State
    private var currentSta = 0.001f
    private var currentLta = 0.001f
    private var staAlpha = 0.06f // ~0.16s time constant
    private var ltaAlpha = 0.006f // ~1.6s time constant

    // Circular buffers for raw and filtered pressure
    private val rawBuffer = FloatArray(BUFFER_SIZE) { 0f }
    private val filteredBuffer = FloatArray(BUFFER_SIZE) { 0f }
    private var bufferHead = 0

    // Wavefront propagation tracking
    private var wavefrontRadiusKm = 0.0f

    // Alert throttle
    private var lastAlertTimeSec = -10.0f

    // StateFlow outputs
    private val _nodesState = MutableStateFlow(defaultNodes)
    val nodesState: StateFlow<List<NodeTelemetry>> = _nodesState.asStateFlow()

    private val _rawWaveform = MutableStateFlow(FloatArray(BUFFER_SIZE))
    val rawWaveform: StateFlow<FloatArray> = _rawWaveform.asStateFlow()

    private val _filteredWaveform = MutableStateFlow(FloatArray(BUFFER_SIZE))
    val filteredWaveform: StateFlow<FloatArray> = _filteredWaveform.asStateFlow()

    private val _currentEvent = MutableStateFlow(EventType.AMBIENT_BASELINE)
    val currentEvent: StateFlow<EventType> = _currentEvent.asStateFlow()

    private val _staLtaRatio = MutableStateFlow(1.0f)
    val staLtaRatio: StateFlow<Float> = _staLtaRatio.asStateFlow()

    private val _isTriggered = MutableStateFlow(false)
    val isTriggered: StateFlow<Boolean> = _isTriggered.asStateFlow()

    private val _psdBins = MutableStateFlow<List<PsdBin>>(emptyList())
    val psdBins: StateFlow<List<PsdBin>> = _psdBins.asStateFlow()

    private val _alertsLog = MutableStateFlow<List<DetectionAlert>>(emptyList())
    val alertsLog: StateFlow<List<DetectionAlert>> = _alertsLog.asStateFlow()

    private val _wavefrontRadius = MutableStateFlow(0f)
    val wavefrontRadius: StateFlow<Float> = _wavefrontRadius.asStateFlow()

    private val _hyperbolicLines = MutableStateFlow<List<HyperbolicBranch>>(emptyList())
    val hyperbolicLines: StateFlow<List<HyperbolicBranch>> = _hyperbolicLines.asStateFlow()

    private val _estimatedBearingDeg = MutableStateFlow(0.0f)
    val estimatedBearingDeg: StateFlow<Float> = _estimatedBearingDeg.asStateFlow()

    private val _estimatedCepMeters = MutableStateFlow(280.0f)
    val estimatedCepMeters: StateFlow<Float> = _estimatedCepMeters.asStateFlow()

    init {
        computeDistancesAndTdoa()
        computeWelchPsd(EventType.AMBIENT_BASELINE, 0f)
    }

    fun setEventSourceLocation(xKm: Float, yKm: Float) {
        sourceXKm = xKm.coerceIn(-25f, 25f)
        sourceYKm = yKm.coerceIn(-25f, 25f)
        computeDistancesAndTdoa()
    }

    fun injectEvent(type: EventType) {
        activeEvent = type
        _currentEvent.value = type
        eventStartTimeSec = currentTimeSec
        isEventActive = (type != EventType.AMBIENT_BASELINE)
        wavefrontRadiusKm = 0.0f
        computeDistancesAndTdoa()
    }

    fun resetSimulation() {
        currentTimeSec = 0f
        eventStartTimeSec = 0f
        activeEvent = EventType.AMBIENT_BASELINE
        _currentEvent.value = EventType.AMBIENT_BASELINE
        isEventActive = false
        currentSta = 0.001f
        currentLta = 0.001f
        wavefrontRadiusKm = 0.0f
        for (i in 0 until BUFFER_SIZE) {
            rawBuffer[i] = 0f
            filteredBuffer[i] = 0f
        }
        _rawWaveform.value = FloatArray(BUFFER_SIZE)
        _filteredWaveform.value = FloatArray(BUFFER_SIZE)
        _isTriggered.value = false
        _staLtaRatio.value = 1.0f
        computeWelchPsd(EventType.AMBIENT_BASELINE, 0f)
    }

    fun clearAlerts() {
        _alertsLog.value = emptyList()
    }

    /**
     * Advances simulation by dt seconds (e.g. 0.03s for ~33 Hz visual tick, simulating 3 samples)
     */
    fun tick(dtSeconds: Float) {
        val dt = dtSeconds * simulationSpeed
        currentTimeSec += dt

        // Expand acoustic wavefront if event is active (speed of sound ~0.343 km/s)
        if (isEventActive) {
            val eventElapsed = currentTimeSec - eventStartTimeSec
            wavefrontRadiusKm = eventElapsed * (SOUND_SPEED_M_S / 1000.0f)
            _wavefrontRadius.value = wavefrontRadiusKm
        } else {
            wavefrontRadiusKm = 0.0f
            _wavefrontRadius.value = 0.0f
        }

        // Generate synthetic pressure for primary node (Node-Alpha)
        val alphaNode = defaultNodes[0]
        val distToAlphaKm = alphaNode.distanceToSourceKm
        val arrivalTimeAlpha = distToAlphaKm / (SOUND_SPEED_M_S / 1000.0f)
        val eventElapsedAtAlpha = if (isEventActive) (currentTimeSec - eventStartTimeSec) - arrivalTimeAlpha else -1.0f

        val t = currentTimeSec

        // 1. Physical Dynamics: Atmospheric Wind Turbulence
        // Low-frequency 1/f drift + brownian boundary layer noise (fluctuations ±1.5 - 3.0 Pa)
        val windDrift = (sin(t * 0.08) * 1.5 + sin(t * 0.3) * 0.8 + (Math.random() - 0.5) * 0.4).toFloat() * windNoiseGain

        // 2. Target Coherent Infrasound Signal (0.01 Hz - 20 Hz, Millipascal resolution)
        val coherentInfrasoundMpa = synthesizeInfrasound(activeEvent, eventElapsedAtAlpha)
        val coherentPa = coherentInfrasoundMpa / 1000.0f

        // 3. Raw Inlet Pressure (Patm)
        val rawPressurePa = windDrift + coherentPa

        // 4. Simulated Adaptive Pneumatic Manifold + Bandpass Filter (0.01 - 20 Hz)
        // Pneumatic capillaries reject 99.5% of wind turbulence; electronic stage passes coherent infrasound
        val filteredNoiseFloorMpa = ((Math.random() - 0.5) * 0.9).toFloat()
        val filteredPressureMpa = coherentInfrasoundMpa + (windDrift * 5.0f) + filteredNoiseFloorMpa

        // Store into rolling buffer
        rawBuffer[bufferHead] = rawPressurePa
        filteredBuffer[bufferHead] = filteredPressureMpa
        bufferHead = (bufferHead + 1) % BUFFER_SIZE

        // 5. STA / LTA (Short-Term Average / Long-Term Average) Calculation
        val sampleEnergy = filteredPressureMpa * filteredPressureMpa
        currentSta = staAlpha * sampleEnergy + (1.0f - staAlpha) * currentSta
        currentLta = ltaAlpha * sampleEnergy + (1.0f - ltaAlpha) * currentLta

        val safeLta = max(currentLta, 0.02f)
        val ratio = currentSta / safeLta
        _staLtaRatio.value = ratio

        val triggered = ratio >= staLtaThreshold
        _isTriggered.value = triggered

        // Trigger Event Detection Alert
        if (triggered && isEventActive && (currentTimeSec - lastAlertTimeSec > 5.0f)) {
            triggerAlert(filteredPressureMpa, ratio)
            lastAlertTimeSec = currentTimeSec
        }

        // Update Welch PSD
        computeWelchPsd(activeEvent, filteredPressureMpa)

        // Update Telemetry for all 3 nodes
        updateNodeTelemetries(rawPressurePa, filteredPressureMpa)

        // Update ordered waveform arrays for canvas
        val orderedRaw = FloatArray(BUFFER_SIZE)
        val orderedFiltered = FloatArray(BUFFER_SIZE)
        for (i in 0 until BUFFER_SIZE) {
            val idx = (bufferHead + i) % BUFFER_SIZE
            orderedRaw[i] = rawBuffer[idx]
            orderedFiltered[i] = filteredBuffer[idx]
        }
        _rawWaveform.value = orderedRaw
        _filteredWaveform.value = orderedFiltered
    }

    private fun synthesizeInfrasound(event: EventType, elapsedSec: Float): Float {
        if (elapsedSec < 0f) {
            // Ambient microbarom background ~1.5 mPa
            return (sin(currentTimeSec * 2.0 * PI * 0.2) * 1.4 + (Math.random() - 0.5) * 0.8).toFloat()
        }

        return when (event) {
            EventType.AMBIENT_BASELINE -> {
                (sin(currentTimeSec * 2.0 * PI * 0.2) * 1.5 + (Math.random() - 0.5) * 0.8).toFloat()
            }
            EventType.ROCKET_LAUNCH -> {
                // DRDO / ISRO signature: rising chirp, 0.5 Hz to 4.0 Hz
                if (elapsedSec > 16.0f) return (sin(currentTimeSec * 2.0 * PI * 0.2) * 1.5).toFloat()
                val chirpRate = 0.22f // Hz per second
                val instFreq = 0.5f + chirpRate * elapsedSec
                val envelope = min(1.0f, elapsedSec / 1.5f) * max(0.0f, 1.0f - (elapsedSec - 10.0f) / 6.0f)
                val signal = sin(2.0 * PI * instFreq * elapsedSec) * event.peakAmplitudeMpa * envelope
                (signal + (Math.random() - 0.5) * 2.5).toFloat()
            }
            EventType.SURFACE_EXPLOSION -> {
                // NTRO signature: sharp shock front, rapid sub-Hz decay
                if (elapsedSec > 10.0f) return (sin(currentTimeSec * 2.0 * PI * 0.2) * 1.5).toFloat()
                val shockRise = min(1.0f, elapsedSec / 0.12f)
                val decay = exp(-elapsedSec * 0.55f)
                val blastOsc = sin(2.0 * PI * 1.2 * elapsedSec)
                val signal = shockRise * decay * blastOsc * event.peakAmplitudeMpa
                (signal + (Math.random() - 0.5) * 1.8).toFloat()
            }
            EventType.STORM_FRONT -> {
                // IMD signature: continuous 0.1–0.3 Hz standing waves (ocean microbaroms)
                val carrier = sin(2.0 * PI * 0.22 * elapsedSec)
                val modulation = 0.65 + 0.35 * sin(2.0 * PI * 0.04 * elapsedSec)
                val signal = carrier * modulation * event.peakAmplitudeMpa
                (signal + (Math.random() - 0.5) * 2.0).toFloat()
            }
            EventType.LANDSLIDE -> {
                // NDMA signature: rumble envelope, 1.0–8.0 Hz multi-harmonic
                if (elapsedSec > 14.0f) return (sin(currentTimeSec * 2.0 * PI * 0.2) * 1.5).toFloat()
                val envelope = min(1.0f, elapsedSec / 2.0f) * max(0.0f, 1.0f - (elapsedSec - 8.0f) / 6.0f)
                val h1 = sin(2.0 * PI * 1.8 * elapsedSec) * 0.4
                val h2 = sin(2.0 * PI * 3.6 * elapsedSec) * 0.5
                val h3 = sin(2.0 * PI * 5.4 * elapsedSec) * 0.25
                val signal = (h1 + h2 + h3) * envelope * event.peakAmplitudeMpa
                (signal + (Math.random() - 0.5) * 3.5).toFloat()
            }
        }
    }

    private fun updateNodeTelemetries(rawP: Float, filtP: Float) {
        val updated = _nodesState.value.mapIndexed { index, node ->
            val dist = node.distanceToSourceKm
            val arrSec = dist / (SOUND_SPEED_M_S / 1000.0f)
            val waveArrived = isEventActive && (currentTimeSec - eventStartTimeSec >= arrSec)
            
            // Attenuate slightly with 1/r cylindrical / spherical geometric spreading
            val atten = (8.0f / max(dist, 4.0f)).coerceIn(0.4f, 1.4f)
            val nodeFiltered = if (waveArrived) filtP * atten else (Math.random() - 0.5).toFloat() * 1.8f
            val nodeRaw = rawP + (Math.random() - 0.5).toFloat() * 0.1f

            // Micro-variations in solar/voltage
            val jitterVolt = node.voltage + (sin(currentTimeSec * 0.05 + index) * 0.005).toFloat()
            val jitterSolar = (node.solarWatts + (cos(currentTimeSec * 0.07 + index) * 0.08).toFloat()).coerceAtLeast(0f)

            node.copy(
                arrivalDelaySec = arrSec,
                rawPressurePa = nodeRaw,
                filteredPressureMpa = nodeFiltered,
                isWaveArrived = waveArrived,
                voltage = jitterVolt,
                solarWatts = jitterSolar,
                coherenceIndex = if (waveArrived) 0.98f else 0.93f
            )
        }
        _nodesState.value = updated
    }

    private fun computeDistancesAndTdoa() {
        val updated = defaultNodes.map { node ->
            val dx = sourceXKm - node.xKm
            val dy = sourceYKm - node.yKm
            val dist = sqrt(dx * dx + dy * dy)
            val arrDelay = dist / (SOUND_SPEED_M_S / 1000.0f)
            node.copy(
                distanceToSourceKm = dist,
                arrivalDelaySec = arrDelay
            )
        }
        _nodesState.value = updated

        // Compute Azimuth from array centroid (0,0)
        var azDeg = Math.toDegrees(atan2(sourceXKm.toDouble(), sourceYKm.toDouble())).toFloat()
        if (azDeg < 0f) azDeg += 360f
        _estimatedBearingDeg.value = azDeg

        // Compute CEP based on distance (closer = smaller error)
        val centroidDist = sqrt(sourceXKm * sourceXKm + sourceYKm * sourceYKm)
        val cep = (120f + centroidDist * 14.5f).coerceIn(150f, 650f)
        _estimatedCepMeters.value = cep

        // Generate Hyperbolic TDOA bearing branches
        generateHyperbolicCurves(updated)
    }

    private fun generateHyperbolicCurves(nodes: List<NodeTelemetry>) {
        if (nodes.size < 3) return
        val na = nodes[0] // Alpha
        val nb = nodes[1] // Bravo
        val nc = nodes[2] // Charlie

        // d_diff = c * delta_t
        val cKmS = SOUND_SPEED_M_S / 1000.0f
        val dDiffAB = (na.distanceToSourceKm - nb.distanceToSourceKm)
        val dDiffAC = (na.distanceToSourceKm - nc.distanceToSourceKm)

        // Compute hyperbolic curves on map grid
        val branchAB = computeHyperbolaPoints(na.xKm, na.yKm, nb.xKm, nb.yKm, dDiffAB, "AB")
        val branchAC = computeHyperbolaPoints(na.xKm, na.yKm, nc.xKm, nc.yKm, dDiffAC, "AC")

        _hyperbolicLines.value = listOfNotNull(branchAB, branchAC)
    }

    private fun computeHyperbolaPoints(
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        targetDDiff: Float,
        label: String
    ): HyperbolicBranch {
        val ptsX = mutableListOf<Float>()
        val ptsY = mutableListOf<Float>()

        // Parametric ray sweeping around array
        val steps = 80
        val sweepRadiusKm = 24.0f

        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2.0 * PI
            val px = (cos(angle) * sweepRadiusKm).toFloat()
            val py = (sin(angle) * sweepRadiusKm).toFloat()

            val d1 = sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1))
            val d2 = sqrt((px - x2) * (px - x2) + (py - y2) * (py - y2))
            val dDiff = d1 - d2

            // If close to the hyperbola condition
            if (kotlin.math.abs(dDiff - targetDDiff) < 0.6f) {
                ptsX.add(px)
                ptsY.add(py)
            }
        }

        // Ensure we always have bearing line to the source
        ptsX.add(sourceXKm)
        ptsY.add(sourceYKm)

        return HyperbolicBranch(ptsX, ptsY, "TDOA Hyperbola $\\Delta t_{$label}$")
    }

    private fun computeWelchPsd(event: EventType, currentAmplitude: Float) {
        val frequencies = floatArrayOf(
            0.02f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.35f, 0.5f,
            0.75f, 1.0f, 1.2f, 1.5f, 2.0f, 2.4f, 3.0f, 3.6f,
            4.5f, 6.0f, 8.0f, 10.0f, 12.5f, 15.0f, 17.5f, 20.0f
        )

        val targetPeakFreq = event.defaultFrequencyHz
        val peakAmp = if (isEventActive) event.peakAmplitudeMpa else 2.0f

        val bins = frequencies.map { freq ->
            // Gaussian resonance peak around event characteristic frequency
            val df = freq - targetPeakFreq
            val width = when (event) {
                EventType.STORM_FRONT -> 0.08f
                EventType.ROCKET_LAUNCH -> 0.8f
                EventType.SURFACE_EXPLOSION -> 0.5f
                EventType.LANDSLIDE -> 1.4f
                EventType.AMBIENT_BASELINE -> 0.3f
            }

            val resonance = exp(-(df * df) / (2.0f * width * width))
            // Convert to synthetic PSD in dB rel 1 mPa^2/Hz (-30 to +40 dB)
            val baseNoise = -22.0f + (Math.random() * 3.0).toFloat()
            val powerDb = (baseNoise + resonance * (peakAmp * 0.65f)).coerceIn(-35.0f, 45.0f)
            val isPeak = resonance > 0.75f && isEventActive

            val label = if (isPeak && freq in (targetPeakFreq - 0.3f)..(targetPeakFreq + 0.3f)) {
                when (event) {
                    EventType.STORM_FRONT -> "0.22 Hz (Microbarom)"
                    EventType.ROCKET_LAUNCH -> "2.4 Hz (ISRO Chirp)"
                    EventType.SURFACE_EXPLOSION -> "1.2 Hz (Blast)"
                    EventType.LANDSLIDE -> "3.6 Hz (Rumble)"
                    EventType.AMBIENT_BASELINE -> "0.15 Hz (Ambient)"
                }
            } else null

            PsdBin(freqHz = freq, powerDb = powerDb, isPeak = isPeak, label = label)
        }

        _psdBins.value = bins
    }

    private fun triggerAlert(amplitudeMpa: Float, staLta: Float) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'IST'", Locale.getDefault())
        val timestampStr = sdf.format(Date())

        val nodes = _nodesState.value
        val dtAB = if (nodes.size >= 2) (nodes[0].arrivalDelaySec - nodes[1].arrivalDelaySec) * 1000.0f else 0.0f
        val dtAC = if (nodes.size >= 3) (nodes[0].arrivalDelaySec - nodes[2].arrivalDelaySec) * 1000.0f else 0.0f

        // Calculate source lat/lon approximation from relative km
        // 1 deg lat ~ 111 km, 1 deg lon ~ 99 km at 27°N
        val latDeg = BASE_LAT + (sourceYKm / 111.0)
        val lonDeg = BASE_LON + (sourceXKm / 99.0)

        val alert = DetectionAlert(
            id = "ECH-" + UUID.randomUUID().toString().take(6).uppercase(),
            timestamp = timestampStr,
            eventType = activeEvent,
            peakAmplitudeMpa = amplitudeMpa.coerceAtLeast(activeEvent.peakAmplitudeMpa * 0.9f),
            staLtaRatio = staLta,
            estimatedAzimuthDeg = _estimatedBearingDeg.value,
            agencyRouting = activeEvent.agency,
            confidenceCepMeters = _estimatedCepMeters.value,
            sourceLat = latDeg,
            sourceLon = lonDeg,
            delayAlphaBravoMs = dtAB,
            delayAlphaCharlieMs = dtAC
        )

        _alertsLog.value = listOf(alert) + _alertsLog.value.take(24)
    }
}
