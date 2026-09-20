package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.ProjectEchoDashboard
import com.example.ui.theme.ProjectEchoTheme
import com.example.ui.theme.TacticalDarkBg

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectEchoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TacticalDarkBg
                ) {
                    ProjectEchoDashboard()
                }
            }
        }
    }
}
