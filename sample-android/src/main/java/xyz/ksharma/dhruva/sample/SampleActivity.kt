package xyz.ksharma.dhruva.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF6B65DA),
                    secondary = Color(0xFFA6CFEF),
                    background = Color(0xFF0F0E1F),
                    surface = Color(0xFF16152C),
                ),
            ) {
                SampleApp()
            }
        }
    }
}
