import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

expect val zahradnictviKarlovHtml: String

class LinkParserTest {

    @Test
    fun emptyPageReturnsNoLinks() {
        assertEquals(emptyList(), LinkParser.getLinks(""))
    }

    @Test
    fun zahradnictviKarlovReturnsExpectedLinks() {
        assertTrue { zahradnictviKarlovHtml.isNotBlank() }
        assertEquals(
            listOf(
            "http://html5shim.googlecode.com/svn/trunk/html5.js",
            "http://explorercanvas.googlecode.com/svn/trunk/excanvas.js",
            "https://www.googletagmanager.com/gtag/js?id=UA-162341339-1",
            "https://cdn.jsdelivr.net/npm/cookie-bar/cookiebar-latest.min.js?forceLang=cs&tracking=1&thirdparty=1&noGeoIp=1&showNoConsent=1",
            "http://zahradnictvi-karlov.rajce.idnes.cz/",
            "https://mapy.cz/s/toCP",
            "http://webThemez.com",
            "http://zahradnictvikarlov.cz/#work", // TODO (MH): 12/28/20 should contain all "navigation elements" https://stackoverflow.com/questions/20841363/regex-finding-all-href-in-a-tags
        ).sorted()
            , LinkParser.getLinks(zahradnictviKarlovHtml).sorted())
    }
}
