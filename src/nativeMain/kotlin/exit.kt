import kotlin.system.exitProcess

actual fun exit(status: Int): Nothing = exitProcess(status)
