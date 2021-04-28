package domain

sealed class LinkContent {
    abstract val statusString: String
    abstract val isSuccess: Boolean

    data class Success(val content: String) : LinkContent() {
        override val statusString: String = "OK"
        override val isSuccess: Boolean = true
    }
    object UnreadableSuccess : LinkContent() {
        override val statusString: String = "OK"
        override val isSuccess: Boolean = true
    }
    data class InvalidStatusCode(val statusCode: Int) : LinkContent() {
        override val statusString: String = "FAIL (status: $statusCode)"
        override val isSuccess: Boolean = false
    }
    data class Unreachable(val message: String) : LinkContent() {
        override val statusString: String = "FAIL ($message)"
        override val isSuccess: Boolean = false
    }

    object Timeout : LinkContent() {
        override val statusString = "TIMEOUT"
        override val isSuccess = false
    }
}
