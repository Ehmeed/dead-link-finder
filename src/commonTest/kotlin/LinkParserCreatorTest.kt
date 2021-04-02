import domain.Link
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LinkParserCreatorTest {

    @Test
    fun parse_broken_absolute_link_without_http() {
        val parsed = LinkParser.Link(
            value = "zoe.lundegaard.ai/docs/zoe-api/swagger",
            sourcePageUrl = "https://zoe.lundegaard.ai/docs/s-analytics/reference/s-analytics/",
            domainUrl = "https://zoe.lundegaard.ai/"
        )
        val expected = Link.Relative("zoe.lundegaard.ai/docs/zoe-api/swagger", "https://zoe.lundegaard.ai/docs/s-analytics/reference/s-analytics/")
        assertEquals(expected.rawValue, parsed.rawValue)
        assertEquals(expected.value, parsed.value)
    }
}
