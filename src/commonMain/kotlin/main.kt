import Ext.getHost
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking

private lateinit var log: Logger

class Main : CliktCommand() {
    // FIXME (MH): 1/2/21 url creating from base url

    // TODO (MH): 12/6/20 retry
    // TODO (MH): 12/6/20 multi threading
    // TODO (MH): 12/6/20 ignore specific domains

    // TODO (MH): 1/13/21 follow redirects (optionally)
    // TODO (MH): 1/13/21 allow to customize this
    private val allowedStatusCodes: List<Int> = (200..300).toList()

    private val url: String by argument(help = "Url to target")
    private val depth: Int by option(help = "Max recursion depth", names = arrayOf("-d", "--depth")).int()
        .restrictTo(min = 0)
        .default(3)

    private val noSummary: Boolean by option("--no-summary", help = "Don't show final summary. This option is forced if -q or --quiet was supplied").flag(default = false)

    private val requestHeaders: List<Pair<String, String>> by option("-H", help = "Add header, e.g.: -H User-Agent:Mozilla:4.0")
        .convert {
            val split = it.split(":", limit = 2)
            require(split.size == 2) { "Invalid header format" }
            split[0] to split[1]
        }.multiple()

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
        "-q" to "0",       // print nothing
        "--quiet" to "0",  // print nothing
        "-v" to "2",       // verbose info
        "--verbose" to "2",// verbose info
        "--debug" to "3",  // print everything
    ).default("1")  // print urls

    private lateinit var urlDomain: String

    private fun validateArguments() {
        log = Logger(logLevel.toInt())
        urlDomain = getHost(url)
        log.important { "Targeting url: $url host: $urlDomain" }
    }

    override fun run() = runBlocking<Unit> {
        validateArguments()

        val linksStore = LinkStore()
        val httpClient = HttpClient() {
            expectSuccess = false
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    if (statusCode !in allowedStatusCodes) throw RuntimeException("Disallowed status code: $statusCode")
                }
            }
        }
        httpClient.use { client ->
            linksStore.addToVisit(Link.Absolute(url), 0)
            while (linksStore.hasNextToVisit()) {
                val nextToVisit = linksStore.getNextToVisit()
                val nextLinkContent = client.getContent(nextToVisit.link.value)
                val isDead = nextLinkContent == null
                linksStore.addVisited(nextToVisit, isDead)
                val status = if (isDead) "DEAD" else "OK  "
                log.info { "$status (depth: ${nextToVisit.depth}): ${nextToVisit.link.value}" }
                if (!isDead) {
                    val content = nextLinkContent ?: error("Content of not dead link cannot be null")
                    val newLinks = getNewLinks(content, nextToVisit, linksStore.getAllToVisitOrVisited())
                    newLinks.forEach { linksStore.addToVisit(it) }
                }
            }
        }

        val deadLinks = linksStore.getDead()
        if (!noSummary) {
            if (deadLinks.isEmpty()) {
                log.important { "\nNo dead links found out of ${linksStore.visitedSize()} visited urls" }
            } else {
                log.important { "\nFound ${deadLinks.size} dead links out of ${linksStore.visitedSize()} visited urls:" }
                deadLinks.forEach { log.important { it } }
            }
        }
    }

    private fun getNewLinks(content: String, link: ToVisitLink, toVisitOrVisited: Set<String>): List<ToVisitLink> {
        val visitedLinkUrl = link.link.value
        if (link.link is Link.Anchor && link.depth >= 1) {
            log.debug { "Ignoring nested links in anchor: $visitedLinkUrl" }
            return emptyList()
        }
        if (link.depth == depth) {
            log.debug { "Reached maximum depth ($depth) for: $visitedLinkUrl" }
            return emptyList()
        }
        val newLinks = LinkParser.getLinks(content, visitedLinkUrl)

        val candidateLinks = newLinks.asSequence()
            .filter { it !is Link.Mailto }
            .filter { it.value !in toVisitOrVisited }
            .map { ToVisitLink(it, link.depth + 1) }
            .filter { (link, linkDepth) ->
                when (crossDomain) {
                    CrossDomainBehavior.IGNORE -> getHost(link.value) == urlDomain
                    CrossDomainBehavior.DONT_RECURSE -> linkDepth <= 1 || getHost(link.value) == urlDomain
                    CrossDomainBehavior.UNCHANGED -> true
                }
            }.toList()
        log.debug { "Found ${candidateLinks.size} new links at $visitedLinkUrl" }
        return candidateLinks
    }

    private suspend fun HttpClient.getContent(url: String): String? = try {
        get<String>(url) {
            requestHeaders.forEach { header(it.first, it.second) }
        }
    } catch (ex: MalformedInputException) {
        log.debug { "Cannot read page as string, but status is accepted" }
        ""
    } catch (ex: RuntimeException) {
        log.debug { "Failed to get: $url" }
        null
    }

    companion object {
        fun main(args: Array<String>) = Main().main(args)
    }
}


