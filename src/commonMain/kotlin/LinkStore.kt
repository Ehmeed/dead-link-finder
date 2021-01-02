class LinkStore {

    private val visitedLinks: MutableMap<String, VisitedLink> = mutableMapOf()

    private val toVisitLinks: MutableMap<String, ToVisitLink> = mutableMapOf()

    fun addVisited(link: ToVisitLink, dead: Boolean) {
        visitedLinks[link.link.value] = VisitedLink(link.link, link.depth, dead)
    }

    fun getDead() = visitedLinks.filterValues { it.dead }.map { it.key }

    fun visitedSize() = visitedLinks.size

    fun addToVisit(link: Link, depth: Int) {
        toVisitLinks[link.value] = ToVisitLink(link, depth)
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


data class VisitedLink(
    val link: Link,
    val depth: Int,
    val dead: Boolean,
)

data class ToVisitLink(
    val link: Link,
    val depth: Int,
)
