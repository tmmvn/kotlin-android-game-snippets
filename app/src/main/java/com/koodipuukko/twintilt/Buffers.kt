package com.koodipuukko.twintilt

import android.util.Log
import java.nio.*

class Buffers {
	private var vbb: ByteBuffer? = null
	private var ibb: ByteBuffer? = null
	private var tbb: ByteBuffer? = null // Byte buffers for everything
	private var vertexBuffer: FloatBuffer
	private var textureBuffer: FloatBuffer // Final float buffers
	private var indexBuffer: ShortBuffer // indice buffer
	private var vbCount = 0
	private var ibCount = 0
	private var tbCount = 0 // to keep track of how many of everything we have
	private var textureId = 0 // texture id
	private var indices: ShortArray // array for indices
	private var vertices: FloatArray // array for vertices
	private var textureCoordinates: FloatArray // array for texcoords
	// Constructor
	init {
		vertices = FloatArray(0) // initilaize arrays
		indices = ShortArray(0)
		textureCoordinates = FloatArray(0)
		try {  // initialize buffers and catch any errors
			if(DEBUG) {
				Log.d(TAG, "Allocating vertex buffer.")
			}
			vbb = ByteBuffer.allocateDirect(indices.size * 2)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "Failed to allocate vertex buffer: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Vertex buffer allocated.")
			}
		}
		try {
			if(DEBUG) {
				Log.d(TAG, "Allocating index buffer.")
			}
			ibb = ByteBuffer.allocateDirect(vertices.size * 2)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "Failed to allocate index buffer: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Index buffer allocated.")
			}
		}
		try {
			if(DEBUG) {
				Log.d(TAG, "Allocating texture coordinates buffer.")
			}
			tbb = ByteBuffer.allocateDirect(textureCoordinates.size * 2)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "Failed to allocate texture coordinates buffer: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Vertex buffer allocated.")
			}
		}
		indexBuffer = ibb!!.asShortBuffer()
		vertexBuffer = vbb!!.asFloatBuffer()
		textureBuffer = tbb!!.asFloatBuffer()
	}
	// function to set texture id, everything in a buffer is rendered with same texture
	fun setTextureId(id: Int) {
		textureId = id
	}
	// function to return texture id
	fun getTextureId(): Int {
		return textureId
	}
	// function to get indices amount
	fun getIndicesCount(): Int {
		return ibCount
	}
	// functions to return buffers
	fun getVertexBuffer(): FloatBuffer {
		return vertexBuffer
	}

	fun getIndexBuffer(): ShortBuffer {
		return indexBuffer
	}

	fun getTextureBuffer(): FloatBuffer {
		return textureBuffer
	}
	// functions to add arrays to buffers
	fun addToVertexBuffer(vbToAdd: FloatArray) {
		val addLength = vbToAdd.size // length of array to add
		if((addLength + vbCount) > vertices.size) // if not enough room, increase the size of array
		{
			if(DEBUG) {
				Log.d(TAG, "Increased vertex buffer size.")
			}
			val newSize = vertices.size + addLength * 2
			val temp = FloatArray(newSize)
			System.arraycopy(vertices, 0, temp, 0, vertices.size)
			vertices = temp
		}
		System.arraycopy(
			vbToAdd,
			0,
			vertices,
			vbCount,
			addLength
		) // add the array
		vbCount += addLength // increase tracker to correct amount
	}
	// Same as above
	fun addToTextureBuffer(tbToAdd: FloatArray) {
		val addLength = tbToAdd.size
		if((addLength + tbCount) > textureCoordinates.size) {
			if(DEBUG) {
				Log.d(TAG, "Increased texture coordinates buffer size.")
			}
			val newSize = textureCoordinates.size + addLength * 2
			val temp = FloatArray(newSize)
			System.arraycopy(
				textureCoordinates,
				0,
				temp,
				0,
				textureCoordinates.size
			)
			textureCoordinates = temp
		}
		System.arraycopy(tbToAdd, 0, textureCoordinates, tbCount, addLength)
		tbCount += addLength
	}
	// Same as above
	fun addToIndexBuffer(ibToAdd: ShortArray) {
		val addLength = ibToAdd.size
		if((addLength + ibCount) > indices.size) {
			if(DEBUG) {
				Log.d(TAG, "Increased index buffer size.")
			}
			val newSize = indices.size + addLength * 2
			val temp = ShortArray(newSize)
			System.arraycopy(indices, 0, temp, 0, indices.size)
			indices = temp
		}
		System.arraycopy(ibToAdd, 0, indices, ibCount, addLength)
		for(i in ibCount until indices.size) {
			indices[i] = (indices[i] + ibCount * 2 / 3).toShort()
		}
		ibCount += addLength
	}
	// function to clear arrays
	fun clearArrays() {
		val tempFloat1 = FloatArray(0)
		val tempFloat2 = FloatArray(0)
		val tempShort = ShortArray(0)
		vertices = tempFloat1
		indices = tempShort
		textureCoordinates = tempFloat2
		ibCount = 0
		vbCount = 0
		tbCount = 0
		if(DEBUG) {
			Log.d(TAG, "Cleared arrays.")
		}
	}
	// function to clear buffers
	private fun clearBuffers() {
		vbb!!.clear()
		ibb!!.clear()
		tbb!!.clear()
		indexBuffer.clear()
		try {
			indexBuffer.compact() // apparently required due to some odd short/int buffer bug
		} catch(e: ReadOnlyBufferException) {
			if(DEBUG) {
				Log.e(TAG, "Index buffer compact failed: $e")
			}
		}
		vertexBuffer.clear()
		try {
			vertexBuffer.compact()
		} catch(e: ReadOnlyBufferException) {
			if(DEBUG) {
				Log.e(TAG, "Vertex buffer compact failed: $e")
			}
		}
		textureBuffer.clear()
		try {
			textureBuffer.compact()
		} catch(e: ReadOnlyBufferException) {
			if(DEBUG) {
				Log.e(TAG, "Texture coordinates buffer compact failed: $e")
			}
		}
		if(DEBUG) {
			Log.d(TAG, "Cleared buffers.")
		}
	}
	// Creates all the buffers
	fun createBuffers() {
		clearBuffers()
		createIndexBuffer()
		createVertexBuffer()
		createTextureBuffer()
	}
	// Creates index buffer by allocating enough space to accomodate the array and then add the array in to the final
	// buffer
	private fun createIndexBuffer() {
		try {
			if(DEBUG) {
				Log.d(TAG, "Allocating index buffer.")
			}
			ibb = ByteBuffer.allocateDirect(indices.size * 2)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "Index buffer allocation failed: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Index buffer allocation succesful.")
			}
		}
		ibb!!.order(ByteOrder.nativeOrder())
		indexBuffer = ibb!!.asShortBuffer()
		try {
			if(DEBUG) {
				Log.d(TAG, "Adding array to buffer (index).")
			}
			indexBuffer.put(indices)
		} catch(e: BufferOverflowException) {
			if(DEBUG) {
				Log.e(TAG, "Index buffer overflow: $e")
			}
		} catch(e: ReadOnlyBufferException) {
			if(DEBUG) {
				Log.e(TAG, "Index buffer put failed: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Array to buffer (index) succesful.")
			}
		}
		indexBuffer.position(0)
	}
	// same as above
	private fun createVertexBuffer() {
		try {
			if(DEBUG) {
				Log.d(TAG, "Allocating vertex buffer.")
			}
			vbb = ByteBuffer.allocateDirect(vertices.size * 4)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "Vertex buffer allocation failed: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Vertex buffer allocation succesful.")
			}
		}
		vbb!!.order(ByteOrder.nativeOrder())
		vertexBuffer = vbb!!.asFloatBuffer()
		try {
			if(DEBUG) {
				Log.d(TAG, "Adding array to buffer (vertex).")
			}
			vertexBuffer.put(vertices)
		} catch(e: BufferOverflowException) {
			if(DEBUG) {
				Log.e(TAG, "Vertex buffer overflow: $e")
			}
		} catch(e: ReadOnlyBufferException) {
			if(DEBUG) {
				Log.e(TAG, "Vertex buffer put failed: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Array to buffer (vertex) succesful.")
			}
		}
		vertexBuffer.position(0)
	}
	// same as above
	private fun createTextureBuffer() {
		try {
			if(DEBUG) {
				Log.d(TAG, "Allocating texture coordinates buffer.")
			}
			tbb = ByteBuffer.allocateDirect(textureCoordinates.size * 4)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "Texture coordinates buffer allocation failed: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Texture coordinates buffer allocation succesful.")
			}
		}
		tbb!!.order(ByteOrder.nativeOrder())
		textureBuffer = tbb!!.asFloatBuffer()
		try {
			if(DEBUG) {
				Log.d(TAG, "Adding array to buffer (texture coordinates).")
			}
			textureBuffer.put(textureCoordinates)
		} catch(e: BufferOverflowException) {
			if(DEBUG) {
				Log.e(TAG, "Texture coordinates buffer overflow: $e")
			}
		} catch(e: ReadOnlyBufferException) {
			if(DEBUG) {
				Log.e(TAG, "Texture coordinates buffer put failed: $e")
			}
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Array to buffer (texture coordinates) succesful.")
			}
		}
		textureBuffer.position(0)
	}

	companion object {
		private const val TAG = "Jewel Tilt" // Game tag in logs
		private const val DEBUG = false
	}
}
