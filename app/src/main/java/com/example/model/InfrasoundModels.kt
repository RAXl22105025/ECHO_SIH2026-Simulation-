package com.example.model

enum class EventType(
    val title: String,
    val description: String,
    val agency: String,
    val defaultFrequencyHz: Float,
    val peakAmplitudeMpa: Float,
    val signatureCategory: String
) {
    AMBIENT_BASELINE(
        title = "Ambient Baseline",
        description = "Pink noise + local wind turbulence without coherent events",
        agency = "NONE",
        defaultFrequencyHz = 0.15f,
        peakAmplitudeMpa = 1.8f,
        signatureCategory = "Baseline"
    ),
    ROCKET_LAUNCH(
        title = "Rocket Launch / Re-entry",
        description = "DRDO / ISRO signature: rising chirp, 0.5–4.0 Hz",
        agency = "ISRO / DRDO",
        defaultFrequencyHz = 2.4f,
        peakAmplitudeMpa = 46.5f,
        signatureCategory = "Aerospace"
    ),
    SURFACE_EXPLOSION(
        title = "Surface Explosion / Blast",
        description = "NTRO signature: sharp shock front, rapid sub-Hz decay",
        agency = "NTRO",
        defaultFrequencyHz = 1.2f,
        peakAmplitudeMpa = 82.0f,
        signatureCategory = "Detonation"
    ),
    STORM_FRONT(
        title = "Severe Storm / Microbarom",
        description = "IMD signature: continuous 0.1–0.3 Hz standing waves",
        agency = "IMD",
        defaultFrequencyHz = 0.22f,
        peakAmplitudeMpa = 26.0f,
        signatureCategory = "Meteorological"
    ),
    LANDSLIDE(
        title = "Landslide / Avalanche",
        description = "NDMA signature: rumble envelope, 1.0–8.0 Hz",
        agency = "NDMA",
        defaultFrequencyHz = 3.6f,
        peakAmplitudeMpa = 34.0f,
        signatureCategory = "Geophysical"
    )
}

data class NodeTelemetry(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val xKm: Float,
    val yKm: Float,
    val voltage: Float = 3.32f,
    val solarWatts: Float = 4.2f,
    val coreTempC: Float = 27.4f,
    val sdFillPercent: Float = 38.6f,
    val coherenceIndex: Float = 0.94f,
    val loraRssi: Int = -82,
    val loraSnr: Float = 8.5f,
    val uplinkStatus: String = "LoRa Mesh ACTIVE",
    val isPrimaryUplink: Boolean = true,
    val distanceToSourceKm: Float = 8.4f,
    val arrivalDelaySec: Float = 0.0f,
    val rawPressurePa: Float = 0.0f,
    val filteredPressureMpa: Float = 0.0f,
    val isWaveArrived: Boolean = false
)

data class PsdBin(
    val freqHz: Float,
    val powerDb: Float,
    val isPeak: Boolean = false,
    val label: String? = null
)

data class DetectionAlert(
    val id: String,
    val timestamp: String,
    val eventType: EventType,
    val peakAmplitudeMpa: Float,
    val staLtaRatio: Float,
    val estimatedAzimuthDeg: Float,
    val agencyRouting: String,
    val confidenceCepMeters: Float,
    val sourceLat: Double,
    val sourceLon: Double,
    val delayAlphaBravoMs: Float,
    val delayAlphaCharlieMs: Float
)

data class HyperbolicBranch(
    val pointsX: List<Float>,
    val pointsY: List<Float>,
    val pairLabel: String
)
