package com.koodipuukko.dragonsquad

import android.util.Log

class TerrainMap {
	private var width = 0
	private var height = 0
	private val viewWidth: Int
	private val viewHeight: Int
	private lateinit var mapSquares: IntArray
	private val viewSquares: IntArray
	private val bufferLayer1: Buffers
	private val bufferLayer2: Buffers
	private var focusSquareX = 0
	private var focusSquareY = 0
	private var focusXOffset = 0f
	private var focusYOffset = 0f
	private var scrollUp = false
	private var scrollLeft = false
	private var scrollRight = false
	private var scrollDown = false

	init {
		bufferLayer1 = Buffers()
		bufferLayer2 = Buffers()
		viewWidth = 10
		viewHeight = 10
		viewSquares = IntArray(viewWidth * viewHeight)
	}

	fun getViewFocusX(): Int {
		return focusSquareX
	}

	fun getViewXOffset(): Float {
		return focusXOffset
	}

	fun getViewYOffset(): Float {
		return focusYOffset
	}

	fun getViewFocusY(): Int {
		return focusSquareY
	}

	fun getViewWidth(): Int {
		return viewWidth
	}

	fun getViewHeight(): Int {
		return viewHeight
	}

	fun getFloorBuffer(): Buffers {
		return bufferLayer1
	}

	fun getWallBuffer(): Buffers {
		return bufferLayer2
	}

	fun setFocusSquare(x: Int, y: Int) {
		focusSquareX = x
		focusSquareY = y
		if(DEBUG) {
			Log.d(TAG, "TerrainMap: Focus square changed: $x,$y")
		}
	}

	fun panMap(xAmount: Float, yAmount: Float) {
		var needMapUpdate = false
		if(DEBUG) {
			Log.d(TAG, "X: $xAmount Y: $yAmount")
		}
		if((scrollLeft && scrollRight) || (scrollLeft && xAmount < 0) || (scrollRight && xAmount > 0)) {
			focusXOffset += xAmount
		}
		if((scrollUp && scrollDown) || (scrollDown && yAmount < 0) || (scrollUp && yAmount > 0)) {
			focusYOffset += yAmount
		}
		if(focusYOffset > 132f || focusYOffset < -132f) {
			if(focusYOffset < 0) {
				focusSquareY--
				focusSquareX--
				scrollUp = true
				focusYOffset = 0f
			} else {
				focusSquareY++
				focusSquareX++
				scrollDown = true
				focusYOffset = -0f
			}
			needMapUpdate = true
		}
		if(focusXOffset > 264f || focusXOffset < -264f) {
			if(focusXOffset < 0) {
				focusSquareX--
				focusSquareY++
				scrollRight = true
				focusXOffset = 0f
			} else {
				focusSquareX++
				focusSquareY--
				scrollLeft = true
				focusXOffset = -0f
			}
			needMapUpdate = true
		}
		if(focusSquareX < -1) {
			focusSquareX = -1
			scrollLeft = false
		} else if(focusSquareX > width - viewWidth - 1) {
			focusSquareX = width - viewWidth - 1
			scrollRight = false
		}
		if(focusSquareY < viewHeight / 2) {
			focusSquareY = viewHeight / 2
			scrollDown = false
		} else if(focusSquareY > height - viewHeight) {
			focusSquareY = height - viewHeight
			scrollUp = false
		}
		if(DEBUG) {
			Log.d(TAG, "TerrainMap: Focus X: $focusSquareX Y: $focusSquareY")
		}
		bufferLayer1.setOffsets(focusXOffset, focusYOffset)
		bufferLayer2.setOffsets(focusXOffset, focusYOffset)
		if(needMapUpdate) {
			updateMap()
		}
	}

	fun prepareRenderData(): Boolean {
		var square: TerrainSquare
		var xPos: Int
		var yPos: Int
		var viewTileCount = 0
		for(y in -viewHeight / 2 + 1 until viewHeight / 2 + 1) {
			for(x in -1 until viewWidth - 1) {
				square = TerrainSquare()
				square.setTextureLocation(0)
				xPos = (x - y) * 132
				yPos = (x + y) * 66
				square.offset(xPos, yPos)
				square.addIndices(bufferLayer1)
				square.addVertexes(bufferLayer1)
				square.addTextureCoordinates(bufferLayer1)
				square.setTextureLocation(7)
				square.addIndices(bufferLayer2)
				square.addVertexes(bufferLayer2)
				square.addTextureCoordinates(bufferLayer2)
				viewTileCount++
			}
		}
		bufferLayer1.createBuffers(i = true, v = true, t = true)
		bufferLayer2.createBuffers(i = true, v = true, t = true)
		if(DEBUG) {
			Log.d(
				TAG,
				"TerrainMap: Render data prepared succesfully with " + viewTileCount + "tiles."
			)
		}
		return true
	}

	fun loadMap(mapId: Int): Boolean {
		var even = false
		focusXOffset = 0f
		focusYOffset = 0f
		scrollUp = true
		scrollDown = true
		scrollLeft = true
		scrollRight = true
		bufferLayer1.setTextureId(0)
		bufferLayer2.setTextureId(0)
		width = 30
		height = 30
		focusSquareX = 10
		focusSquareY = 10
		val numTiles = width * height
		mapSquares = IntArray(numTiles)
		for(i in 0 until numTiles) {
			if(even) {
				mapSquares[i] = 0
				even = false
			} else {
				mapSquares[i] = 0
				even = true
			}
		}
		mapSquares[430] = 1
		if(DEBUG) {
			Log.d(
				TAG,
				"TerrainMap: Map Loaded Succesfully with $numTiles tiles"
			)
		}
		return true
	}

	fun updateMap() {
		var n = 0
		var mapPosition: Int
		val square: TerrainSquare = TerrainSquare()
		for(y in focusSquareY until focusSquareY + viewHeight) {
			for(x in focusSquareX until focusSquareX + viewWidth) {
				mapPosition = x + y * width
				if(mapSquares[mapPosition] != viewSquares[n]) {
					if(mapSquares[mapPosition] != 0) {
						square.setTextureLocation(mapSquares[mapPosition])
					} else {
						square.setTextureLocation(7)
					}
					for(j in 0..7) {
						bufferLayer2.changeTextureBuffer(
							n * 8 + j,
							square.getTextureCoordinate(j)
						)
					}
					viewSquares[n] = mapSquares[mapPosition]
				}
				n++
			}
		}
		bufferLayer2.createBuffers(false, false, true)
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG =
			true // if debug mode is enabled for this class or not
	}
}
