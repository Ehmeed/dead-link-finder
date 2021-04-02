fun main() {
    val args = arrayOf(
//        "--no-summary",
        "-d",
        "1",
        "--cross-domain",
        "dont-recurse",
        "-H",

//        "https://zoe.lundegaard.ai/docs/",
        "https://zoe.lundegaard.ai/docs/s-analytics/reference/s-analytics#send.event",
    )
    Main.main(args)
}
