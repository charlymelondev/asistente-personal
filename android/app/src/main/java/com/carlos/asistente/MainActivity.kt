package com.carlos.asistente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.carlos.asistente.ui.navigation.NavGraph
import com.carlos.asistente.ui.theme.AsistenteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsistenteTheme {
                NavGraph()
            }
        }
    }
}
