package com.koodipuukko.dragonsquad

import android.util.Log

open class Quad
	(width: Int, height: Int) {
	protected var vertices: FloatArray = FloatArray(12) // vertices of the quad
	private val indices = shortArrayOf(0, 1, 2, 0, 2, 3) // indices draw order
	private val texCoords = floatArrayOf(
		// texture coordinates
		1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
	)
	protected var textureWidth: Int = 0 // width of the texture used
	protected var textureHeight: Int = 0 // height of the texture used
	private var textureId = 0 // texture id to be used in rendering
	private var rotation: Float // rotation value
	private var listIndex = 0 // index of the quad in any lists

	init {
		vertices[0] =
			-width / 2f // Create vertex coordinates according to given dimensions
		vertices[1] = height / 2f
		vertices[2] = 0f
		vertices[3] = -width / 2f
		vertices[4] = -height / 2f
		vertices[5] = 0f
		vertices[6] = width / 2f
		vertices[7] = -height / 2f
		vertices[8] = 0f
		vertices[9] = width / 2f
		vertices[10] = height / 2f
		vertices[11] = 0f
		rotation = 0f // set rotation to default
		if(DEBUG) {
			Log.d(TAG, "Quad: Created quad succesfully.")
		}
	}
	/**
		* Sets texture coordinates for texture atlases and such
	 * @param xMin
	 * Description Starting x-coordinate of texture.
	 * @param xMax
	 * Description Final x-coordinate of texture.
	 * @param yMin
	 * Description Starting y-coordinate of texture
	 * @param yMax
	 * Description Ending y-coordinate of texture
	 */
	fun setTextureCoordinates(
		xMin: Int,
		xMax: Int,
		yMin: Int,
		yMax: Int
	) {		// We divide the given coordinates with texture dimensions to get the correct values
		texCoords[4] = xMin.toFloat() / textureWidth
		texCoords[5] = yMin.toFloat() / textureHeight
		texCoords[6] = xMin.toFloat() / textureWidth
		texCoords[7] = yMax.toFloat() / textureHeight
		texCoords[0] = xMax.toFloat() / textureWidth
		texCoords[1] = yMax.toFloat() / textureHeight
		texCoords[2] = xMax.toFloat() / textureWidth
		texCoords[3] = yMin.toFloat() / textureHeight
		if(DEBUG) {
			Log.d(TAG, "Quad: Changed texture coords succesfully")
		}
	}
	/**
		* Sets the index of the quad in any lists
	 * @param indexToSet
	 * Description Index of the item in the list.
	 */
	fun setListIndex(indexToSet: Int) {
		listIndex = indexToSet
	}
	/**
		* Returns current list index
	 * @return The quad's current index in the list it is in.
	 */
	fun getListIndex(): Int {
		return listIndex
	}
	/**
		* Rotates the quad
	 * @param amount
	 * Description Amount to rotate in degrees.
	 */
	fun rotate(amount: Float) {
		rotation += amount				// make sure we stay within +-360 range
		if(rotation > 360) {
			rotation = 0f
		}
		if(rotation < -360) {
			rotation = 0f
		}
	}
	/**
		* Return's the current amount of rotation
	 * @return The current amount of rotation in degrees
	 */
	fun getRotation(): Float {
		return rotation
	}
	/**
		* Sets the current texture id of the quad
	 * @param id
	 * Description The texture id to use.
	 */
	fun setTexture(id: Int) {
		textureId = id
		if(DEBUG) {
			Log.d(TAG, "Quad: texture id set to $id")
		}
	}

	fun addIndices(buffers: Buffers) {
		buffers.addToIndexBuffer(indices)
	}

	fun addTextureCoordinates(buffers: Buffers) {
		buffers.addToTextureBuffer(texCoords)
	}

	fun addVertexes(buffers: Buffers) {
		buffers.addToVertexBuffer(vertices)
	}

	fun sendTexture(buffers: Buffers) {
		buffers.setTextureId(textureId)
	}

	fun getTextureCoordinate(index: Int): Float {
		return texCoords[index]
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG =
			false // if debug mode is enabled for this class or not
	}
}
