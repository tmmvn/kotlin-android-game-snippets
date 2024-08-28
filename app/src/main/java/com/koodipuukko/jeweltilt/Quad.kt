package com.koodipuukko.jeweltilt

import android.util.Log
import kotlin.math.sqrt

open class Quad
	(size: Float, scale: Float) {
	private var textureId = 0 // texture id to be used in rendering
	private val buffers: Buffers = Buffers() // buffers used for rendering
	private val vertices = FloatArray(12) // vertices of the quad
	private val indices = shortArrayOf(0, 1, 2, 0, 2, 3) // indices draw order
	private val texCoords = floatArrayOf( // texture coordinates
		0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f
	)
	private var rotation: Float // rotation value
	private var radius: Float // radius, used in collision tests
	private var listIndex = 0
	// Constructor
	init {
		vertices[0] =
			scale * size * -1 // Create vertex coordinates and scale by size
		vertices[1] = scale * size * 1
		vertices[2] = scale * size * 0
		vertices[3] = scale * size * -1
		vertices[4] = scale * size * -1
		vertices[5] = scale * size * 0
		vertices[6] = scale * size * 1
		vertices[7] = scale * size * -1
		vertices[8] = scale * size * 0
		vertices[9] = scale * size * 1
		vertices[10] = scale * size * 1
		vertices[11] = scale * size * 0
		radius =
			(sqrt(8.0).toFloat() * scale * size) / 2 // Calculate radius for bounding sphere
		rotation = 0f
		if(DEBUG) {
			Log.d(TAG, "Quad of size " + scale * size + " created.")
		}
	}

	fun setListIndex(indexToSet: Int) {
		listIndex = indexToSet
	}

	fun getListIndex(): Int {
		return listIndex
	}
	// Function to distort
	fun distort(x: Float, y: Float) {
		run {
			var i = 0
			while(i < 12) {
				vertices[i] *= x
				i += 3
			}
		}
		texCoords[4] *= x
		texCoords[6] *= x
		var i = 1
		while(i < 12) {
			vertices[i] *= y
			i += 3
		}
		texCoords[5] *= y
		texCoords[3] *= y
		radius *= (x * y)
	}
	// Function to rotate
	fun rotate(amount: Float) {
		rotation += amount
		if(rotation > 360) {
			rotation = 0f
		}
		if(rotation < -360) {
			rotation = 0f
		}
	}
	//Function to return rotation amount
	fun getRotation(): Float {
		return rotation
	}
	// Function to return radius
	fun getRadius(): Float {
		return radius
	}
	// Function to return buffers
	fun getBuffers(): Buffers {
		return buffers
	}
	// Function to return texture id
	fun setTexture(id: Int) {
		textureId = id
		buffers.setTextureId(textureId)
		if(DEBUG) {
			Log.d(TAG, "Quad texture id set to $id")
		}
	}
	// Function to clear buffer-arrays
	fun clearBuffer() {
		buffers.clearArrays()
	}
	// Function to draw the quad to its determined buffers
	fun draw() {
		buffers.addToIndexBuffer(indices)
		buffers.addToTextureBuffer(texCoords)
		buffers.addToVertexBuffer(vertices)
		buffers.setTextureId(textureId)
		if(DEBUG) {
			Log.d(TAG, "Quad drawn to buffers.")
		}
	}

	companion object {
		private const val TAG = "Jewel Tilt" // Game tag in logs
		private const val DEBUG = false
	}
}
