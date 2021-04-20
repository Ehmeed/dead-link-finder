import domain.Link
import http.UrlExt

object LinkParser {
    private val HREF_WITH_TEXT = Regex("""<a[^>]* href="([^"]*)"[^>]*>([\s\S]*?)(?=</a>)""")
    private val HREF = Regex("""<a[^>]* href="([^"]*)""")

    fun getLinks(htmlContent: String, sourcePageUrl: String, parseText: Boolean): List<Link> {
        val normalizedSourcePageUrl = normalizeUrl(sourcePageUrl.substringBeforeLast("#"))
        val normalizedDomainUrl = normalizeUrl(UrlExt.removeUrlPath(sourcePageUrl))
        return parseLinks(htmlContent, parseText)
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

    internal fun Link(value: String, text: String?, sourcePageUrl: String, domainUrl: String): Link = when {
        value.startsWith("/") -> Link.RootRelative(text, value, domainUrl)
        value.startsWith("#") -> Link.Anchor(text, value, sourcePageUrl)
        value.startsWith("mailto") -> Link.Mailto(text, value)
        value.startsWith("http") || value.startsWith("https") -> Link.Absolute(text, value)
        else -> Link.Relative(text, value, sourcePageUrl)
    }

    private fun normalizeUrl(baseUrl: String): String = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl

    private fun parseLinks(html: String, parseText: Boolean): Sequence<Pair<String, String?>> = if (parseText) {
        HREF_WITH_TEXT.findAll(html)
            .map { it.groupValues[1] to it.groupValues[2] }
    } else {
        HREF.findAll(html)
            .map { it.groupValues[1] to null }
    }
}
