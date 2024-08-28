package com.koodipuukko.jeweltilt

import android.util.Log

class PlayerBall
	(size: Float, scale: Float) : Quad(size, scale) {
	private var maxWidth = 0
	private var maxHeight = 0
	private var minWidth = 0
	private var minHeight = 0 // movement bounds
	private val coords = FloatArray(3) // current position
	private var type = 1
	fun changeType(newType: Int) {
		type = newType
	}

	fun getType(): Int {
		return type
	}
	// Function to set position
	fun setCoordinates(x: Float, y: Float, z: Float) {
		coords[0] = x
		coords[1] = y
		coords[2] = z
		if(DEBUG) {
			Log.d(TAG, "Player ball coordinates set to: $x, $y, $z")
		}
	}

	fun setMovemenetBounds(maxX: Int, maxY: Int, minX: Int, minY: Int) {
		maxWidth = maxX // Set bounds for movement
		maxHeight = maxY
		minWidth = minX
		minHeight = minY
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

	fun move(
		x: Float,
		y: Float,
		z: Float
	) {		// Check if x is to be changed
		if(x != 0f) {			// Move and if out of bounds set to limits
			coords[0] += x
			if(coords[0] < minWidth) {
				coords[0] = minWidth.toFloat()
			}
			if(coords[0] > maxWidth) {
				coords[0] = maxWidth.toFloat()
			}
		}				// Same as x
		if(y != 0f) {
			coords[1] += y
			if(coords[1] < minHeight) {
				coords[1] = minHeight.toFloat()
			}
			if(coords[1] > maxHeight) {
				coords[1] = maxHeight.toFloat()
			}
		}				// Same as y
		if(z != 0f) {
			coords[2] += z
		}
	}

	companion object {
		private const val TAG = "Jewel Tilt" // Game tag in logs
		private const val DEBUG = false
	}
}
