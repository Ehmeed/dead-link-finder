import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val args = arrayOf(
        "-H",
        "-d",
        "3",
        "https://zoe.lundegaard.ai/docs",
    )
    Main.main(args)
}
