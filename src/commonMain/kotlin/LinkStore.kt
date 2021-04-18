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

    fun addVisited(link: VisitedLink) {
        visitedLinks[link.link.value] = link
    }

    fun addToVisit(links: List<ToVisitLink>) {
        links.associateBy { it.link.value }
            .filterKeys { it !in (visitedLinks.keys + toVisitLinks.keys) }
            .let(toVisitLinks::putAll)
    }

    fun hasNextToVisit() = toVisitLinks.isNotEmpty()

    fun getNextToVisit(): ToVisitLink {
        check(hasNextToVisit()) { "No more links to visit" }
        val first = toVisitLinks.keys.first()
        return toVisitLinks.remove(first) ?: error("key has to be present")
    }
}
