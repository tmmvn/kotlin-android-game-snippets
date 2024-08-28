package com.koodipuukko.dragonsquad

import android.util.Log

class Unit
	(private val id: Int) : Quad(48, 48) {
	private val buffer: Buffers = Buffers()
	private val responseLevel = 0
	private val currentCommand = 0
	private var targetX = 0
	private var targetY = 0
	private var wayPointX = 0
	private var wayPointY = 0
	private var focusX = 0
	private var focusY = 0
	private var focusXOffset = 0f
	private var focusYOffset = 0f
	private var viewWidth = 0
	private var viewHeight = 0
	private var xPosition = 0
	private var yPosition = 0
	private var xTilePosition = 0
	private var yTilePosition = 0
	private val lookDirection = 0
	private var animationFrame = 0
	private val currentAnimation = 0
	private val name: String? = null
	private val xp = 0
	private val lvl = 0
	private val perception = 0
	private val marksmanship = 0
	private val medical = 0
	private val toughness = 0
	private val agility = 0
	private val stance = 0
	private val morale = 0
	private val specialSkills = IntArray(3)
	private var needToMove = false

	init {
		textureWidth = 256
		textureHeight = 256
	}

	fun getXPosition(): Int {
		return xPosition
	}

	fun getYPosition(): Int {
		return yPosition
	}

	fun getTileXPosition(): Float {
		return xTilePosition.toFloat()
	}

	fun getTileYPosition(): Float {
		return yTilePosition.toFloat()
	}

	fun getBuffer(): Buffers {
		return buffer
	}

	fun setTargetWaypoint(x: Int, y: Int) {
		wayPointX = x
		wayPointY = y
	}

	fun setTarget(x: Int, y: Int) {
		targetX = x
		targetY = y
	}

	fun move() {
		needToMove = if(wayPointX != xPosition || wayPointY != yPosition) {
			true
		} else {
			false
		}
		if(needToMove) {
			if(DEBUG) {
				Log.d(TAG, "Unit: going to target x:$wayPointX y:$wayPointY")
			}
			if(wayPointX != xPosition) {
				if(wayPointX > xPosition) {
					xTilePosition--
				} else {
					xTilePosition++
				}
			}
			if(wayPointY != yPosition) {
				if(wayPointY > yPosition) {
					yTilePosition--
				} else {
					yTilePosition++
				}
			}
			if(xTilePosition < -264) {
				xTilePosition = 0
				xPosition++
				yPosition--
			}
			if(xTilePosition > 264) {
				xTilePosition = 0
				xPosition--
				yPosition++
			}
			if(yTilePosition < -132) {
				yTilePosition = 0
				yPosition++
				xPosition++
			}
			if(yTilePosition > 132) {
				yTilePosition = 0
				yPosition--
				xPosition--
			}
			if(DEBUG) {
				Log.d(TAG, "Unit: location now x:$xPosition y:$yPosition")
			}
		}
		updateLocation()
	}

	fun setView(width: Int, height: Int) {
		viewWidth = width
		viewHeight = height
	}

	fun setViewFocus(x: Int, y: Int, xOffset: Float, yOffset: Float) {
		focusX = x
		focusY = y
		focusXOffset = xOffset
		focusYOffset = yOffset
	}

	fun updateLocation() {
		val xPos: Float = ((xPosition - focusX) - (yPosition - focusY)) * 132 - xTilePosition - focusXOffset
		val yPos: Float = ((xPosition - focusX) + (yPosition - focusY)) * 66 - yTilePosition - focusYOffset
		buffer.setOffsets(-xPos, -yPos)			//if(DEBUG)
		//Log.d(TAG, "Trooper: Updated location to x: " + xPos + " y: " + yPos);
	}

	fun render() {
		setTextureCoordinates(22, 0, 0, 22)
		this.addIndices(buffer)
		this.addVertexes(buffer)
		this.addTextureCoordinates(buffer)
		buffer.createBuffers(true, true, true)
	}

	private fun animate() {
		when (currentAnimation) {
			0 -> if(animationFrame < 4) // walking west
			{
				animationFrame++
			} else {
				animationFrame = 0
			}

			else -> {}
		}
	}

	fun setPosition(x: Int, y: Int) {
		xPosition = x
		yPosition = y
	}

	fun setSpecificPosition(x: Int, y: Int) {
		xTilePosition = x
		yTilePosition = y
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG =
			true // if debug mode is enabled for this class or not
	}
}
