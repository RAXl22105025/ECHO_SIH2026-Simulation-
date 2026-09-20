package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Project ECHO", appName)
  }

  @Test
  fun `test infrasound engine simulation and tdoa calculation`() {
    val engine = com.example.simulation.InfrasoundEngine()
    assertEquals(3, engine.nodesState.value.size)

    // Inject explosion event
    engine.injectEvent(com.example.model.EventType.SURFACE_EXPLOSION)
    assertEquals(com.example.model.EventType.SURFACE_EXPLOSION, engine.currentEvent.value)

    // Advance simulation
    for (i in 0 until 50) {
      engine.tick(0.03f)
    }

    // Verify buffer has values
    org.junit.Assert.assertTrue(engine.rawWaveform.value.isNotEmpty())
    org.junit.Assert.assertTrue(engine.filteredWaveform.value.isNotEmpty())
    org.junit.Assert.assertTrue(engine.estimatedBearingDeg.value in 0f..360f)
  }
}
