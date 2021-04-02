import domain.Link
import http.UrlExt

object LinkParser {
    private val HREF = Regex("""<a[^>]* href="([^"]*)""")

    fun getLinks(htmlContent: String, sourcePageUrl: String): List<Link> {
        val normalizedSourcePageUrl = normalizeUrl(sourcePageUrl.substringBeforeLast("#"))
        val normalizedDomainUrl = normalizeUrl(UrlExt.removeUrlPath(sourcePageUrl))
        return HREF.findAll(htmlContent)
            .map { it.groupValues[1] }
            .distinct()
            .map { Link(value = it, sourcePageUrl = normalizedSourcePageUrl, domainUrl = normalizedDomainUrl) }
            .toList()
    }

    internal fun Link(value: String, sourcePageUrl: String, domainUrl: String): Link = when {
        value.startsWith("/") -> Link.RootRelative(value, domainUrl)
        value.startsWith("#") -> Link.Anchor(value, sourcePageUrl)
        value.startsWith("mailto") -> Link.Mailto(value)
        value.startsWith("http") || value.startsWith("https") -> Link.Absolute(value)
        else -> Link.Relative(value, sourcePageUrl)
    }

    private fun normalizeUrl(baseUrl: String): String = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
}
