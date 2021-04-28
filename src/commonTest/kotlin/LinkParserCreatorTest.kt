import domain.Link
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkParserCreatorTest {

    @Test
    fun parse_broken_absolute_link_without_http() {
        val parsed = LinkParser.Link(
            text = "abc",
            value = "zoe.lundegaard.ai/docs/zoe-api/swagger",
            sourcePageUrl = "https://zoe.lundegaard.ai/docs/s-analytics/reference/s-analytics/",
            domainUrl = "https://zoe.lundegaard.ai/"
        )
        val expected = Link.Relative("abc", "zoe.lundegaard.ai/docs/zoe-api/swagger", "https://zoe.lundegaard.ai/docs/s-analytics/reference/s-analytics/")
        assertEquals(expected.rawValue, parsed.rawValue)
        assertEquals(expected.value, parsed.value)
    }
}
