package domain

sealed class LinkContent {
    abstract val statusString: String

    data class Success(val content: String) : LinkContent() {
        override val statusString: String = "OK"
    }
    object UnreadableSuccess: LinkContent() {
        override val statusString: String = "OK"
    }
    data class InvalidStatusCode(val statusCode: Int): LinkContent() {
        override val statusString: String = "FAIL (status: $statusCode)"
    }
    data class Unreachable(val message: String): LinkContent() {
        override val statusString: String = "FAIL ($message)"
    }
}

