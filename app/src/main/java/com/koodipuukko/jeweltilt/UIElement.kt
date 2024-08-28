package com.koodipuukko.jeweltilt

import android.util.Log

class UIElement
	(size: Float, scale: Float) : Quad(size, scale) {
	private val coords = FloatArray(3) // current position
	// Function to set position
	fun setCoordinates(x: Float, y: Float, z: Float) {
		coords[0] = x
		coords[1] = y
		coords[2] = z
		if(DEBUG) {
			Log.d(TAG, "UI element coordinates set to: $x, $y, $z")
		}
	}
	// Functions to return coordinates
	fun getXCoordinate(): Float {
		return coords[0]
	}

	fun getYCoordinate(): Float {
		return coords[1]
	}

	fun getZCoordinate(): Float {
		return coords[2]
	}

	companion object {
		private const val TAG = "Jewel Tilt" // Game tag in logs
		private const val DEBUG = false
	}
}
