import domain.Link
import http.Ext

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
