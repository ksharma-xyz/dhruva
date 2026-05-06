package xyz.ksharma.dhruva.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.ksharma.dhruva.location.Location
import xyz.ksharma.dhruva.location.LocationConfig
import xyz.ksharma.dhruva.location.LocationError
import xyz.ksharma.dhruva.location.LocationPriority
import xyz.ksharma.dhruva.location.data.rememberLocationTracker
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dhruva sample app — single screen, runs on Android and iOS via Compose Multiplatform.
 *
 * Shows everything Dhruva does in one view:
 *   - Latest fix coordinates, accuracy, speed, bearing, age, with smooth updates.
 *   - Animated polestar (Dhruva's namesake) at the centre, gently rotating; pulses
 *     on each new fix to make new data feel alive instead of flickering text.
 *   - A recent-trail mini-canvas: the last few positions plotted relative to the
 *     latest fix, gives an immediate sense of motion without a full map.
 *   - Mode toggle: One-shot vs Streaming. Tap a single button; state animates.
 *   - Error states surface as a banner card explaining the LocationError type.
 *
 * The user is expected to grant location permission via the system before tapping.
 * The sample doesn't include a permission flow because Dhruva intentionally
 * doesn't ship one; pair with Aagya for that. The error banner mentions this.
 */
@Composable
fun SampleApp(modifier: Modifier = Modifier) {
    val tracker = rememberLocationTracker()
    val scope = rememberCoroutineScope()

    var oneShotJob by remember { mutableStateOf<Job?>(null) }
    var streamJob by remember { mutableStateOf<Job?>(null) }

    var current by remember { mutableStateOf<Location?>(null) }
    var error by remember { mutableStateOf<LocationError?>(null) }
    val trail = remember { mutableStateListOf<Location>() }
    var pulseTick by remember { mutableStateOf(0) }
    var streaming by remember { mutableStateOf(false) }

    fun pushFix(fix: Location) {
        current = fix
        error = null
        trail.add(0, fix)
        if (trail.size > TRAIL_LIMIT) trail.removeAt(trail.lastIndex)
        pulseTick += 1
    }

    fun stop() {
        streamJob?.cancel(); streamJob = null
        oneShotJob?.cancel(); oneShotJob = null
        streaming = false
        tracker.stopTracking()
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFF0F0E1F)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HeaderBar(streaming = streaming)
            CenterPiece(current = current, pulseTick = pulseTick, streaming = streaming)
            FixDetails(current = current)
            ErrorBanner(error = error)
            TrailPanel(trail = trail, current = current)

            Spacer(Modifier.weight(1f))

            ActionButton(
                streaming = streaming,
                hasFix = current != null,
                onOneShot = {
                    streamJob?.cancel(); streamJob = null
                    streaming = false
                    oneShotJob?.cancel()
                    oneShotJob = scope.launch {
                        try {
                            val fix = tracker.getCurrentLocation(timeoutMs = 8_000)
                            pushFix(fix)
                        } catch (e: LocationError) {
                            error = e
                        }
                    }
                },
                onStartStream = {
                    error = null
                    streaming = true
                    streamJob = scope.launch {
                        try {
                            tracker.startTracking(
                                LocationConfig(
                                    updateIntervalMs = 5_000,
                                    priority = LocationPriority.HIGH_ACCURACY,
                                ),
                            ).collect(::pushFix)
                        } catch (e: LocationError) {
                            error = e
                            streaming = false
                        }
                    }
                },
                onStop = ::stop,
            )
        }
    }
}

private const val TRAIL_LIMIT = 20

@Composable
private fun HeaderBar(streaming: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Dhruva",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFA6CFEF),
            )
            Text(
                text = "ध्रुव · the polestar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6E78A8),
            )
        }
        StatusPill(streaming = streaming)
    }
}

@Composable
private fun StatusPill(streaming: Boolean) {
    val (label, color) = if (streaming) {
        "Streaming" to Color(0xFF6B65DA)
    } else {
        "Idle" to Color(0xFF6B6F75)
    }
    val infinite = rememberInfiniteTransition(label = "stream-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "blink",
    )
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color.copy(alpha = if (streaming) alpha else 1f),
                        RoundedCornerShape(50),
                    ),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color,
            )
        }
    }
}

@Composable
private fun CenterPiece(current: Location?, pulseTick: Int, streaming: Boolean) {
    val infinite = rememberInfiniteTransition(label = "polestar")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing)),
        label = "rotate",
    )
    val pulseScale = remember(pulseTick) { mutableStateOf(1f) }
    LaunchedEffect(pulseTick) {
        pulseScale.value = 1.18f
        kotlinx.coroutines.delay(120)
        pulseScale.value = 1f
    }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = pulseScale.value,
        animationSpec = tween(durationMillis = 280),
        label = "pulse-scale",
    )
    val ringColor by animateColorAsState(
        targetValue = if (current != null) Color(0xFF79B8E8) else Color(0xFF2D2A8C),
        animationSpec = tween(700),
        label = "ring",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2D2A8C).copy(alpha = 0.30f), Color(0xFF0F0E1F)),
                ),
                RoundedCornerShape(28.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .scale(scale),
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            // Outer ring
            drawCircle(
                color = ringColor.copy(alpha = if (streaming) 0.85f else 0.4f),
                radius = size.minDimension / 2,
                center = center,
                style = Stroke(width = 2.5f),
            )
            // Polestar (rotating 8-pointed star)
            rotate(degrees = rotation, pivot = center) {
                val long = size.minDimension / 2.2f
                val shortR = size.minDimension / 6f
                val points = 8
                val path = androidx.compose.ui.graphics.Path()
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) long else shortR
                    val theta = (i * PI / points).toFloat() - PI.toFloat() / 2f
                    val x = center.x + r * cos(theta)
                    val y = center.y + r * sin(theta)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFA6CFEF), Color(0xFF6B65DA)),
                        center = center,
                        radius = long,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FixDetails(current: Location?) {
    Surface(
        color = Color(0xFF16152C),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (current == null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Waiting for a fix",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFA6CFEF),
                )
                Text(
                    text = "Tap the action button below. Make sure the OS has granted location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6E78A8),
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatBlock("LATITUDE", formatCoord(current.latitude))
                    StatBlock("LONGITUDE", formatCoord(current.longitude))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatBlock("ACCURACY", current.accuracy?.let { "± ${it.roundToInt()} m" } ?: "—")
                    StatBlock("SPEED", current.speed?.let { "${(it * 3.6).roundToInt()} km/h" } ?: "—")
                    StatBlock(
                        label = "BEARING",
                        value = current.bearing?.let { "${it.roundToInt()}°" } ?: "—",
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = Color(0xFF6E78A8),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            ),
            color = Color(0xFFA6CFEF),
        )
    }
}

@Composable
private fun ErrorBanner(error: LocationError?) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val message = when (error) {
            is LocationError.PermissionDenied -> "Location permission denied. Pair Dhruva with Aagya, or grant manually in Settings."
            is LocationError.LocationDisabled -> "Device location services are off. Enable them in system settings."
            is LocationError.Timeout -> "No fix in time. Move toward a window or try a lower priority."
            is LocationError.Unknown -> "Platform error: ${error.cause?.message ?: "unknown"}."
            null -> ""
        }
        Surface(
            color = Color(0xFF3A1410),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFB4A6),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun TrailPanel(trail: List<Location>, current: Location?) {
    val hasTrail = trail.size >= 2
    AnimatedVisibility(visible = hasTrail) {
        Surface(
            color = Color(0xFF16152C),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "RECENT TRAIL",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    color = Color(0xFF6E78A8),
                )
                Spacer(Modifier.height(8.dp))
                TrailCanvas(trail = trail)
            }
        }
    }
}

@Composable
private fun TrailCanvas(trail: List<Location>) {
    val plotted = remember(trail) {
        derivedStateOf {
            if (trail.size < 2) emptyList<Offset>()
            else {
                val latRange = trail.minOf { it.latitude } to trail.maxOf { it.latitude }
                val lonRange = trail.minOf { it.longitude } to trail.maxOf { it.longitude }
                val latSpan = (latRange.second - latRange.first).coerceAtLeast(1e-6)
                val lonSpan = (lonRange.second - lonRange.first).coerceAtLeast(1e-6)
                trail.map { fix ->
                    Offset(
                        x = ((fix.longitude - lonRange.first) / lonSpan).toFloat(),
                        y = (1.0 - (fix.latitude - latRange.first) / latSpan).toFloat(),
                    )
                }
            }
        }
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F0E1F)),
    ) {
        val pad = 12f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val points = plotted.value.map { Offset(pad + it.x * w, pad + it.y * h) }
        if (points.size < 2) return@Canvas

        // Connect with a dashed polyline
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF6B65DA).copy(alpha = 1f - i / points.size.toFloat()),
                start = points[i + 1],
                end = points[i],
                strokeWidth = 2f,
                pathEffect = pathEffect,
            )
        }
        // Latest fix is index 0 — draw a bigger dot
        drawCircle(color = Color(0xFFA6CFEF), radius = 6f, center = points[0])
        drawCircle(
            color = Color(0xFFA6CFEF).copy(alpha = 0.3f),
            radius = 12f,
            center = points[0],
        )
        // Older fixes — small dots fading out
        for (i in 1 until points.size) {
            drawCircle(
                color = Color(0xFF6B65DA).copy(alpha = 1f - i / points.size.toFloat()),
                radius = 3f,
                center = points[i],
            )
        }
    }
}

@Composable
private fun ActionButton(
    streaming: Boolean,
    hasFix: Boolean,
    onOneShot: () -> Unit,
    onStartStream: () -> Unit,
    onStop: () -> Unit,
) {
    val (label, color) = when {
        streaming -> "Stop streaming" to Color(0xFFE25C29)
        hasFix -> "Refresh now · or stream" to Color(0xFF6B65DA)
        else -> "Get my location" to Color(0xFF6B65DA)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = if (streaming) onStop else onOneShot,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
        }
        if (!streaming) {
            Button(
                onClick = onStartStream,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F1E3F),
                    contentColor = Color(0xFFA6CFEF),
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = "Start continuous tracking",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

private fun formatCoord(value: Double): String {
    val rounded = (value * 1_000_000).toLong() / 1_000_000.0
    return if (rounded >= 0) "+$rounded" else "$rounded"
}
