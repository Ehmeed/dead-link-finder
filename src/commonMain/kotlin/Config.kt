data class Config(
    val allowedStatusCodes: List<Int> = (200..300).toList(),
    val url: String,
    val depth: Int,
    val parseText: Boolean,
    val noSummary: Boolean,
    val requestHeaders: List<Pair<String, String>>,
    val crossDomainBehavior: Main.CrossDomainBehavior,
    val logLevel: String,
    val urlDomain: String,
)
