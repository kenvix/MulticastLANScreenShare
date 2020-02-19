@file:JvmName("Main")

package com.kenvix.screenshare

import com.kenvix.screenshare.network.MulticastServer
import com.kenvix.screenshare.screen.DefaultFragmentImageProcessor
import com.kenvix.screenshare.screen.DefaultReceivedImageProcessor
import com.kenvix.screenshare.screen.RobotScreenCapturer
import com.kenvix.screenshare.ui.BaseUI
import com.kenvix.screenshare.ui.GuiDispatcher
import com.kenvix.screenshare.ui.SwingClientUI
import org.apache.commons.cli.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.system.exitProcess


object Main {
    const val header = "Kenvix LAN UDP Multicast ScreenShare"

    var options = Options()
        private set
    var commands: CommandLine? = null
        private set

    var isLoopbackEnabled = true
        private set

    var networkInterface: NetworkInterface? = null
        private set

    val clientUI: BaseUI = SwingClientUI.getInstance()

    var windowWidth = getOptionValue('w', 1366)
    var windowHeight = getOptionValue('e', 768)

    @JvmStatic
    fun main(args: Array<String>?) {
        commands = getCmd(args)

        if (commands?.hasOption('h') == true) {
            printHelp()
            exitProcess(0)
        }


        println(header)
        val host = getOptionValue('a', "230.114.5.14")
        val port = getOptionValue('p', 1919)
        isLoopbackEnabled = commands?.hasOption('n') != true

        loadNativeLib()
        if (commands?.hasOption('i') == true)
            networkInterface = NetworkInterface.getByName(commands!!.getOptionValue('i'))

        if (commands?.hasOption('s') == true) {
            runAsServer(host, port)

            if (isLoopbackEnabled)
                showWindow("Server Loopback")
        } else {
            println("Client mode. Target multicast Address: $host    Port: $port")
            runAsClient(host, port)
        }

        println()

        readConsoleCommand()
    }

    private fun runAsServer(host: String, port: Int) {
        val server = MulticastServer(multicastAddress = InetAddress.getByName(host), multicastPort = port, networkInterface = networkInterface)

        server.listen()

        val processor = DefaultFragmentImageProcessor(server, packetSize = getOptionValue('t', 1000))
        val capturer = RobotScreenCapturer(processor, fps = getOptionValue('f', 6), monitor = getOptionValue('m', 0))
        capturer.start()

        println("Server started at Multicast Address: $host    Port: $port")
    }

    private fun runAsClient(host: String, port: Int) {
        showWindow()

        val server = MulticastServer(multicastAddress = InetAddress.getByName(host), multicastPort = port, networkInterface = networkInterface)
        server.listen()

        val receiver = DefaultReceivedImageProcessor(server, packetSize = getOptionValue('t', 1000))
        receiver.start(30000)
    }

    private fun showWindow(title: String = "Client") {
        GuiDispatcher.show(windowWidth, windowHeight)
        GuiDispatcher.title = title
    }

    private fun readConsoleCommand() {
        println("Ready to receive command. Please input 'help' for help")
        val scanner = Scanner(System.`in`)

        while (true) {
            print("> ")
            val commandString = scanner.nextLine()
            val command = commandString.split(' ')

            if (command.isNotEmpty()) {
                when (command[0]) {
                    "help" -> {
                        println("You can enter long command below to change options.")
                        printHelp()
                    }
                    "exit" -> exitProcess(0)
                    "show" -> showWindow()
                }
            }
        }
    }

    @Throws(ParseException::class)
    private fun getCmd(args: Array<String>?): CommandLine? {
        options.addOption("p", "port", true, "Port of screen share render. Default 1919")
        options.addOption("a", "address", true, "Multicast address of screen share render. Default 230.114.5.14")

        options.addOption("v", "verbose", false, "Verbose logging mode.")
        options.addOption("s", "server",false, "Render server mode.")

        options.addOption("f", "fps",true, "[Server] FPS. Default 6")
        options.addOption("m", "monitor",true, "[Server] Monitor ID. Default 0")
        options.addOption("t", "packet-size",true, "[Server] UDP Packet size. Default 1000")
        options.addOption("n", "no-loopback",false, "[Server] Do NOT show local playback")
        options.addOption("q", "quality",true, "[Server] Image quality (1~100). Default 50")

        options.addOption("w", "width",true, "Playback window width")
        options.addOption("e", "height",true, "Playback window height")
        options.addOption("i", "network",true, "Which network interface to use")

        options.addOption("h", "help", false, "Print help messages")

        val parser = DefaultParser()
        return parser.parse(options, args)
    }

    private fun printHelp() {
        val formatter = HelpFormatter()
        formatter.printHelp("java -jar ScreenShare.jar", header, options, "", true)

        println()
        println("Online Network interfaces on this machine: ")
        println(NetworkInterface.getNetworkInterfaces().toList().filter {
            it.inetAddresses.hasMoreElements() && !it.isLoopback && it.isUp && !it.isVirtual
        }.joinToString(transform = {
            "${it.name}: ${it.displayName} (${it.inetAddresses.toList().joinToString { it.hostAddress }})"
        }, separator = "\n"))
    }

    inline fun <reified T> getOptionValue(opt: Char, defaultValue: T): T {
        if (commands == null)
            return defaultValue

        return when(T::class) {
            Int::class -> commands!!.getOptionValue(opt, defaultValue.toString()).toInt() as T
            Float::class -> commands!!.getOptionValue(opt, defaultValue.toString()).toFloat() as T
            Double::class -> commands!!.getOptionValue(opt, defaultValue.toString()).toDouble() as T
            Long::class -> commands!!.getOptionValue(opt, defaultValue.toString()).toLong() as T
            String::class -> commands!!.getOptionValue(opt, defaultValue.toString()) as T
            else -> throw IllegalArgumentException("Not supported type")
        }
    }

    private fun loadNativeLib() {
        var arch = System.getProperties().getProperty("os.arch")
        if (arch == "x86_64") arch = "x86_64"

        val paths: String? = System.getProperties().getProperty("java.library.path")
        val dirPath = File("Library/$arch").absolutePath
        addNativeLibDir(dirPath)
        System.loadLibrary("turbojpeg")
    }

    @Throws(IOException::class)
    fun addNativeLibDir(s: String) {
        try { // This enables the java.library.path to be modified at runtime
                // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
                //
            val field: Field = ClassLoader::class.java.getDeclaredField("usr_paths")
            field.isAccessible = true
            val paths = field.get(null) as Array<*>
            for (i in paths.indices) {
                if (s == paths[i]) {
                    return
                }
            }
            val tmp = arrayOfNulls<String>(paths.size + 1)
            System.arraycopy(paths, 0, tmp, 0, paths.size)
            tmp[paths.size] = s
            field.set(null, tmp)
            System.setProperty(
                "java.library.path",
                System.getProperty("java.library.path") + File.pathSeparator + s
            )
        } catch (e: IllegalAccessException) {
            throw IOException("Failed to get permissions to set library path")
        } catch (e: NoSuchFieldException) {
            throw IOException("Failed to get field handle to set library path")
        }
    }
}