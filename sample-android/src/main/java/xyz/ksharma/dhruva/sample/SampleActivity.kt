package xyz.ksharma.dhruva.sample

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.data.rememberLocationTracker

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen()
                }
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    val tracker = rememberLocationTracker()
    val scope = rememberCoroutineScope()

    val current = remember { MutableStateFlow<Location?>(null) }
    val streaming = remember { MutableStateFlow<Location?>(null) }
    var status by remember { mutableStateOf("Idle") }

    val currentValue by current.collectAsState()
    val streamingValue by streaming.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* no-op for sample */ }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(text = "Dhruva Sample", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Tap 'Get Once' for a single fix or 'Stream' to watch updates roll in.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(text = "Status: $status", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            Text("One-shot", style = MaterialTheme.typography.titleMedium)
            currentValue?.let {
                Text(
                    text = "${it.latitude}, ${it.longitude}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = {
                scope.launch {
                    runCatching {
                        status = "Asking..."
                        current.value = tracker.getCurrentLocation()
                        status = "Got fix"
                    }.onFailure {
                        status = "Failed: ${it.message}"
                    }
                }
            }) {
                Text("Get Once")
            }

            HorizontalDivider()

            Text("Streaming", style = MaterialTheme.typography.titleMedium)
            streamingValue?.let {
                Text(
                    text = "${it.latitude}, ${it.longitude} (acc=${it.accuracy})",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = {
                scope.launch {
                    runCatching {
                        status = "Streaming..."
                        tracker.startTracking(LocationConfig(updateIntervalMs = 5_000))
                            .collect { fix -> streaming.value = fix }
                    }.onFailure {
                        status = "Failed: ${it.message}"
                    }
                }
            }) {
                Text("Stream")
            }

            HorizontalDivider()

            OutlinedButton(onClick = {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }) {
                Text("Request Permission")
            }
        }
    }
}
