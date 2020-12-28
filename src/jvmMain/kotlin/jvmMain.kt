fun main() {
    val args = arrayOf(
        "--verbose",
        "-d",
        "3",
        "--cross-domain",
        "dont-recurse",
        "http://zahradnictvikarlov.cz",
    )
    Main.main(args)
}
