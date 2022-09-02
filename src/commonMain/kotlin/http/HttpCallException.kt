package http

class HttpCallException(val status: Int) : RuntimeException()
class MovedException(val location: String) : RuntimeException()
