import com.kenvix.screenshare.network.MulticastServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
    val server = MulticastServer()
    server.onReceive = {
        print(it.length)
    }

    server.listen()

    runBlocking {
        while (true) {
            delay(999000)
        }
    }
}