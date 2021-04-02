import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import domain.Link
import domain.LinkContent
import http.Client
import http.UrlExt.getHost
import kotlinx.coroutines.runBlocking

lateinit var log: Logger

private const val USER_INPUT_LINK = "<given by user input>"

class Main : CliktCommand() {
    // FIXME (MH): 1/2/21 url creating from base url

    // TODO (MH): 12/6/20 retry
    // TODO (MH): 12/6/20 multi threading

    // TODO (MH): 1/23/21 timeout per request

    // TODO (MH): 1/13/21 follow redirects
    // TODO rate limiting
    // TODO (MH): 1/23/21 when found same link from multiple sources, shows only the first one
    // TODO (MH): 2/11/21 skip fragments(anchors)

    // TODO (MH): 12/6/20 ignore specific domains, allow custom response codes per domain
    // TODO (MH): 1/13/21 allow to customize this
    private val allowedStatusCodes: List<Int> = (200..300).toList()

    private val url: String by argument(help = "Url to target")
    private val depth: Int by option(help = "Max recursion depth", names = arrayOf("-d", "--depth")).int()
        .restrictTo(min = 0)
        .default(99)

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
    ).default("1")  // print only summary

    private lateinit var urlDomain: String

    private fun validateArguments() {
        log = Logger(logLevel.toInt())
        urlDomain = getHost(url)
        log.debug { "Targeting url: $url host: $urlDomain" }
    }

    override fun run() = runBlocking<Unit> {
        validateArguments()

        val linksStore = LinkStore()
        val httpClient = Client(allowedStatusCodes, requestHeaders)

        httpClient.use { client ->
            linksStore.addToVisit(Link.Absolute(url), null, 0)
            while (linksStore.hasNextToVisit()) {
                val nextToVisit = linksStore.getNextToVisit()
                val nextLinkContent = client.getContent(nextToVisit.link.value)
                if (nextLinkContent is LinkContent.Success) {
                    val newLinks = getNewLinks(nextLinkContent.content, nextToVisit, linksStore.getAllToVisitOrVisited())
                    newLinks.forEach { linksStore.addToVisit(it) }
                }
                linksStore.addVisited(nextToVisit, nextLinkContent)
                log.verbose { "${nextLinkContent.statusString} (depth: ${nextToVisit.depth}): ${nextToVisit.link.value} <- ${nextToVisit.source?.value ?: USER_INPUT_LINK}" }
            }
        }

        val visitedLinks = linksStore.visited.values
        val visitedCount = visitedLinks.size
        val deadLinks = visitedLinks.filter {
            when (it.content) {
                is LinkContent.Success, LinkContent.UnreadableSuccess -> false
                is LinkContent.InvalidStatusCode, is LinkContent.Unreachable -> true
            }
        }
        if (!noSummary) {
            if (deadLinks.isEmpty()) {
                log.default { "\nNo dead links found out of $visitedCount visited urls" }
            } else {
                log.default { "\nFound ${deadLinks.size} dead links out of $visitedCount visited urls:" }
                deadLinks.forEach { log.default { "${it.link.value}  <- ${it.source?.value ?: USER_INPUT_LINK} :: ${it.content.statusString}" } }
            }
        }
    }

    private fun getNewLinks(content: String, link: ToVisitLink, toVisitOrVisited: Set<String>): List<ToVisitLink> {
        val visitedLinkUrl = link.link.value
        if (link.link is Link.Anchor) {
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
            .map { ToVisitLink(it, link.link, link.depth + 1) }
            .filter { (link, _, linkDepth) ->
                when (crossDomain) {
                    CrossDomainBehavior.IGNORE -> getHost(link.value) == urlDomain
                    CrossDomainBehavior.DONT_RECURSE -> linkDepth <= 1 || getHost(link.value) == urlDomain
                    CrossDomainBehavior.UNCHANGED -> true
                }
            }.toList()
        log.debug { "Found ${candidateLinks.size} new links at $visitedLinkUrl" }
        return candidateLinks
    }

    companion object {
        fun main(args: Array<String>) = Main().main(args)
    }
}


