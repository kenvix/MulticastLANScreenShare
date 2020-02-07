//--------------------------------------------------
// Class RobotScreenCapturer
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.screenshare.screen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

class RobotScreenCapturer(
    override var fps: Int = 30,
    override var onFragmentCaptured: ((output: BufferedImage) -> Unit)? = null
) : ScreenCapturer, AutoCloseable {

    private val robot = Robot()
    private val toolkit = Toolkit.getDefaultToolkit()

    var maxFragmentPixelNum: Int = 600

    override var fragmentWidth: Int  = 16
    override var fragmentHeight: Int = 9
    override var screenWidth: Int    = -1
    override var screenHeight: Int   = -1
    var captureDelayMills: Long      = -1

    private var actualFragmentWidth: Int  = -1
    private var actualFragmentHeight: Int = -1

    private var captureJob: CompletableJob? = null
    private var captureWorkScope: CoroutineScope? = null

    @Suppress("DeferredResultUnused")
    @UseExperimental(ObsoleteCoroutinesApi::class)
    fun start() {
        captureJob = Job()
        captureWorkScope = CoroutineScope(Dispatchers.Default + captureJob!!)

        updateScreenProfile()
        val tickerChannel = ticker(delayMillis = captureDelayMills, initialDelayMillis = 0)

        captureWorkScope!!.launch {
            var x = 0
            var y = 0

            for (tick in tickerChannel) {
                async {
                    x = (x + actualFragmentWidth) % screenWidth
                    y = (y + actualFragmentHeight) % screenHeight

                    onFragmentCaptured?.invoke(capture(x, y))
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

    private suspend fun capture(x: Int, y: Int): BufferedImage = withContext(Dispatchers.Default) {
        val rectangle = Rectangle(x, y, fragmentWidth, fragmentHeight)
        robot.createScreenCapture(rectangle)
    }
}