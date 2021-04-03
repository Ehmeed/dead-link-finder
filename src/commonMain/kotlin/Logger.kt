class Logger(private val level: Int) {

    companion object {
        // FIXME (MH): 4/3/21 this is shit
        lateinit var log: Logger
    }

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
