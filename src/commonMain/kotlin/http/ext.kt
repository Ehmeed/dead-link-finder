package http

import io.ktor.http.*

object Ext {
    fun getHost(url: String): String {
        val parsedUrl = URLBuilder(url).build()
        return if (parsedUrl.port == parsedUrl.protocol.defaultPort) parsedUrl.host
        else parsedUrl.hostWithPort
    }

    fun removeUrlPath(url: String): String {
        val parsedUrl = URLBuilder(url).build()
        return url.removeSuffix(parsedUrl.fullPath)
    }
}
