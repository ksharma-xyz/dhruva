package xyz.ksharma.dhruva.location

/**
 * Minimal logger interface used by Dhruva internals. Defaults to [NoOpLogger]; swap in
 * your own logger to forward Dhruva's diagnostics to Kermit, Timber, OSLog, etc.
 */
public interface Logger {
    public fun debug(message: String)
    public fun info(message: String)
    public fun warn(message: String, error: Throwable? = null)
    public fun error(message: String, error: Throwable? = null)
}

public object NoOpLogger : Logger {
    override fun debug(message: String) = Unit
    override fun info(message: String) = Unit
    override fun warn(message: String, error: Throwable?) = Unit
    override fun error(message: String, error: Throwable?) = Unit
}
