import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

private lateinit var log: Logger

class Main : CliktCommand() {
    // TODO (MH): 12/6/20 retry
    // TODO (MH): 12/6/20 auth
    // TODO (MH): 12/6/20 multi threading
    // TODO (MH): 12/6/20 ignore specific domains
    private val url: String by argument(help = "Url to target")
    private val depth: Int by option(help = "Max recursion depth", names = arrayOf("-d", "--depth")).int()
        .restrictTo(min = 0)
        .default(3)

    private val noSummary: Boolean by option("--no-summary", help = "Don't show final summary. This option is forced if -q or --quiet was supplied").flag(default = false)

    enum class CrossDomainBehavior {
        IGNORE, DONT_RECURSE, UNCHANGED
    }

    private val crossDomain by option(help = """
        Behavior for links from other domains (ignore|dont-recurse|unchanged) default: dont-recurse        
        """.trimIndent())
        .choice("ignore", "dont-recurse", "unchanged")
        .convert { CrossDomainBehavior.valueOf(it.toUpperCase().replace("-", "_")) }
        .default(CrossDomainBehavior.DONT_RECURSE)

    private val logLevel by option(help = """Verbosity level:
        ```
            -q, --quiet:    no output
            default:        warnings and errors
            -v, --verbose:  verbose output
            --debug:        debug output
        ```
    """.trimIndent()).switch(
        "-q" to "0",
        "--quiet" to "0",  // print nothing
        "-v" to "2",       // verbose info
        "--verbose" to "2",
        "--debug" to "3",
    ).default("1")  // print urls

    private val LINK = Regex("""((?:ht|f)tps?://[-a-zA-Z0-9.]+\.[a-zA-Z]{2,3})(/[^"<]*)?""")

    private lateinit var urlDomain: String

    private fun validateArguments() {
        log = Logger(logLevel.toInt())
        urlDomain = getHost(url)
        log.info { "Targeting url: $url host: $urlDomain" }
    }

    override fun run() = runBlocking<Unit> {
        Platform.isMemoryLeakCheckerActive = false
        validateArguments()

        val visited = mutableMapOf<String, Boolean>()
        HttpClient().use { client ->
            val toVisit = mutableMapOf<String, Int>()
            toVisit[url] = 0
            // TODO (MH): 12/6/20 cross domain
            while (toVisit.isNotEmpty()) {
                val next = toVisit.keys.first()
                val nextDepth = toVisit.remove(toVisit.keys.first()) ?: error("key has to be present")
                if (nextDepth > depth) continue
                val nextLinks = client.getLinks(next)
                visited[next] = nextLinks == null
                log.info { "Getting: $next (depth: $nextDepth) -> Dead: ${visited[next]}" }
                val newLinks = nextLinks?.filter { it !in toVisit }?.map { it to nextDepth + 1 } ?: emptySequence()
                toVisit.putAll(newLinks)
            }
        }

        val deadLinks = visited.filterValues { it }
        if (!noSummary) {
            log.imporant { "Found ${deadLinks.size} dead links out of ${visited.size} visited urls." }
            deadLinks.forEach {
                log.info { it.key }
            }
        }
//        exitProcess(if (deadLinks.isEmpty()) 0 else 1)
    }

    private suspend fun HttpClient.getLinks(url: String): Sequence<String>? = runCatching {
        LINK.findAll(get<String>(url)).map { it.value }
    }.onFailure { log.debug { "Failed to get: $url $it" } }
        .getOrNull()
}

fun main(args: Array<String>) = Main().main(args)


private fun getHost(url: String): String = URLBuilder(url).build().host
