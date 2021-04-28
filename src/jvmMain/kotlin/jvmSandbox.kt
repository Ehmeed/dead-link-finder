import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val args = arrayOf(
        "--verbose",
        "--timeout", "100",
        "--cross-domain", "ignore",
        "http://localhost:8084/docs",
    )
    Main.main(args)
}
