import domain.Link
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.signal
import kotlin.system.exitProcess

actual fun exit(status: Int): Nothing = exitProcess(status)

// workaround because staticCFunction lambda cannot capture
private lateinit var _store: LinkStore
private lateinit var _config: Config

actual fun installShutdownHook(store: LinkStore, config: Config) {
    _store = store
    _config = config
    signal(SIGINT, staticCFunction<Int, Unit> {
        log.default { "Interrupted!" }
        printSummaryAndGetStatus(_store, _config.noSummary)
        exit(130)
    })
}
