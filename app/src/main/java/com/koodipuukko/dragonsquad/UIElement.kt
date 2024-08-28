package com.koodipuukko.dragonsquad

class UIElement
	(private val width: Int, private val height: Int) : Quad(width, height) {
	private var xPosition = 0
	private var yPosition = 0

	init {
		textureWidth = 256
		textureHeight = 256
	}

	fun getWidth(): Int {
		return width
	}

	fun getHeight(): Int {
		return height
	}

	fun getX(): Int {
		return xPosition
	}

	fun getY(): Int {
		return yPosition
	}

	fun setXPosition(value: Int) {
		var i = 0
		while(i < 12) {
			vertices[i] = vertices[i] + value
			i += 3
		}
		xPosition = value
	}

	fun setYPosition(value: Int) {
		var i = 1
		while(i < 12) {
			vertices[i] = vertices[i] + value
			i += 3
		}
		yPosition = value
	}

	fun setTextureLocation(id: Int) {
		when (id) {
			0 -> setTextureCoordinates(0, 255, 0, 255)
		}
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG = false // Debug flag
	}
}
