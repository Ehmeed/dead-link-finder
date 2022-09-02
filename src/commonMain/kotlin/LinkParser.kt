import domain.Link
import http.UrlExt

object LinkParser {
    private val HREF_WITH_TEXT = Regex("""<a[^>]* href="([^"]*)"[^>]*>([\s\S]*?)(?=</a>)""")
    private val HREF = Regex("""<a[^>]* href="([^"]*)""")

    fun getLinks(htmlContent: String, sourcePageUrl: String, parseText: Boolean, parseSitemap: Boolean): List<Link> {
        val normalizedSourcePageUrl = normalizeUrl(sourcePageUrl.substringBeforeLast("#"))
        val normalizedDomainUrl = normalizeUrl(UrlExt.removeUrlPath(sourcePageUrl))
        return parseLinks(htmlContent, parseText, parseSitemap)
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
        value.startsWith(Link.Mailto.PREFIX) -> Link.Mailto(text, value)
        value.startsWith(Link.Tel.PREFIX) -> Link.Tel(text, value)
        value.startsWith("http") || value.startsWith("https") -> Link.Absolute(text, value)
        else -> Link.Relative(text, value, sourcePageUrl)
    }

    private fun normalizeUrl(baseUrl: String): String = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl

    private fun parseLinks(html: String, parseText: Boolean, sitemap: Boolean): List<Pair<String, String?>> {
        val links = if (parseText) {
            HREF_WITH_TEXT.findAll(html)
                .map { it.groupValues[1] to extractText(it.groupValues[2]) }
        } else {
            HREF.findAll(html)
                .map { it.groupValues[1] to null }
        }.toMutableList()
        if (sitemap) {
            Regex("""<loc>([\s\S]*?)</loc>""").findAll(html).map { it.groupValues[1] to null }.toList().also {
                links.addAll(it)
            }
        }

        return links
    }

    private val INSIDE_TAG = Regex(""">([^<>]*)<""")
    private val TITLE = Regex("""title="(.*?)"""")
    internal fun extractText(htmlFragment: String): String? {
        if (!htmlFragment.trim().startsWith("<")) return htmlFragment
        val bestMatch = INSIDE_TAG.findAll(htmlFragment)
            .map { it.groupValues[1] }
            .filter { it.isNotBlank() && it.length < 60 }
            .maxByOrNull { it.length }
        return bestMatch ?: TITLE.find(htmlFragment)?.groupValues?.get(1)
    }
}
