package com.koodipuukko.dragonsquad

import java.util.ArrayList
import android.util.Log

class UI {
	private val graphicsBuffer: Buffers = Buffers()
	var textFields: MutableList<Text> = ArrayList<Text>()

	init {
		try {
			textFields.clear()
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "UI: Error clearing text fields")
			}
		}
	}

	fun addGraphicElement(element: UIElement) {
		element.addIndices(graphicsBuffer)
		element.addVertexes(graphicsBuffer)
		element.addTextureCoordinates(graphicsBuffer)
	}

	fun addTextElement(element: Text?) {
		try {
			if(element != null) {
				textFields.add(element)
				if(DEBUG) {
					Log.d(TAG, "UI: Added text field succesfully.")
				}
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "UI: Error adding text element: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "UI: Error adding text element: $e")
			}
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "UI: Error adding text element: $e")
			}
		}
	}

	fun getGraphicsBuffer(): Buffers {
		return graphicsBuffer
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG = true // Debug flag
	}
}
