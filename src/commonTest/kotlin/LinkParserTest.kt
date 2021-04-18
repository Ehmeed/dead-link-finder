import io.ktor.http.*
import kotlin.test.*

expect val zahradnictviKarlovHtml: String

class LinkParserTest {

    @Test
    fun emptyPageReturnsNoLinks() {
        assertEquals(emptyList(), LinkParser.getLinks("", "http://webThemez.com"))
    }

    @Test
    fun invalidUrlThrows() {
        assertFailsWith<URLParserException> {
            assertEquals(emptyList(), LinkParser.getLinks("", ""))
        }
    }

    @Test
    fun zahradnictviKarlovReturnsExpectedLinks() {
        assertTrue { zahradnictviKarlovHtml.isNotBlank() }
        val expected = listOf(
            "http://webThemez.com",
            "http://zahradnictvi-karlov.rajce.idnes.cz/",
            "http://zahradnictvikarlov.cz#",
            "http://zahradnictvikarlov.cz#aboutUs",
            "http://zahradnictvikarlov.cz#carousel",
            "http://zahradnictvikarlov.cz#contactUs",
            "http://zahradnictvikarlov.cz#home",
            "http://zahradnictvikarlov.cz#top",
            "http://zahradnictvikarlov.cz#work",
            "http://zahradnictvikarlov.cz/images/work/1.jpg",
            "http://zahradnictvikarlov.cz/images/work/2.jpg",
            "http://zahradnictvikarlov.cz/images/work/3.jpg",
            "http://zahradnictvikarlov.cz/images/work/4.jpg",
            "http://zahradnictvikarlov.cz/images/work/5.jpg",
            "http://zahradnictvikarlov.cz/images/work/6.jpg",
            "http://zahradnictvikarlov.cz/images/work/7.jpg",
            "http://zahradnictvikarlov.cz/images/work/8.jpg",
            "https://mapy.cz/s/toCP",
            "mailto:info@zahradnictvikarlov.cz",
        ).sorted()
        val actual = LinkParser.getLinks(zahradnictviKarlovHtml, "http://zahradnictvikarlov.cz")
            .map { it.value }.sorted()
        assertEquals(expected, actual)
    }
}
