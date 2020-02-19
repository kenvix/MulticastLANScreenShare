@file:JvmName("Main")

package com.kenvix.screenshare

import com.kenvix.screenshare.network.MulticastServer
import com.kenvix.screenshare.screen.DefaultFragmentImageProcessor
import com.kenvix.screenshare.screen.DefaultReceivedImageProcessor
import com.kenvix.screenshare.screen.RobotScreenCapturer
import com.kenvix.screenshare.ui.GuiDispatcher
import org.apache.commons.cli.*
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter
import kotlin.system.exitProcess

object Main {
    const val header = "Kenvix LAN UDP Multicast ScreenShare"

    var options = Options()
        private set
    var commands: CommandLine? = null
        private set

    var isLoopbackEnabled = true
        private set

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

        if (commands?.hasOption('s') == true) {
            runAsServer(host, port)

            if (isLoopbackEnabled)
                showWindow()
        } else {
            println("Client mode. Target multicast Address: $host    Port: $port")
            runAsClient(host, port)
        }

        println()

        readConsoleCommand()
    }

    private fun runAsServer(host: String, port: Int) {
        val server = MulticastServer(multicastAddress = InetAddress.getByName(host), multicastPort = port)

        server.listen()

        val processor = DefaultFragmentImageProcessor(server, packetSize = getOptionValue('t', 1000))
        val capturer = RobotScreenCapturer(processor, fps = getOptionValue('f', 6), monitor = getOptionValue('m', 0))
        capturer.start()

        println("Server started at Multicast Address: $host    Port: $port")
    }

    private fun runAsClient(host: String, port: Int) {
        showWindow()

        val server = MulticastServer(multicastAddress = InetAddress.getByName(host), multicastPort = port)
        server.listen()

        val receiver = DefaultReceivedImageProcessor(server, packetSize = getOptionValue('t', 1000))
        receiver.start(1000)
    }

    private fun showWindow() {
        GuiDispatcher.show(windowWidth, windowHeight)
        GuiDispatcher.title = "Client"
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
        options.addOption("q", "quality",true, "[Server] Image quality. Default 0.5")

        options.addOption("w", "width",true, "Playback window width")
        options.addOption("e", "height",true, "Playback window height")
        options.addOption("i", "network",true, "Which network card to use")

        options.addOption("h", "help", false, "Print help messages")

        val parser = DefaultParser()
        return parser.parse(options, args)
    }

    private fun printHelp() {
        val formatter = HelpFormatter()
        formatter.printHelp("java -jar ScreenShare.jar", header, options, "", true)
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
}