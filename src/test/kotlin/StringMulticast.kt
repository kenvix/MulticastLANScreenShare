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
        var i = 0
        while (true) {
            i++;
            val b = "!$i"
            server.send(b.toByteArray())
            println("< Send: $b")
            delay(2000)
        }
    }
}