import domain.Link
import domain.LinkContent

data class VisitedLink(
    val link: Link,
    val source: Link?,
    val depth: Int,
    val content: LinkContent,
)

data class ToVisitLink(
    val link: Link,
    val source: Link?,
    val depth: Int,
)

class LinkStore {

    private val visitedLinks: MutableMap<String, VisitedLink> = mutableMapOf()

    private val toVisitLinks: MutableMap<String, ToVisitLink> = mutableMapOf()

    val visited: Map<String, VisitedLink> get() = visitedLinks

    fun addVisited(link: ToVisitLink, content: LinkContent) {
        visitedLinks[link.link.value] = VisitedLink(link.link, link.source, link.depth, content)
    }

    fun addToVisit(link: Link, source: Link?, depth: Int) {
        toVisitLinks[link.value] = ToVisitLink(link, source, depth)
    }

    fun addToVisit(link: ToVisitLink) {
        toVisitLinks[link.link.value] = link
    }

    fun hasNextToVisit() = toVisitLinks.isNotEmpty()

    fun getNextToVisit(): ToVisitLink {
        check(hasNextToVisit()) { "No more links to visit" }
        val first = toVisitLinks.keys.first()
        return toVisitLinks.remove(first) ?: error("key has to be present")
    }

    fun getAllToVisitOrVisited() = visitedLinks.keys + toVisitLinks.keys
}
