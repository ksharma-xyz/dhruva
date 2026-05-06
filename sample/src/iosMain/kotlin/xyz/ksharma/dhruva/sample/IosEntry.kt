package xyz.ksharma.dhruva.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point. The Swift side calls [SampleViewController] to obtain a
 * `UIViewController` hosting the shared Compose Multiplatform sample.
 */
@Suppress("FunctionName")
fun SampleViewController(): UIViewController = ComposeUIViewController {
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
