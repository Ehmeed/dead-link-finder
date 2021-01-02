fun main() {
    val args = arrayOf(
        "-v",
//        "--no-summary",
        "-d",
        "3",
        "--cross-domain",
        "dont-recurse",
        "http://zahradnictvikarlov.cz",
    )
    Main.main(args)
}
