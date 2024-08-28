package com.koodipuukko.dragonsquad

import android.util.Log
import android.view.Display
import android.view.MotionEvent

class Portal
	(rendererToUse: GLRenderer, displayToUse: Display) : Thread() {
	var renderer: GLRenderer = rendererToUse
	var display: Display = displayToUse
	private var scrolling = false
	private var scrollCounter = 0
	private var prepareScrolling = false
	private val running = true
	// variables to be used to get constant FPS
	private var beginTime: Long = 0
	private var timeDiff: Long = 0
	private var sleepTime = 0
	private var test: TerrainMap? = null
	private var friendly: Unit? = null
	private var ui: UI = UI()
	private var testi: Text? = null

	init {
		LoadData()
	}

	private fun LoadData() {
		test = TerrainMap()
		test?.loadMap(0)
		test?.prepareRenderData()
		test?.updateMap()
		testi = Text(50, 50, 1)
		testi?.setText(("X: " + test?.getViewFocusX()).toString() + " Y: " +
		test?.getViewFocusY())
		testi?.render()
		ui.addTextElement(testi)
		friendly = Unit(0)
		friendly?.getBuffer()?.setTextureId(2)
		friendly?.render()
		friendly?.setPosition(12, 12)
		friendly?.setView(test!!.getViewWidth(), test!!.getViewHeight())
		friendly?.setTargetWaypoint(13, 13)
		friendly?.updateLocation()
		renderer.setUIBuffer(ui)
		renderer.setTerrainFloorBuffer(test?.getFloorBuffer())
		renderer.setTerrainWallBuffer(test?.getWallBuffer())
		renderer.setFriendlyUnitBuffer(friendly?.getBuffer())
	}

	@Override override fun run() {
		while(running) {			// store current time
			beginTime = System.currentTimeMillis()
			friendly?.move()						// calculate if sleeping
			// is needed to have constant FPS
			timeDiff = System.currentTimeMillis() - beginTime
			sleepTime =
				(FRAME_PERIOD - timeDiff).toInt()						// need to sleep
			if(sleepTime > 0) {
				try {
					Thread.sleep(sleepTime.toLong())
				} catch(e: InterruptedException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error sleeping: $e")
					}
				}
			}
		}
	}

	fun passTouchEvent(event: MotionEvent) {
		val eventAction: Int = event.action
		when (eventAction) {
			MotionEvent.ACTION_DOWN -> prepareScrolling = true
			MotionEvent.ACTION_MOVE -> {
				if(scrolling) {
					test?.panMap(
						(display.getWidth() / 2 - event.getX()) / -30f,
						(display.getHeight() / 2 - event.getY()) / -30f
					)
					friendly?.setViewFocus(
						test!!.getViewFocusX(),
						test!!.getViewFocusY(),
						test!!.getViewXOffset(),
						test!!.getViewYOffset()
					)
					friendly?.updateLocation()
				} else {
					if(prepareScrolling) {
						scrollCounter++
					}
				}
				if(scrollCounter >= 9) {
					scrolling = true
				}
			}

			MotionEvent.ACTION_UP -> {
				if(!scrolling) {
					friendly?.setTargetWaypoint(
						getTouchedTile(event.x, event.y)[0],
						getTouchedTile(event.x, event.y)[1]
					)
				}
				scrolling = false
				prepareScrolling = false
				scrollCounter = 0
			}

			else -> {}
		}
		if(DEBUG) {
			Log.d(
				TAG,
				("Got touch event: " + event.x).toString() + ", " + event.y
			)
		}
	}

	private fun getTouchedTile(touchX: Float, touchY: Float): IntArray {
		val touchCoords = IntArray(2)
		touchCoords[0] = (touchX / 132 + touchY / 66).toInt() / 2
		touchCoords[1] = (touchX / 132 - touchY / 66).toInt() / 2
		touchCoords[0] += test!!.getViewFocusX()
		touchCoords[1] += test!!.getViewFocusY()
		if(DEBUG) {
			Log.d(
				TAG,
				"Portal: touched tile at x: " + touchCoords[0] + " y: " + touchCoords[1]
			)
		}
		return touchCoords
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG = true // Debug flag
		private const val MAX_FPS = 50 // Max FPS
		private const val FRAME_PERIOD =
			1000 / MAX_FPS // How time each frame takes
	}
}
