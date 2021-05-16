import kotlin.system.exitProcess

actual fun exit(status: Int): Nothing = exitProcess(status)
actual fun installShutdownHook(store: LinkStore, config: Config) {
    Runtime.getRuntime().addShutdownHook(Thread {
        if (store.hasNextToVisit()) {
            log.default { "Interrupted!" }
            printSummaryAndGetStatus(store, config.noSummary)
        }
    })
}
