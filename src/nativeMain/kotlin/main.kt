import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    Platform.isMemoryLeakCheckerActive = false
    println("Hello, Kotlin/Native!")

    HttpClient().use { client ->
        val content: String = client.get("https://en.wikipedia.org/wiki/Main_Page")
        println(content)
    }
}
