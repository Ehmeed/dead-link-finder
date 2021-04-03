import kotlinx.coroutines.runBlocking

fun main() {
    val args = arrayOf(
        "-v",
//        "--no-summary",
        "-d",
        "1",
        "--cross-domain",
        "dont-recurse",
        "-H",
        
//        "http://localhost:5000/same-domain",
        "https://zoe.lundegaard.ai/docs/",
//        "https://zoe.lundegaard.ai/docs/s-analytics/reference/s-analytics#send.event",
    )
    runBlocking {
        Main.main(args)
    }
}
