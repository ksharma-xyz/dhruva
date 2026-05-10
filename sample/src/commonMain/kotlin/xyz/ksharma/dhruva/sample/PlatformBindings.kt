package xyz.ksharma.dhruva.sample

import androidx.compose.runtime.Composable

/**
 * Returns a closure that opens the host app's system Settings page so the user
 * can grant location permission manually. Used by the sample's error banner
 * when location is denied.
 *
 * Dhruva intentionally doesn't ship a permission flow (pair with Aagya for
 * that), but for a standalone sample we want a one-tap path to grant.
 */
@Composable
internal expect fun rememberSettingsOpener(): () -> Unit
