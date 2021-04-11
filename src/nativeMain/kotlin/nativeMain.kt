import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    Platform.isMemoryLeakCheckerActive = false
    Main.main(args)
}
