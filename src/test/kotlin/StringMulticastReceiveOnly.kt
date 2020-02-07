import com.kenvix.screenshare.network.MulticastServer
import kotlinx.coroutines.*
import java.net.InetAddress

fun main() {
    val server = MulticastServer()
    server.onReceive = {
        println("> Receive: ${String(it.data, 0, it.length)}")
    }
    server.listen()

    runBlocking {
        while (true) {
            delay(999000)
        }
    }
}