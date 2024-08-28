package com.koodipuukko.twintilt

import android.util.Log

class Sparkle
	(size: Float, private val type: Int, scale: Float) : Quad(size, scale) {
	private var lifeTime = 0
	private var larger = true
	private var scale = 1f
	private val coords = FloatArray(3) // current position
	fun getScale(): Float {
		return scale
	}
	// Function to set position
	fun setCoordinates(x: Float, y: Float, z: Float) {
		coords[0] = x
		coords[1] = y
		coords[2] = z
		if(DEBUG) {
			Log.d(TAG, "Sparkle coordinates set to: $x, $y, $z")
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

	fun animate(): Boolean {
		if(lifeTime <= 25) {
			lifeTime++
			if(type == 1) {
				this.rotate(10f)
				if(scale < 1.8f && larger) {
					this.scale += 0.1f
				} else {
					larger = false
				}
				if(!larger) {
					this.scale -= 0.1f
				}
			}
			if(type == 2) {
				this.rotate(-10f)
				if(scale < 1.8f && larger) {
					this.scale += 0.1f
				} else {
					larger = false
				}
				if(!larger) {
					this.scale -= 0.1f
				}
			}
			if(type == 3) {
				this.rotate(-10f)
				this.scale -= 0.1f
			}
			if(type == 4) {
				this.rotate(10f)
				this.scale -= 0.1f
			}
			return true
		} else {
			lifeTime = 0
			return false
		}
	}

	companion object {
		private const val TAG = "Twintilt" // Game tag in logs
		private const val DEBUG = false
	}
}
