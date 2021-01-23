class Logger(private val level: Int) {

    init {
        debug { "Initialized logger with level $level" }
    }

    fun default(lazyMessage: () -> String) {
        if (level >= 1) sout(lazyMessage)
    }

    fun verbose(lazyMessage: () -> String) {
        if (level >= 2) sout(lazyMessage)
    }

    fun debug(lazyMessage: () -> String) {
        if (level >= 3) sout(lazyMessage)
    }

    private fun sout(lazyMessage: () -> String) {
        println(lazyMessage())
    }
}
