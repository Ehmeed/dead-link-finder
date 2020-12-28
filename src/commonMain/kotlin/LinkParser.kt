
object LinkParser {
    private val LINK = Regex("""((?:ht|f)tps?://[-a-zA-Z0-9.]+\.[a-zA-Z]{2,3})(/[^"<]*)?""")

    fun getLinks(htmlContent: String): List<String> =
        LINK.findAll(htmlContent).map { it.value }.distinct().toList()
}
