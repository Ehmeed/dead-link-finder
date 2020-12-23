fun main() {
    val args = arrayOf(
        "--verbose",
        "-d",
        "2",
        "--cross-domain",
        "dont-recurse",
        "-H",
        "https://zoe.lundegaard.ai/docs/",
    )
    Main.main(args)
}
