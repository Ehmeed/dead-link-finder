import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private lateinit var config: Config

class Main : CliktCommand() {
    // TODO (MH): 4/28/21 client side rendered sites (probably for jvm only - HtmlUnit)
    // TODO (MH): 5/9/21 consider fail fast flag
    // TODO (MH): 4/18/21 nice output formatting with tabs maybe so it';s  machine readable
    // TODO (MH): 5/9/21 better base image for native binary
    // TODO (MH): 5/3/21 cached builder; jvm image; native image

    // TODO (MH): 2/11/21 skip fragments(anchors) = study (maybe they return 200 everytime) - check if it is possible to verify anchor existence
    // TODO (MH): 4/20/21 putting unreachable URL as argument crashes with exception

    // TODO (MH): 1/23/21 when found same link from multiple sources, shows only the first one
    // TODO (MH): 12/6/20 retry
    // TODO (MH): 12/6/20 multi threading
    // TODO rate limiting


    // TODO config json file
    // TODO (MH): 12/6/20 ignore specific domains, allow custom response codes per domain
    // TODO (MH): 1/13/21 allow to customize this
    private val allowedStatusCodes: List<Int> = (200..300).toList()

    private val url: String by argument(help = "Url to target")
    private val depth: Int by option(help = "Max recursion depth", names = arrayOf("-d", "--depth")).int()
        .restrictTo(min = 0)
        .default(1_000_000)

    private val timeout: Int by option(help = "Timeout per request in milliseconds (default 5000ms)").int()
        .restrictTo(min = 1)
        .default(5000)

    private val parseText: Boolean by option("--show-text", help = "Shows text displayed on the element with each link. This is inaccurate and also will slow down execution.")
        .flag(default = false)

    private val requestHeaders: List<Pair<String, String>> by option("-H", help = "Add header, e.g.: -H 'User-Agent:Mozilla:4.0'")
        .convert {
            val split = it.split(":", limit = 2)
            require(split.size == 2) { "Invalid header format use: header name:header value" }
            split[0] to split[1]
        }.multiple()

    enum class CrossDomainBehavior {
        IGNORE, DONT_RECURSE, UNCHANGED
    }

    private val crossDomain by option(
        help = """
        Behavior for links from other domains (ignore|dont-recurse|unchanged) default: dont-recurse        
        """.trimIndent()
    )
        .choice("ignore", "dont-recurse", "unchanged")
        .convert { CrossDomainBehavior.valueOf(it.toUpperCase().replace("-", "_")) }
        .default(CrossDomainBehavior.DONT_RECURSE)

    private val noSummary: Boolean by option("--no-summary", help = "Don't show final summary. This option is forced if -q or --quiet was supplied").flag(default = false)

    private val logLevel by option(
        help = """Verbosity level:
        ```
            -q, --quiet:    no output
            default:        prints only dead links
            -v, --verbose:  prints all targeted links
            --debug:        debug output
        ```
        """.trimIndent()
    ).switch(
        "-q" to "0", // print nothing
        "--quiet" to "0", // print nothing
        "-v" to "2", // verbose info
        "--verbose" to "2", // verbose info
        "--debug" to "3", // print everything
    ).default("1") // print only summary

    override fun run() {
        log = Logger(logLevel.toInt())
        if (url == "version") {
            log.force { "Dead link finder $VERSION" }
            exit(0)
        }

        config = Config(
            allowedStatusCodes = allowedStatusCodes,
            url = url,
            depth = depth,
            parseText = parseText,
            noSummary = noSummary,
            requestHeaders = requestHeaders,
            crossDomainBehavior = crossDomain,
            logLevel = logLevel,
            timeout = timeout,
        )
    }

    companion object {
        @OptIn(ExperimentalTime::class)
        suspend fun main(args: Array<String>) {
            val executionTime = measureTime {
                Main().main(args)
                runner(config)
            }
            if (!config.noSummary) {
                log.verbose { "Execution finished in ${executionTime.toIsoString()}" }
            }
        }
    }
}
