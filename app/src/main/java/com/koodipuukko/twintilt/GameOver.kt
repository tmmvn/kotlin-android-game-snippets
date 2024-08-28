package com.koodipuukko.twintilt

import android.content.Context
import android.util.Log

class GameOver
	(
	private val mainThread: TwinTiltCode,
	private var gameLoop: GameLoop?,
	private val context: Context,
	private val renderer: GLRenderer,
	private val MAX_WIDTH: Int,
	private val MAX_HEIGHT: Int
) : Thread() {
	override fun run() {
		try {
			sleep(5000)
		} catch(e: InterruptedException) {
			if(DEBUG) {
				Log.e(TAG, "GameOver: Error sleeping: $e")
			}
		}
		try {
			gameLoop!!.interrupt()
		} catch(e: SecurityException) {
			if(DEBUG) {
				Log.e(TAG, "GameOver: Error stopping gameloop: $e")
			}
		}
		gameLoop = null
		gameLoop =
			GameLoop(mainThread, context, renderer, MAX_WIDTH, MAX_HEIGHT)
		mainThread.ChangeGameLoop(gameLoop)
		gameLoop!!.setMenuRunning(true)
		gameLoop!!.setRunning(false)
		gameLoop!!.run()
		try {
			this.interrupt()
		} catch(e: SecurityException) {
			if(DEBUG) {
				Log.e(TAG, "GameOver: Error stopping me: $e")
			}
		}
	}

	companion object {
		private const val TAG = "Twintilt" // Game tag in logs
		private const val DEBUG = false // Sets debug mode on or off
	}
}
