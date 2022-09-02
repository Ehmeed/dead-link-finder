package domain

sealed class Link {
    abstract val value: String
    abstract val rawValue: String
    abstract val text: String?
    abstract val isVisitable: Boolean
    class Anchor(override val text: String?, override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl.dropLast(1) + rawValue
        override val isVisitable: Boolean = true
    }

    data class Absolute(override val text: String?, override val rawValue: String) : Link() {
        override val value: String = rawValue
        override val isVisitable: Boolean = true
    }

    // FIXME (MH): 1/13/21 this will probs fail when baseUrl contains some params or fragment
    class Relative(override val text: String?, override val rawValue: String, baseUrl: String) : Link() {
        override val value: String = baseUrl + rawValue
        override val isVisitable: Boolean = true
    }

    class RootRelative(override val text: String?, override val rawValue: String, domainUrl: String) : Link() {
        override val value: String = domainUrl + rawValue.drop(1)
        override val isVisitable: Boolean = true
    }

    data class Mailto(override val text: String?, override val rawValue: String) : Link() {
        companion object {
            const val PREFIX = "mailto:"
        }
        override val value: String = rawValue
        override val isVisitable: Boolean = false
    }

    data class Tel(override val text: String?, override val rawValue: String) : Link() {
        companion object {
            const val PREFIX = "tel:"
        }
        override val value: String = rawValue
        override val isVisitable: Boolean = false
    }
}
