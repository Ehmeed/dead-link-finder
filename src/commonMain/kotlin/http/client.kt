package http

import domain.LinkContent
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import log

class Client(
    private val allowedStatusCodes: List<Int>,
    private val requestHeaders: List<Pair<String, String>>,
    timeout: Int,
) {

    private val httpClient = HttpClient() {
        expectSuccess = false
        followRedirects = true
        install(HttpTimeout) {
            requestTimeoutMillis = timeout.toLong()
        }
        HttpResponseValidator {
            validateResponse { response ->
                when (val statusCode = response.status.value) {
                    HttpStatusCode.MovedPermanently.value -> {
                        throw MovedException(response.headers[HttpHeaders.Location] ?: throw HttpCallException(statusCode))
                    }
                    !in allowedStatusCodes -> throw HttpCallException(statusCode)
                }
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
        log.debug { "Cannot read page as string, but status is accepted" }
        LinkContent.UnreadableSuccess
    } catch (ex: HttpCallException) {
        log.debug { "Invalid status code ${ex.status} for: $url" }
        LinkContent.InvalidStatusCode(ex.status)
    } catch (ex: MovedException) {
        log.debug { "Status Moved Permanently, trying provided location" }
        getContent(ex.location)      // fixme can get stuck in infinite loop
    } catch (ex: IOException) {
        log.debug { "Failed to get: $url $ex" }
        LinkContent.Unreachable(ex.message ?: "unknown reason")
    } catch (ex: HttpRequestTimeoutException) {
        log.debug { "Timeout getting $url" }
        LinkContent.Timeout
    }
}
