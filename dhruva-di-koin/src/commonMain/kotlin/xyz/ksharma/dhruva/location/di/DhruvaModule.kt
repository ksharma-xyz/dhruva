package xyz.ksharma.dhruva.location.di

import org.koin.core.module.Module
import org.koin.dsl.module
import xyz.ksharma.dhruva.location.Logger
import xyz.ksharma.dhruva.location.NoOpLogger

/**
 * Koin module factory for Dhruva.
 *
 * The `LocationTracker` itself needs platform context (`Activity` / `UIViewController`),
 * so use Dhruva's `rememberLocationTracker()` Composable factory in your UI. This module
 * provides the cross-cutting [Logger] singleton that the controller can pull from Koin
 * when you wire it up.
 */
public fun dhruvaModule(
    logger: Logger = NoOpLogger,
): Module = module {
    single<Logger> { logger }
}
