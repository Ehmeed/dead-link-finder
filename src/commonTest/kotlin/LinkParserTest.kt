import domain.Link
import io.ktor.http.*
import kotlin.test.*

class LinkParserTest {

    @Test
    fun emptyPageReturnsNoLinks() {
        assertEquals(emptyList(), LinkParser.getLinks("", "http://webThemez.com", true))
    }

    @Test
    fun invalidUrlThrows() {
        assertFailsWith<URLParserException> {
            assertEquals(emptyList(), LinkParser.getLinks("", "", true))
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
        val actual = LinkParser.getLinks(zahradnictviKarlovHtml, "http://zahradnictvikarlov.cz", false)
            .map { it.value }.sorted()
        assertEquals(expected, actual)
        val actualWithText = LinkParser.getLinks(zahradnictviKarlovHtml, "http://zahradnictvikarlov.cz", true)
            .map { it.value }.sorted()
        assertEquals(expected, actualWithText)
    }

    @Test
    fun validateLinkWithTagsInside() {
        val links = LinkParser.getLinks(
            """to <a href="/docs/s-analytics/form-model-analysis"><strong>analyse a form model</strong></a>""",
            "https://zoe.lundegaard.ai/docs/s-analytics/guides/form-tracking-with-webdata/",
            parseText = true
        ).filter { it.rawValue == "/docs/s-analytics/form-model-analysis" }

        assertEquals(links.size, 1)
        val actual = links.single()

        val expected = Link.RootRelative("analyse a form model",
            "/docs/s-analytics/form-model-analysis",
            "https://zoe.lundegaard.ai/")

        assertEquals(expected.rawValue, actual.rawValue)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.value, actual.value)
    }
    @Test
    fun validateLinkWithoutTagsInsideWithNewline() {
        val links = LinkParser.getLinks(
            """<a href="/docs/s-analytics/form-model-analysis">analyse a form
model</a>""",
            "https://zoe.lundegaard.ai/docs/s-analytics/guides/form-tracking-with-webdata/",
            parseText = true
        ).filter { it.rawValue == "/docs/s-analytics/form-model-analysis" }

        assertEquals(links.size, 1)
        val actual = links.single()

        val expected = Link.RootRelative("analyse a form\nmodel",
            "/docs/s-analytics/form-model-analysis",
            "https://zoe.lundegaard.ai/")

        assertEquals(expected.rawValue, actual.rawValue)
        assertEquals(expected.text, actual.text)
        assertEquals(expected.value, actual.value)
    }

    @Test
    fun `stopship`() {
        names.split("\n")
            .map { LinkParser.extractText(it) }
            .sortedBy { it?.length }
            .forEach { println(it) }
    }

    @Test
    fun parsingTextSimple() {
        val input = """
            Some text
        """.trimIndent()

        assertEquals("Some text", LinkParser.extractText(input))
    }

    @Test
    fun parsingTextSimple2() {
        val input = """
            <b>Some text</b>
        """.trimIndent()

        assertEquals("Some text", LinkParser.extractText(input))
    }

    @Test
    fun parsingTextStyle() {
        val input = """
            <style data-emotion-css="15k7mhn">.css-15k7mhn{box-sizing:border-box;margin:0;min-width:0;padding:1px 2px;color:#e8852b;}</style><code class="css-15k7mhn">register</code>
        """.trimIndent()

        assertEquals("register", LinkParser.extractText(input))
    }

    @Test
    fun parsingTextTitle() {
        val input = """
            <style data-emotion-css="1h2op9t">.css-1h2op9t{box-sizing:border-box;margin:0;min-width:0;max-width:100%;height:auto;margin-bottom:8px;}</style><img alt="Zoe Behavioral Prescoring" title="Zoe Behavioral Prescoring" src="/docs/static/dashboard-aa67c95b9f0f521fd282cd5bc5bfd0cf.png" class="css-1h2op9t"/>
        """.trimIndent()

        assertEquals("Zoe Behavioral Prescoring", LinkParser.extractText(input))
    }

    @Test
    fun parsingTextComplex() {
        val input = """
            <div title="behavior_input_changes_distance" class="css-67ghj5">behavior_input_changes_distance</div><div class="css-13gjugf">1 feature<button class="css-xabruz"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentcolor"><rect width="24" height="24" transform="rotate(-90 12 12)" opacity="0"></rect><path d="M10 19a1 1 0 0 1-.64-.23 1 1 0 0 1-.13-1.41L13.71 12 9.39 6.63a1 1 0 0 1 .15-1.41 1 1 0 0 1 1.46.15l4.83 6a1 1 0 0 1 0 1.27l-5 6A1 1 0 0 1 10 19z"></path></svg></button></div>
        """.trimIndent()

        assertEquals("behavior_input_changes_distance", LinkParser.extractText(input))
    }
}
