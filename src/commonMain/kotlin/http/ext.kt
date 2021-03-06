package http

import io.ktor.http.*
import log

object UrlExt {
    fun getHost(url: String): String {
        val parsedUrl = URLBuilder(url).build()
        return if (parsedUrl.port == parsedUrl.protocol.defaultPort) parsedUrl.host
        else parsedUrl.hostWithPort
    }

    fun removeUrlPath(url: String): String {
        val parsedUrl = URLBuilder(url).build()
        val fragment = "#${parsedUrl.fragment}"
        val path = parsedUrl.fullPath
        return url.removeSuffix(fragment).removeSuffix(path)
    }

    fun isUrl(url: String): Boolean = try {
        URLBuilder(url)
        true
    } catch (e: URLParserException) {
        // FIXME (MH): 4/3/21 this is shit
//        @ThreadLocal
        log.debug { "Not a valid URL: $url" }
        false
    }
}
