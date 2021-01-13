object LinkParser {
    private val HREF = Regex("""<a[^>]* href="([^"]*)""")

    fun getLinks(htmlContent: String, sourcePageUrl: String): List<Link> {
        val normalizedSourcePageUrl = normalizeUrl(sourcePageUrl)
        val normalizedDomainUrl = Ext.removeUrlPath(sourcePageUrl)
        return HREF.findAll(htmlContent)
            .map { it.groupValues[1] }
            .distinct()
            .map { Link(it, normalizedSourcePageUrl, normalizeUrl(normalizedDomainUrl)) }
            .toList()
    }

    private fun Link(value: String, sourcePageUrl: String, domainUrl: String): Link = when {
        value.startsWith("/") -> Link.RootRelative(value, domainUrl)
        value.startsWith("http") -> Link.Absolute(value)
        value.startsWith("#") -> Link.Anchor(value, sourcePageUrl)
        value.startsWith("mailto") -> Link.Mailto(value)
        else -> Link.Relative(value, sourcePageUrl)
    }

    private fun normalizeUrl(baseUrl: String): String = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
}

sealed class Link {
    abstract val value: String
    abstract val rawValue: String

    class Anchor(override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl + rawValue
    }

    data class Absolute(override val rawValue: String) : Link() {
        override val value: String = rawValue
    }

    // FIXME (MH): 1/13/21 this will probs fail when baseUrl contains some params or fragment
    class Relative(override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl + rawValue
    }

    class RootRelative(override val rawValue: String, domainUrl: String) : Link() {
        override val value: String = domainUrl + rawValue.drop(1)
    }

    data class Mailto(override val rawValue: String) : Link() {
        override val value: String = rawValue

        val address = rawValue.removePrefix("mailto:")
    }
}
