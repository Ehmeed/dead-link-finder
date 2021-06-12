import domain.Link
import domain.LinkContent
import http.Client
import http.UrlExt

private const val USER_INPUT_LINK = "<given by user input>"

suspend fun runner(config: Config) {
    val urlDomain = UrlExt.getHost(config.url)
    log.default { "Targeting url: ${config.url} host: $urlDomain" }
    val linksStore = LinkStore()
    installShutdownHook(linksStore, config)
    val httpClient = Client(config.allowedStatusCodes, config.requestHeaders, config.timeout)

    httpClient.use { client ->
        linksStore.addToVisit(listOf(ToVisitLink(Link.Absolute(USER_INPUT_LINK, config.url), source = null, depth = 0)))
        while (linksStore.hasNextToVisit()) {
            val nextToVisit = linksStore.getNextToVisit()
            val nextToVisitContent = client.getContent(nextToVisit.link.value)
            val visitedLink = VisitedLink(nextToVisit.link, nextToVisit.source, nextToVisit.depth, nextToVisitContent)
            linksStore.addVisited(visitedLink)
            if (visitedLink.content is LinkContent.Success) {
                getNewLinks(visitedLink.content.content, nextToVisit, config, urlDomain)
                    .let(linksStore::addToVisit)
            }
            val linkLogger = if (visitedLink.content.isSuccess) { it: () -> String -> log.verbose(it) } else { it: () -> String -> log.default(it) }
            linkLogger { formatLink(visitedLink) }
        }
    }

    val hasDeadLinks = printSummaryAndGetStatus(linksStore, config.noSummary)
    exit(if (hasDeadLinks) 1 else 0)
}

private fun getNewLinks(content: String, link: ToVisitLink, config: Config, urlDomain: String): List<ToVisitLink> {
    val visitedLinkUrl = link.link.value
    if (link.depth == config.depth) {
        log.debug { "Reached maximum depth (${config.depth}) for: $visitedLinkUrl" }
        return emptyList()
    }
    val newLinks = LinkParser.getLinks(content, visitedLinkUrl, config.parseText)

    val candidateLinks = newLinks.asSequence()
        .filterNot { it is Link.Mailto }
        .map { ToVisitLink(it, link.link, link.depth + 1) }
        .filter { (link, _, linkDepth) ->
            when (config.crossDomainBehavior) {
                Main.CrossDomainBehavior.IGNORE -> UrlExt.getHost(link.value) == urlDomain
                Main.CrossDomainBehavior.DONT_RECURSE -> linkDepth <= 1 || UrlExt.getHost(link.value) == urlDomain
                Main.CrossDomainBehavior.UNCHANGED -> true
            }
        }.toList()
    log.debug { "Found ${candidateLinks.size} links at $visitedLinkUrl" }
    return candidateLinks
}

internal fun printSummaryAndGetStatus(linksStore: LinkStore, disableSummary: Boolean): Boolean {
    val visitedLinks = linksStore.visited.values
    val deadLinks = visitedLinks.filter { !it.content.isSuccess }
    if (!disableSummary) {
        val visitedCount = visitedLinks.size
        if (deadLinks.isEmpty()) {
            log.default { "No dead links found out of $visitedCount visited urls" }
        } else {
            log.default { "Found ${deadLinks.size} dead links out of $visitedCount visited urls:" }
            deadLinks.forEach { log.default { formatLink(it) } }
        }
    }
    return deadLinks.isNotEmpty()
}

private fun formatLink(visited: VisitedLink): String {
    val linkText = visited.link.text?.let { " with text: ${it.replace('\n', ' ')}" } ?: ""
    return "${visited.content.statusString} (depth: ${visited.depth}) :: ${visited.link.value} found at ${visited.source?.value ?: USER_INPUT_LINK}$linkText"
}

expect fun exit(status: Int): Nothing

expect fun installShutdownHook(store: LinkStore, config: Config)
