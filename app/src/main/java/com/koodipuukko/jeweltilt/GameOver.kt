package com.koodipuukko.jeweltilt

import android.content.Context
import android.util.Log

class GameOver
	(
	main: JewelTilt,
	gameThread: GameLoop?,
	passedContext: Context,
	rend: GLRenderer,
	width: Int,
	height: Int
) : Thread() {
	private var gameLoop: GameLoop?
	private val mainThread: JewelTilt
	private val context: Context
	private val renderer: GLRenderer
	private val MAX_WIDTH: Int
	private val MAX_HEIGHT: Int

	init {
		gameLoop = gameThread
		context = passedContext
		renderer = rend
		MAX_WIDTH = width
		MAX_HEIGHT = height
		mainThread = main
	}

	@Override override fun run() {
		try {
			Thread.sleep(5000)
		} catch(e: InterruptedException) {
			if(DEBUG) {
				Log.e(TAG, "GameOver: Error sleeping: $e")
			}
		}
		try {
			gameLoop?.interrupt()
		} catch(e: SecurityException) {
			if(DEBUG) {
				Log.e(TAG, "GameOver: Error stopping gameloop: $e")
			}
		}
		gameLoop = null
		gameLoop =
			GameLoop(mainThread, context, renderer, MAX_WIDTH, MAX_HEIGHT)
		mainThread.changeGameLoop(gameLoop)
		gameLoop?.setMenuRunning(true)
		gameLoop?.setRunning(false)
		gameLoop?.run()
		try {
			this.interrupt()
		} catch(e: SecurityException) {
			if(DEBUG) {
				Log.e(TAG, "GameOver: Error stopping me: $e")
			}
		}
	}

	companion object {
		private const val TAG = "Jewel Tilt" // Game tag in logs
		private const val DEBUG = false // Sets debug mode on or off
	}
}
