import domain.Link
import domain.LinkContent
import http.Client
import http.UrlExt

private const val USER_INPUT_LINK = "<given by user input>"

suspend fun runner(config: Config) {
    val linksStore = LinkStore()
    val httpClient = Client(config.allowedStatusCodes, config.requestHeaders, config.timeout)

    httpClient.use { client ->
        linksStore.addToVisit(listOf(ToVisitLink(Link.Absolute(USER_INPUT_LINK, config.url), source = null, depth = 0)))
        while (linksStore.hasNextToVisit()) {
            val nextToVisit = linksStore.getNextToVisit()
            val nextToVisitContent = client.getContent(nextToVisit.link.value)
            val visitedLink = VisitedLink(nextToVisit.link, nextToVisit.source, nextToVisit.depth, nextToVisitContent)
            linksStore.addVisited(visitedLink)
            if (visitedLink.content is LinkContent.Success) {
                getNewLinks(visitedLink.content.content, nextToVisit, config)
                    .let(linksStore::addToVisit)
            }
            val linkLogger = if (visitedLink.content.isSuccess) { it: () -> String -> log.verbose(it) } else { it: () -> String -> log.default(it) }
            linkLogger { formatLink(visitedLink) }
        }
    }

    val visitedLinks = linksStore.visited.values
    val visitedCount = visitedLinks.size
    val deadLinks = visitedLinks.filter { !it.content.isSuccess }
    if (!config.noSummary) {
        if (deadLinks.isEmpty()) {
            log.default { "No dead links found out of $visitedCount visited urls" }
        } else {
            log.default { "Found ${deadLinks.size} dead links out of $visitedCount visited urls:" }
            deadLinks.forEach { log.default { formatLink(it) } }
        }
    }

}

private fun getNewLinks(content: String, link: ToVisitLink, config: Config): List<ToVisitLink> {
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
                Main.CrossDomainBehavior.IGNORE -> UrlExt.getHost(link.value) == config.urlDomain
                Main.CrossDomainBehavior.DONT_RECURSE -> linkDepth <= 1 || UrlExt.getHost(link.value) == config.urlDomain
                Main.CrossDomainBehavior.UNCHANGED -> true
            }
        }.toList()
    log.debug { "Found ${candidateLinks.size} links at $visitedLinkUrl" }
    return candidateLinks
}

private fun formatLink(visited: VisitedLink): String {
    val linkText = visited.link.text?.let { " with text: ${it.replace('\n', ' ')}" } ?: ""
    return "${visited.content.statusString} (depth: ${visited.depth}) :: ${visited.link.value} found at ${visited.source?.value ?: USER_INPUT_LINK}$linkText"
}
