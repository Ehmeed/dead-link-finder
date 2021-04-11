package http

import domain.LinkContent
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import log

class Client(private val allowedStatusCodes: List<Int>, private val requestHeaders: List<Pair<String, String>>) {

    private val httpClient = HttpClient() {
        expectSuccess = false
        followRedirects = true
        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status.value
                if (statusCode !in allowedStatusCodes) throw HttpCallException(statusCode)
            }
        }
    }

    suspend fun use(block: suspend (Client) -> Unit) {
        httpClient.use {
            block(this)
        }
    }

    suspend fun getContent(url: String): LinkContent = try {
        httpClient.get<String>(url) {
            requestHeaders.forEach { header(it.first, it.second) }
        }.let { LinkContent.Success(it) }
    } catch (ex: MalformedInputException) {
        // FIXME (MH): 4/3/21 this is shit
//        @ThreadLocal
        log.debug { "Cannot read page as string, but status is accepted" }
        LinkContent.UnreadableSuccess
    } catch (ex: HttpCallException) {
        // FIXME (MH): 4/3/21 this is shit
//        @ThreadLocal
        log.debug { "Invalid status code ${ex.status} for: $url" }
        LinkContent.InvalidStatusCode(ex.status)
    } catch (ex: IOException) {
        // FIXME (MH): 4/3/21 this is shit
//        @ThreadLocal
        log.debug { "Failed to get: $url $ex" }
        LinkContent.Unreachable(ex.message ?: "unknown reason")
    }
}
