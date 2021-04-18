import domain.Link
import domain.LinkContent
import http.Client
import http.UrlExt

private const val USER_INPUT_LINK = "<given by user input>"


suspend fun runner(config: Config) {
    val linksStore = LinkStore()
    val httpClient = Client(config.allowedStatusCodes, config.requestHeaders)

    httpClient.use { client ->
        linksStore.addToVisit(Link.Absolute(config.url), source = null, depth = 0)
        while (linksStore.hasNextToVisit()) {
            val nextToVisit = linksStore.getNextToVisit()
            val nextLinkContent = client.getContent(nextToVisit.link.value)
            if (nextLinkContent is LinkContent.Success) {
                val newLinks = getNewLinks(nextLinkContent.content, nextToVisit, linksStore.getAllToVisitOrVisited(), config)
                newLinks.forEach { linksStore.addToVisit(it) }
            }
            linksStore.addVisited(nextToVisit, nextLinkContent)
            val linkLogger = if (nextLinkContent.isSuccess) { it: () -> String -> log.verbose(it) } else { it: () -> String -> log.default(it) }
            linkLogger { "${nextLinkContent.statusString} (depth: ${nextToVisit.depth}): ${nextToVisit.link.value} <- ${nextToVisit.source?.value ?: USER_INPUT_LINK}" }
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
            deadLinks.forEach { log.default { "${it.link.value} <- ${it.source?.value ?: USER_INPUT_LINK} :: ${it.content.statusString}" } }
        }
    }

}

private fun getNewLinks(content: String, link: ToVisitLink, toVisitOrVisited: Set<String>, config: Config): List<ToVisitLink> {
    val visitedLinkUrl = link.link.value
    if (link.link is Link.Anchor) {
        log.debug { "Ignoring nested links in anchor: $visitedLinkUrl" }
        return emptyList()
    }
    if (link.depth == config.depth) {
        log.debug { "Reached maximum depth ($config.depth) for: $visitedLinkUrl" }
        return emptyList()
    }
    val newLinks = LinkParser.getLinks(content, visitedLinkUrl)

    val candidateLinks = newLinks.asSequence()
        .filter { it !is Link.Mailto }
        .filter { it.value !in toVisitOrVisited }
        .map { ToVisitLink(it, link.link, link.depth + 1) }
        .filter { (link, _, linkDepth) ->
            when (config.crossDomainBehavior) {
                Main.CrossDomainBehavior.IGNORE -> UrlExt.getHost(link.value) == config.urlDomain
                Main.CrossDomainBehavior.DONT_RECURSE -> linkDepth <= 1 || UrlExt.getHost(link.value) == config.urlDomain
                Main.CrossDomainBehavior.UNCHANGED -> true
            }
        }.toList()
    log.debug { "Found ${candidateLinks.size} new links at $visitedLinkUrl" }
    return candidateLinks
}
