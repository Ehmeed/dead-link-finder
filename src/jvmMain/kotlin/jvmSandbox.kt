import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val args = arrayOf(
        "--verbose",
        "--show-text",
        "--cross-domain", "ignore",
        "https://zoe.lundegaard.ai",
    )
    Main.main(args)
}
