import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val args = arrayOf(
        "http://localhost:5000/same-domain",
    )
    Main.main(args)
}
