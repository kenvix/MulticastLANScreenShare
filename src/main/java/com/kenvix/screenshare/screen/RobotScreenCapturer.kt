//--------------------------------------------------
// Class RobotScreenCapturer
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.util.concurrent.Executors

class RobotScreenCapturer(
    override var callback: ScreenCapturer.Callback? = null,
    override var fps: Int = 5,
    override var monitor: Int = 0
) : ScreenCapturer, AutoCloseable {

    private val robot = Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices[monitor])
    private val toolkit = Toolkit.getDefaultToolkit()

    var maxFragmentPixelNum: Int = 8000

    override var fragmentWidth: Int  = 16
    override var fragmentHeight: Int = 9
    override var screenWidth: Int    = -1
    override var screenHeight: Int   = -1
    var captureDelayMills: Long      = -1

    private var actualFragmentWidth: Int  = -1
    private var actualFragmentHeight: Int = -1

    private var captureJob: CompletableJob? = null
    private var captureWorkScope: CoroutineScope? = null

    @UseExperimental(ObsoleteCoroutinesApi::class)
    fun start(shouldCaptureFullscreenOnce: Boolean = true) {
        captureJob = Job()
        captureWorkScope = CoroutineScope(Dispatchers.Default + captureJob!!)

        updateScreenProfile()
        val tickerChannel = ticker(delayMillis = captureDelayMills, initialDelayMillis = 0)

        if (shouldCaptureFullscreenOnce) {
            captureWorkScope!!.launch {
                for (tick in tickerChannel) {
                    launch(Dispatchers.Default) {
                        callback?.onFragmentCaptured(capture(0, 0, screenWidth, screenHeight), 0, 0)
                    }
                }
            }
        } else {
            captureWorkScope!!.launch {
                for (tick in tickerChannel) {
                    var x = 0
                    var y = 0

                    while (y < screenHeight) {
                        x += actualFragmentWidth
                        y += actualFragmentHeight

                        callback?.onFragmentCaptured(capture(x, y), x, y)
                    }
                }
            }
        }
    }

    override fun updateScreenProfile() {
        captureDelayMills = 1000L / fps

        val screenSize = Toolkit.getDefaultToolkit().screenSize
        screenWidth = screenSize.width
        screenHeight = screenSize.height

        val pixels = fragmentHeight * fragmentWidth
        val multiple = maxFragmentPixelNum / pixels
        actualFragmentHeight = fragmentHeight * multiple
        actualFragmentWidth  = fragmentWidth * multiple
    }

    override fun close() {
        if (captureJob?.isActive == true)
            captureJob?.cancel()
    }

    private suspend fun capture(x: Int, y: Int, width: Int = actualFragmentWidth, height: Int = actualFragmentHeight): BufferedImage
            = withContext(Dispatchers.Default) {
        val rectangle = Rectangle(x, y, width, height)
        robot.createScreenCapture(rectangle)
    }
}