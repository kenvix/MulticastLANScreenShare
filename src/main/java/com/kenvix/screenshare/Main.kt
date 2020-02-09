@file:JvmName("Main")

package com.kenvix.screenshare

import com.kenvix.screenshare.network.MulticastServer
import com.kenvix.screenshare.screen.DefaultFragmentImageProcessor
import com.kenvix.screenshare.screen.RobotScreenCapturer
import kotlinx.coroutines.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = MulticastServer()
        server.onReceive = {
            val data = it.data

        }
        server.listen()

        val processor = DefaultFragmentImageProcessor(server)
        val capturer = RobotScreenCapturer(processor)
        capturer.start()
        Thread.sleep(999999)
    }
}