import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking

private lateinit var logger: Logger

class Main : CliktCommand() {
    private val url: String by argument(help = "Url to target")
    private val depth: Int by option(help = "Max recursion depth", names = arrayOf("-d", "--depth")).int().default(3)

    private val noSummary: Boolean by option("--no-summary", help = "Don't show final summary. This option is implied if -q or --quiet was supplied").flag(default = false)

    private val logLevel by option(help = """Verbosity level:```
            -q, --quiet:    no output
            default:        warnings and errors
            -v, --verbose:  verbose output
            --debug:        debug output
        ```
    """.trimIndent()).switch(
        "-q" to "0",
        "--quiet" to "0",
        "-v" to "2",
        "--verbose" to "2",
        "--debug" to "3",
    ).default("1")

    override fun run() = runBlocking<Unit> {
        Platform.isMemoryLeakCheckerActive = false
        validateArguments()


        HttpClient().use { client ->
            val content: String = client.get("https://en.wikipedia.org/wiki/Main_Page")
//            println(content)
        }
    }

    private fun validateArguments() {
        if (depth < 0) {
            throw UsageError(text = "Depth cannot be negative", paramName = "depth")
        }
        logger = Logger(logLevel.toInt())
    }
}

fun main(args: Array<String>) = Main().main(args)


