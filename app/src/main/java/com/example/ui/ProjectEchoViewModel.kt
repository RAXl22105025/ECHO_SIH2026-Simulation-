package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DetectionAlert
import com.example.model.EventType
import com.example.model.HyperbolicBranch
import com.example.model.NodeTelemetry
import com.example.model.PsdBin
import com.example.simulation.InfrasoundEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ProjectEchoViewModel : ViewModel() {

    val engine = InfrasoundEngine()

    private val _isRunning = MutableStateFlow(true)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _simulationSpeed = MutableStateFlow(1.0f)
    val simulationSpeed: StateFlow<Float> = _simulationSpeed.asStateFlow()

    private val _windNoiseGain = MutableStateFlow(1.0f)
    val windNoiseGain: StateFlow<Float> = _windNoiseGain.asStateFlow()

    private val _staLtaThreshold = MutableStateFlow(3.8f)
    val staLtaThreshold: StateFlow<Float> = _staLtaThreshold.asStateFlow()

    // Engine Flow Delegations
    val currentEvent: StateFlow<EventType> = engine.currentEvent
    val nodes: StateFlow<List<NodeTelemetry>> = engine.nodesState
    val rawWaveform: StateFlow<FloatArray> = engine.rawWaveform
    val filteredWaveform: StateFlow<FloatArray> = engine.filteredWaveform
    val staLtaRatio: StateFlow<Float> = engine.staLtaRatio
    val isTriggered: StateFlow<Boolean> = engine.isTriggered
    val psdBins: StateFlow<List<PsdBin>> = engine.psdBins
    val alertsLog: StateFlow<List<DetectionAlert>> = engine.alertsLog
    val wavefrontRadius: StateFlow<Float> = engine.wavefrontRadius
    val hyperbolicLines: StateFlow<List<HyperbolicBranch>> = engine.hyperbolicLines
    val estimatedBearingDeg: StateFlow<Float> = engine.estimatedBearingDeg
    val estimatedCepMeters: StateFlow<Float> = engine.estimatedCepMeters

    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            // Tick at ~35 Hz (approx 28ms interval), generating high-resolution continuous telemetry
            val intervalMs = 28L
            while (isActive) {
                if (_isRunning.value) {
                    engine.tick(intervalMs / 1000.0f)
                }
                delay(intervalMs)
            }
        }
    }

    fun toggleSimulation() {
        _isRunning.value = !_isRunning.value
    }

    fun setSimulationSpeed(speed: Float) {
        _simulationSpeed.value = speed
        engine.simulationSpeed = speed
    }

    fun setWindNoiseGain(gain: Float) {
        _windNoiseGain.value = gain
        engine.windNoiseGain = gain
    }

    fun setStaLtaThreshold(threshold: Float) {
        _staLtaThreshold.value = threshold
        engine.staLtaThreshold = threshold
    }

    fun injectEvent(eventType: EventType) {
        engine.injectEvent(eventType)
    }

    fun setSourceLocation(xKm: Float, yKm: Float) {
        engine.setEventSourceLocation(xKm, yKm)
    }

    fun resetSimulation() {
        engine.resetSimulation()
    }

    fun clearAlerts() {
        engine.clearAlerts()
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
