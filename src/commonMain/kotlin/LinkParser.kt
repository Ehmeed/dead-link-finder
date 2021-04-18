import domain.Link
import http.UrlExt

object LinkParser {
    private val HREF = Regex("""<a[^>]* href="([^"]*)"[^>]*>([\s\S]*?)(?=</a>)""")

    fun getLinks(htmlContent: String, sourcePageUrl: String): List<Link> {
        val normalizedSourcePageUrl = normalizeUrl(sourcePageUrl.substringBeforeLast("#"))
        val normalizedDomainUrl = normalizeUrl(UrlExt.removeUrlPath(sourcePageUrl))
        return HREF.findAll(htmlContent)
            .map { it.groupValues[1] to it.groupValues[2] }
            .distinctBy { it.first }
            .map {
                Link(
                    value = it.first,
                    text = it.second,
                    sourcePageUrl = normalizedSourcePageUrl,
                    domainUrl = normalizedDomainUrl
                )
            }
            .toList()
    }

    internal fun Link(value: String, text: String, sourcePageUrl: String, domainUrl: String): Link = when {
        value.startsWith("/") -> Link.RootRelative(text, value, domainUrl)
        value.startsWith("#") -> Link.Anchor(text, value, sourcePageUrl)
        value.startsWith("mailto") -> Link.Mailto(text, value)
        value.startsWith("http") || value.startsWith("https") -> Link.Absolute(text, value)
        else -> Link.Relative(text, value, sourcePageUrl)
    }

    private fun normalizeUrl(baseUrl: String): String = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
}
