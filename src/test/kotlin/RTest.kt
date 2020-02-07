import kotlinx.coroutines.*

val serverJob = Job()
val uiScope = CoroutineScope(Dispatchers.Main + serverJob)
val ioScope = CoroutineScope(Dispatchers.IO + serverJob)

fun main() {
    ioScope.launch {

    }
}

fun cancel() {
    ioScope.cancel()
}

suspend fun updateUI() {

}