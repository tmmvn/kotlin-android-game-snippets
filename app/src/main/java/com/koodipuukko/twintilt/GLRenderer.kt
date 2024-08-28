package com.koodipuukko.twintilt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.koodipuukko.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer
	( // context, needed for image loading
	private val context: Context
) : GLSurfaceView.Renderer {
	private val playerRenderList: MutableList<PlayerBall?> = ArrayList()
	private val jewelRenderList: MutableList<Jewel?> = ArrayList()
	private val uiRenderList: MutableList<UIElement?> = ArrayList()
	private val sparkleRenderList: MutableList<Sparkle?> = ArrayList()
	private var bitmap: Bitmap? = null // used to load textures
	private val textures = IntArray(NUM_TEXTURES) // texture id array
	private val bitmaps = IntArray(NUM_TEXTURES) // bitmaps id array
	private var renderingPlayer = false
	private var renderingJewels = false
	private var renderingUI = false
	private var renderingSparkle = false
	private var syncPlayer = false
	private var syncJewels = false
	private var syncUI = false
	private var syncSparkle = false
	private var clearPlayer = false
	private var clearJewels = false
	private var clearUI = false
	private var clearSparkle = false
	private var red = 0f
	private var green = 0f
	private var blue = 0f
	// constructor, just sets the context for bitmap loading
	init {
		if(DEBUG) {
			Log.d(TAG, "GLRenderer created initialized succesfully.")
		}
	}
	// function to load textures
	private fun loadTextures(gl: GL10): Boolean {
		if(DEBUG) {
			Log.d(TAG, "Loading textures.")
		}
		bitmaps[0] = R.drawable.player01 // set id's from resources
		bitmaps[1] = R.drawable.player02
		bitmaps[2] = R.drawable.diamond
		bitmaps[3] = R.drawable.stone01
		bitmaps[4] = R.drawable.stone02
		bitmaps[5] = R.drawable.score
		bitmaps[6] = R.drawable.lives
		bitmaps[7] = R.drawable.zero
		bitmaps[8] = R.drawable.one
		bitmaps[9] = R.drawable.two
		bitmaps[10] = R.drawable.three
		bitmaps[11] = R.drawable.four
		bitmaps[12] = R.drawable.five
		bitmaps[13] = R.drawable.six
		bitmaps[14] = R.drawable.seven
		bitmaps[15] = R.drawable.eight
		bitmaps[16] = R.drawable.nine
		bitmaps[17] = R.drawable.menubg
		bitmaps[18] = R.drawable.play
		bitmaps[19] = R.drawable.twinkle
		bitmaps[20] = R.drawable.high
		bitmaps[21] = R.drawable.gameover
		bitmaps[22] = R.drawable.pad1
		bitmaps[23] = R.drawable.pad2
		bitmaps[24] = R.drawable.play01
		bitmaps[25] = R.drawable.play02
		bitmaps[26] = R.drawable.gz
		bitmaps[27] = R.drawable.newhs
		bitmaps[28] = R.drawable.tap
		bitmaps[29] = R.drawable.barbg
		bitmaps[30] = R.drawable.barfill
		bitmaps[31] = R.drawable.play03
		bitmaps[32] = R.drawable.chadone
		bitmaps[33] = R.drawable.helpc
		bitmaps[34] = R.drawable.helptag
		bitmaps[35] = R.drawable.help1
		bitmaps[36] = R.drawable.help2
		bitmaps[37] = R.drawable.help3
		bitmaps[38] = R.drawable.help4
		bitmaps[39] = R.drawable.help5
		bitmaps[40] = R.drawable.help6
		bitmaps[41] = R.drawable.help7
		bitmaps[42] = R.drawable.sfx
		bitmaps[43] = R.drawable.sfxno
		bitmaps[44] = R.drawable.music
		bitmaps[45] = R.drawable.musicno
		gl.glGenTextures(
			NUM_TEXTURES,
			textures,
			0
		) // generate gl-texture indexes
		// loop through bitmaps, decode the bitmap and assign it to the correct gl-texture index
		for(i in 0 until NUM_TEXTURES) {
			bitmap = BitmapFactory.decodeResource(context.resources, bitmaps[i])
			if(bitmap == null) {
				if(DEBUG) {
					Log.w(TAG, "Bitmap decoding failed, aborting.")
					return false
				}
			}						// bind textures and set parameters
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i])
			gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR.toFloat()
			)
			gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR.toFloat()
			)
			gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE.toFloat()
			)
			gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE.toFloat()
			)
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
			bitmap?.recycle() // done, recycle the texture to free memory
		}
		return true
	}
	// Called when surface is first created
	override fun onSurfaceCreated(
		gl: GL10,
		config: EGLConfig
	) {		//gl.glEnable(GL10.GL_LIGHTING);	// enable lightning
		//gl.glEnable(GL10.GL_LIGHT0);	// enable default light
		gl.glEnable(GL10.GL_TEXTURE_2D) // enable textures
		gl.glClearDepthf(1.0f) // set the default depht when clearing screen
		gl.glEnable(GL10.GL_DEPTH_TEST) // enable depth testing
		gl.glDepthFunc(GL10.GL_LEQUAL) // depth function to use
		gl.glHint(
			GL10.GL_PERSPECTIVE_CORRECTION_HINT,
			GL10.GL_NICEST
		) // some nice perspective correction
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Surface created.")
		}
	}

	fun clearPlayerList() {
		var clearing = true
		clearPlayer = true
		while(clearing) {
			if(!renderingPlayer && !syncPlayer) {
				try {
					playerRenderList.clear()
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GLRenderer: Error clearing player list: $e")
					}
				}
				clearing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Player list cleared succesfully.")
		}
		clearPlayer = false
	}

	fun clearSparkleList() {
		var clearing = true
		clearSparkle = true
		while(clearing) {
			if(!renderingSparkle && !syncSparkle) {
				try {
					sparkleRenderList.clear()
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(
							TAG,
							"GLRenderer: Error clearing sparkle list: $e"
						)
					}
				}
				clearing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Sparkle list cleared succesfully.")
		}
		clearSparkle = false
	}

	fun clearJewelList() {
		var clearing = true
		clearJewels = true
		while(clearing) {
			if(!renderingJewels && !syncJewels) {
				try {
					jewelRenderList.clear()
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GLRenderer: Error clearing jewel list: $e")
					}
				}
				clearing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Jewel list cleared succesfully.")
		}
		clearJewels = false
	}

	fun clearUIList() {
		var clearing = true
		clearUI = true
		while(clearing) {
			if(!renderingUI && !syncUI) {
				try {
					uiRenderList.clear()
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GLRenderer: Error clearing UI list: $e")
					}
				}
				clearing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: UI list cleared succesfully.")
		}
		clearUI = false
	}

	fun synchronizePlayer(listToCopy: List<PlayerBall?>) {
		var syncing = true
		syncPlayer = true
		while(syncing) {
			if(!renderingPlayer && !clearPlayer) {
				try {
					playerRenderList.clear()
					if(DEBUG) {
						Log.d(
							TAG,
							"GLRenderer: Sychronized player list cleared."
						)
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GLRenderer: Error clearing player list: $e")
					}
				}
				for(i in listToCopy.indices) {
					try {
						playerRenderList.add(null)
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Null element added succesfully."
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to player list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to player list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to player list: $e"
							)
						}
					}
					try {
						playerRenderList[i] = listToCopy[i]
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Sychronized player list copied succesfully."
							)
						}
					} catch(e: IndexOutOfBoundsException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to player list: $e"
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to player list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to player list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to player list: $e"
							)
						}
					}
				}
				syncing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Sychronized player list succesfully.")
		}
		syncPlayer = false
	}

	fun synchronizeSparkle(listToCopy: List<Sparkle?>) {
		var syncing = true
		syncSparkle = true
		while(syncing) {
			if(!renderingSparkle && !clearSparkle) {
				try {
					sparkleRenderList.clear()
					if(DEBUG) {
						Log.d(
							TAG,
							"GLRenderer: Sychronized sparkle list cleared."
						)
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(
							TAG,
							"GLRenderer: Error clearing sparkle list: $e"
						)
					}
				}
				for(i in listToCopy.indices) {
					try {
						sparkleRenderList.add(null)
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Null element added succesfully."
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to sparkle list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to sparkle list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to sparkle list: $e"
							)
						}
					}
					try {
						sparkleRenderList[i] = listToCopy[i]
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Sychronized sparkle list copied succesfully."
							)
						}
					} catch(e: IndexOutOfBoundsException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to sparkle list: $e"
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to sparkle list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to sparkle list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to sparkle list: $e"
							)
						}
					}
				}
				syncing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Sychronized sparkle list succesfully.")
		}
		syncSparkle = false
	}

	fun synchronizeJewels(listToCopy: List<Jewel?>) {
		var syncing = true
		syncJewels = true
		while(syncing) {
			if(!renderingJewels && !clearJewels) {
				try {
					jewelRenderList.clear()
					if(DEBUG) {
						Log.d(
							TAG,
							"GLRenderer: Sychronized jewel list cleared."
						)
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GLRenderer: Error clearing jewel list: $e")
					}
				}
				for(i in listToCopy.indices) {
					try {
						jewelRenderList.add(null)
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Null element added succesfully."
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to jewel list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to jewel list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to jewel list: $e"
							)
						}
					}
					try {
						jewelRenderList[i] = listToCopy[i]
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Sychronized jewel copied succesfully."
							)
						}
					} catch(e: IndexOutOfBoundsException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to jewel list: $e"
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to jewel list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to jewel list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to jewel list: $e"
							)
						}
					}
				}
				syncing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Sychronized Jewels list succesfully.")
		}
		syncJewels = false
	}

	fun synchronizeUI(listToCopy: List<UIElement?>) {
		var syncing = true
		syncUI = true
		while(syncing) {
			if(!renderingUI && !clearUI) {
				try {
					uiRenderList.clear()
					if(DEBUG) {
						Log.d(TAG, "GLRenderer: Sychronized UI list cleared.")
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GLRenderer: Error clearing UI list: $e")
					}
				}
				for(i in listToCopy.indices) {
					try {
						uiRenderList.add(null)
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Null element added to UI list."
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to UI list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to UI list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error adding null elements to UI list: $e"
							)
						}
					}
					try {
						uiRenderList[i] = listToCopy[i]
						if(DEBUG) {
							Log.d(
								TAG,
								"GLRenderer: Sychronized UI item copied succesfully."
							)
						}
					} catch(e: IndexOutOfBoundsException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to UI list: $e"
							)
						}
					} catch(e: ClassCastException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to UI list: $e"
							)
						}
					} catch(e: UnsupportedOperationException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to UI list: $e"
							)
						}
					} catch(e: IllegalArgumentException) {
						if(DEBUG) {
							Log.e(
								TAG,
								"GLRenderer: Error copying data to UI list: $e"
							)
						}
					}
				}
				syncing = false
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Sychronized UI Element list succesfully.")
		}
		syncUI = false
	}
	// Function to be called when surface is changed
	override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
		if(loadTextures(gl)) // load textures
		{
			if(DEBUG) {
				Log.d(TAG, "Textures loaded succesfully.")
			}
		} else {
			if(DEBUG) {
				Log.w(TAG, "Texture loading failed.")
			}
		}
		gl.glViewport(0, 0, width, height) // set viewport again
		gl.glMatrixMode(GL10.GL_PROJECTION) // set matrix mode
		gl.glLoadIdentity() // load default matrix
		gl.glOrthof(
			-width / 2f, width / 2f, -height / 2f, height / 2f, -1f, 1f
		) // orthogonal perspective works better for 2D
		gl.glMatrixMode(GL10.GL_MODELVIEW) // set matrix mode to model view and reset also
		gl.glLoadIdentity()
	}

	fun flash(redAmount: Float, greenAmount: Float, blueAmount: Float) {
		red += redAmount
		if(red > 1) {
			red = 1f
		}
		if(red < 0) {
			red = 0f
		}
		green += greenAmount
		if(green > 1) {
			green = 1f
		}
		if(green < 0) {
			green = 0f
		}
		blue += blueAmount
		if(blue > 1) {
			blue = 1f
		}
		if(blue < 0) {
			blue = 0f
		}
	}
	// The draw function
	override fun onDrawFrame(gl: GL10) {
		gl.glClearColor(red, green, blue, 1.0f) // set clear color to black
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT) // clear buffers
		gl.glFrontFace(GL10.GL_CCW) // set culling direction
		gl.glEnable(GL10.GL_CULL_FACE) // enable back face culling
		gl.glCullFace(GL10.GL_BACK)
		gl.glEnable(GL10.GL_TEXTURE_2D) // enable textures
		gl.glEnable(GL10.GL_BLEND) // enable blending
		gl.glBlendFunc(
			GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA
		) // let's use additive blending since it's nice for this type of game
		gl.glNormal3f(
			0.0f,
			0.0f,
			1.0f
		) // just a default normal that lights use, basically useless atm
		if(!syncPlayer && !clearPlayer && playerRenderList.size > 0) // if data is not modified, render
		{
			renderingPlayer = true // set flag for rendering
			for(i in playerRenderList) {
				if(i != null) {
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) // enable usage of vertex arrays
					gl.glVertexPointer(
						3, GL10.GL_FLOAT, 0, i.getBuffers().getVertexBuffer()
					) // set pointer to our buffer
					gl.glColor4f(
						1f, 1f, 1f, 1f
					) // set vertex rendering color, since we use textures not really necessary
					gl.glTranslatef(
						i.getXCoordinate(),
						i.getYCoordinate(),
						i.getZCoordinate()
					) // translate the quad to it's correct position
					gl.glRotatef(i.getRotation(), 0f, 0f, 1f)
					gl.glBindTexture(
						GL10.GL_TEXTURE_2D,
						textures[i.getBuffers().getTextureId()]
					) // select correct texture
					gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // enable usage of texture coordinates
					gl.glTexCoordPointer(
						2, GL10.GL_FLOAT, 0, i.getBuffers().getTextureBuffer()
					) // set texture pointer to texcoord buffer
					gl.glDrawElements(
						GL10.GL_TRIANGLES,
						i.getBuffers().getIndicesCount(),
						GL10.GL_UNSIGNED_SHORT,
						i.getBuffers().getIndexBuffer()
					) // draw the quad buffer
					gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // disable stuff we enabled earlier
					gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
					gl.glLoadIdentity() // to make sure everything get's translated from the correct position
				}
			}
			renderingPlayer = false
		}
		if(!syncSparkle && !clearSparkle && sparkleRenderList.size > 0) // if data is not modified, render
		{
			renderingSparkle = true // set flag for rendering
			for(i in sparkleRenderList) {
				if(i != null) {
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) // enable usage of vertex arrays
					gl.glVertexPointer(
						3, GL10.GL_FLOAT, 0, i.getBuffers().getVertexBuffer()
					) // set pointer to our buffer
					gl.glColor4f(
						1f, 1f, 1f, 1f
					) // set vertex rendering color, since we use textures not really necessary
					gl.glTranslatef(
						i.getXCoordinate(),
						i.getYCoordinate(),
						i.getZCoordinate()
					) // translate the quad to it's correct position
					gl.glRotatef(i.getRotation(), 0f, 0f, 1f)
					gl.glScalef(i.getScale(), i.getScale(), 1f)
					gl.glBindTexture(
						GL10.GL_TEXTURE_2D,
						textures[i.getBuffers().getTextureId()]
					) // select correct texture
					gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // enable usage of texture coordinates
					gl.glTexCoordPointer(
						2, GL10.GL_FLOAT, 0, i.getBuffers().getTextureBuffer()
					) // set texture pointer to texcoord buffer
					gl.glDrawElements(
						GL10.GL_TRIANGLES,
						i.getBuffers().getIndicesCount(),
						GL10.GL_UNSIGNED_SHORT,
						i.getBuffers().getIndexBuffer()
					) // draw the quad buffer
					gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // disable stuff we enabled earlier
					gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
					gl.glLoadIdentity() // to make sure everything get's translated from the correct position
				}
			}
			renderingSparkle = false
		}
		if(!syncJewels && !clearJewels && jewelRenderList.size > 0) // if data is not modified, render
		{
			renderingJewels = true // set flag for rendering
			for(i in jewelRenderList) {
				if(i != null) {
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) // enable usage of vertex arrays
					gl.glVertexPointer(
						3, GL10.GL_FLOAT, 0, i.getBuffers().getVertexBuffer()
					) // set pointer to our buffer
					gl.glColor4f(
						1f, 1f, 1f, 1f
					) // set vertex rendering color, since we use textures not really necessary
					gl.glTranslatef(
						i.getXCoordinate(),
						i.getYCoordinate(),
						i.getZCoordinate()
					) // translate the quad to it's correct position
					gl.glRotatef(i.getRotation(), 0f, 0f, 1.0f)
					gl.glBindTexture(
						GL10.GL_TEXTURE_2D,
						textures[i.getBuffers().getTextureId()]
					) // select correct texture
					gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // enable usage of texture coordinates
					gl.glTexCoordPointer(
						2, GL10.GL_FLOAT, 0, i.getBuffers().getTextureBuffer()
					) // set texture pointer to texcoord buffer
					gl.glDrawElements(
						GL10.GL_TRIANGLES,
						i.getBuffers().getIndicesCount(),
						GL10.GL_UNSIGNED_SHORT,
						i.getBuffers().getIndexBuffer()
					) // draw the quad buffer
					gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // disable stuff we enabled earlier
					gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
					gl.glLoadIdentity() // to make sure everything get's translated from the correct position
				}
			}
			renderingJewels = false
		}
		if(!syncUI && !clearUI && uiRenderList.size > 0) // if data is not modified, render
		{
			renderingUI = true // set flag for rendering
			for(i in uiRenderList) {
				if(i != null) {
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) // enable usage of vertex arrays
					gl.glVertexPointer(
						3, GL10.GL_FLOAT, 0, i.getBuffers().getVertexBuffer()
					) // set pointer to our buffer
					gl.glColor4f(
						1f, 1f, 1f, 1f
					) // set vertex rendering color, since we use textures not really necessary
					gl.glTranslatef(
						i.getXCoordinate(),
						i.getYCoordinate(),
						i.getZCoordinate()
					) // translate the quad to it's correct position
					gl.glBindTexture(
						GL10.GL_TEXTURE_2D,
						textures[i.getBuffers().getTextureId()]
					) // select correct texture
					gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // enable usage of texture coordinates
					gl.glTexCoordPointer(
						2, GL10.GL_FLOAT, 0, i.getBuffers().getTextureBuffer()
					) // set texture pointer to texcoord buffer
					gl.glDrawElements(
						GL10.GL_TRIANGLES,
						i.getBuffers().getIndicesCount(),
						GL10.GL_UNSIGNED_SHORT,
						i.getBuffers().getIndexBuffer()
					) // draw the quad buffer
					gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // disable stuff we enabled earlier
					gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
					gl.glLoadIdentity() // to make sure everything get's translated from the correct position
				}
			}
			renderingUI = false
		}
		gl.glLoadIdentity() // make sure we're at the beginning for translations
		gl.glDisable(GL10.GL_TEXTURE_2D) // continue disabling stuff
		gl.glDisable(GL10.GL_CULL_FACE)
	}

	companion object {
		private const val TAG = "Twintilt" // Game tag in logs
		private const val DEBUG = true // Sets debug mode on or off
		private const val NUM_TEXTURES = 46 // constant for texture amount
	}
}
