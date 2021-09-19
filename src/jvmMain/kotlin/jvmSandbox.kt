import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val args = arrayOf(
        "--verbose",
//        "--cross-domain", "ignore",
        "--timeout", "2000",
        "-d", "1",
        "https://solitea.com/sitemap_cs-cz.xml",
    )
    Main.main(args)
}
