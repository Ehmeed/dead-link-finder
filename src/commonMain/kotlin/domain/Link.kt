package domain

sealed class Link {
    abstract val value: String
    abstract val rawValue: String
    abstract val text: String

    class Anchor(override val text: String, override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl.dropLast(1) + rawValue
    }

    data class Absolute(override val text: String, override val rawValue: String) : Link() {
        override val value: String = rawValue
    }

    // FIXME (MH): 1/13/21 this will probs fail when baseUrl contains some params or fragment
    class Relative(override val text: String, override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl + rawValue
    }

    class RootRelative(override val text: String, override val rawValue: String, domainUrl: String) : Link() {
        override val value: String = domainUrl + rawValue.drop(1)
    }

    data class Mailto(override val text: String, override val rawValue: String) : Link() {
        override val value: String = rawValue

        val address = rawValue.removePrefix("mailto:")
    }
}
