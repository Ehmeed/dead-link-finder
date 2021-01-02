object LinkParser {
    private val HREF = Regex("""<a[^>]* href="([^"]*)""")

    fun getLinks(htmlContent: String, baseUrl: String): List<Link> {
        val cleanBaseUrl = cleanBaseUrl(baseUrl)
        return HREF.findAll(htmlContent)
            .map { it.groupValues[1] }
            .distinct()
            .map { Link(it, cleanBaseUrl) }
            .toList()
    }

    private fun Link(value: String, baseUrl: String): Link = when {
        value.startsWith("#") -> Link.Anchor(value, baseUrl)
        value.startsWith("http") -> Link.Absolute(value)
        value.startsWith("mailto") -> Link.Mailto(value)
        else -> Link.Relative(value, baseUrl)
    }

    private fun cleanBaseUrl(baseUrl: String): String {
        return if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
    }
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

    class Relative(override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl + rawValue
    }

    data class Mailto(override val rawValue: String) : Link() {
        override val value: String = rawValue

        val address = rawValue.removePrefix("mailto:")
    }
}
