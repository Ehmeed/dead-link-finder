import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    Platform.isMemoryLeakCheckerActive = true
    Main.main(args)
}
