import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val args = arrayOf(
        "--verbose",
        "https://en.wikipedia.org/wiki/Hyperlink",
    )
    Main.main(args)
}
