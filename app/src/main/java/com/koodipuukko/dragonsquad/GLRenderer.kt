package com.koodipuukko.dragonsquad

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.EGLConfig
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.koodipuukko.R
import java.util.ArrayList
import javax.microedition.khronos.opengles.GL10

class GLRenderer
	(passedContext: Context) : GLSurfaceView.Renderer {
	private var terrainFloorBuffer: Buffers? = null
	private var terrainWallBuffer: Buffers? = null
	private var friendlyUnitBuffer: Buffers? = null
	private var enemyUnitBuffer: Buffers? = null
	private var uiBuffer: Buffers? = null
	private val uiTextBuffer: MutableList<Buffers> = ArrayList<Buffers>()
	private val context: Context =
		passedContext // context, needed for image loading
	private var bitmap: Bitmap? = null // used to load textures
	private val textures = IntArray(NUM_TEXTURES) // texture id array
	private val bitmaps = IntArray(NUM_TEXTURES) // bitmaps id array
	private var renderingTerrain = false
	private var renderingUnits = false
	private var renderingUI = false
	private var clearingTerrain = false
	private var clearingUnits = false
	private var clearingUI = false
	/**
		* Constructor
	 * @param passedContext
	 * Description Context to get resources.
	 */
	init {
		try {
			uiTextBuffer.clear()
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GLRenderer: Error clearing text item list")
			}
		}
		if(DEBUG) {
			Log.d(TAG, "GLRenderer created initialized succesfully.")
		}
	}
	/**
		* Loads textures
	 * @param gl
	 * Description OpenGL reference to use.
	 */
	private fun loadTextures(gl: GL10): Boolean {
		if(DEBUG) {
			Log.d(TAG, "Loading textures.")
		}
		bitmaps[0] = R.drawable.grass
		bitmaps[1] = R.drawable.grass
		bitmaps[2] = R.drawable.soldier1
		bitmaps[3] = R.drawable.font01
		gl.glGenTextures(
			NUM_TEXTURES,
			textures,
			0
		) // generate gl-texture indexes
		// loop through bitmaps, decode the bitmap and assign it to the correct gl-texture index
		for(i in 0 until NUM_TEXTURES) {
			bitmap =
				BitmapFactory.decodeResource(context.getResources(), bitmaps[i])
			if(bitmap == null) {
				if(DEBUG) {
					Log.w(TAG, "Bitmap decoding failed, aborting.")
					return false
				}
			}						// bind textures and set paramete1rs
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i])
			gl.glTexParameterx(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR
			)
			gl.glTexParameterx(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR
			)
			gl.glTexParameterx(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE
			)
			gl.glTexParameterx(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE
			)
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
			bitmap?.recycle() // done, recycle the texture to free memory
			if(DEBUG) {
				Log.d(TAG, "Bitmap loading succesfully")
			}
		}
		return true
	}
	/**
		* Called when the surface is created
	 * @param gl
	 * Description OpenGL-reference.
	 * @param config
	 * Description Config to use.
	 */
	override fun onSurfaceCreated(
		gl: GL10?,
		config: javax.microedition.khronos.egl.EGLConfig?
	) {		//gl.glEnable(GL10.GL_LIGHTING);	// enable lightning
		//gl.glEnable(GL10.GL_LIGHT0);	// enable default light
		gl?.glEnable(GL10.GL_TEXTURE_2D) // enable textures
		gl?.glClearDepthf(1.0f) // set the default depht when clearing screen
		gl?.glEnable(GL10.GL_DEPTH_TEST) // enable depth testing
		gl?.glDepthFunc(GL10.GL_LEQUAL) // depth function to use
		gl?.glHint(
			GL10.GL_PERSPECTIVE_CORRECTION_HINT,
			GL10.GL_NICEST
		) // some nice perspective correction
		if(DEBUG) {
			Log.d(TAG, "GLRenderer: Surface created.")
		}
	}
	/**
		* Called when the surface is changed
	 * @param gl
	 * Description OpenGL-reference
	 * @param width
	 * Description Width of the screen.
	 * @param height
	 * Description Height of the screen.
	 */
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
		}		/*if(CreateLight(gl))	// create the default light
	    {
	    	if(DEBUG)
	    		Log.d(TAG, "Lights created succesfully.");
	    }
	    else
	    	if(DEBUG)
	    		Log.w(TAG, "Light creation failed.");*/
		gl.glViewport(0, 0, width, height) // set viewport again
		gl.glMatrixMode(GL10.GL_PROJECTION) // set matrix mode
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) // enable usage of vertex arrays
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY) // enable usage of texture coordinates
		gl.glLoadIdentity() // load default matrix
		gl.glOrthof(
			0F,
			800F,
			480F,
			0F,
			-1F,
			1F
		) // orthogonal perspective works better for 2D
		gl.glMatrixMode(GL10.GL_MODELVIEW) // set matrix mode to model view and reset also
		gl.glLoadIdentity()
	}

	fun clearTerrainBuffers(): Boolean {
		if(!renderingTerrain) {
			clearingTerrain = true
			terrainFloorBuffer = null
			terrainWallBuffer = null
			clearingTerrain = false
			return true
		} else {
			return false
		}
	}

	fun clearUnitBuffers(): Boolean {
		if(!renderingUnits) {
			clearingUnits = true
			friendlyUnitBuffer = null
			enemyUnitBuffer = null
			clearingUnits = false
			return true
		} else {
			return false
		}
	}

	fun clearUIBuffers(): Boolean {
		if(!renderingUI) {
			clearingUI = true
			uiBuffer = null
			try {
				uiTextBuffer.clear()
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GLRenderer: Error clearing UI buffer: $e")
				}
				return false
			}
			clearingUI = false
			return true
		} else {
			return false
		}
	}

	fun setTerrainFloorBuffer(buffer: Buffers?) {
		terrainFloorBuffer = buffer
	}

	fun setTerrainWallBuffer(buffer: Buffers?) {
		terrainWallBuffer = buffer
	}

	fun setFriendlyUnitBuffer(buffer: Buffers?) {
		friendlyUnitBuffer = buffer
	}

	fun setEnemyUnitBuffer(buffer: Buffers?) {
		enemyUnitBuffer = buffer
	}

	fun setUIBuffer(ui: UI) {
		uiBuffer = ui.getGraphicsBuffer()
		for(i in 0 until ui.textFields.size) {
			try {
				uiTextBuffer.add(
					ui.textFields.get(i).getBuffer()
				)
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GLRenderer: Error adding text element: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GLRenderer: Error adding text element: $e")
				}
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GLRenderer: Error adding text element: $e")
				}
			}
		}
	}
	/**
		* Draw's the scene
	 * @param gl
	 * Description OpenGL-reference.
	 */
	override fun onDrawFrame(gl: GL10) {
		gl.glClearColor(0f, 0f, 0f, 1.0f) // set clear color to black
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT) // clear buffers
		gl.glFrontFace(GL10.GL_CCW) // set culling direction
		gl.glEnable(GL10.GL_CULL_FACE) // enable back face culling
		gl.glCullFace(GL10.GL_FRONT)
		gl.glEnable(GL10.GL_TEXTURE_2D) // enable textures
		gl.glEnable(GL10.GL_BLEND) // enable blending
		gl.glBlendFunc(
			GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA
		) // let's use additive blending since it's nice for this type of game
		renderingTerrain = true
		if(terrainFloorBuffer != null && !clearingTerrain) {
			gl.glBindTexture(
				GL10.GL_TEXTURE_2D,
				textures[terrainFloorBuffer!!.getTextureId()]
			) // select correct texture
			gl.glVertexPointer(
				3,
				GL10.GL_FLOAT,
				0,
				terrainFloorBuffer!!.getVertexBuffer()
			) // set pointer to our buffer
			gl.glColor4f(
				1f,
				1f,
				1f,
				1f
			) // set vertex rendering color, since we use textures not really necessary
			gl.glTranslatef(
				terrainFloorBuffer!!.getXOffset(),
				terrainFloorBuffer!!.getYOffset(),
				0f
			) // translate the quad to it's correct position
			gl.glRotatef(0f, 0f, 0f, 1f)
			gl.glTexCoordPointer(
				2, GL10.GL_FLOAT, 0, terrainFloorBuffer!!.getTextureBuffer()
			) // set texture pointer to texcoord buffer
			gl.glDrawElements(
				GL10.GL_TRIANGLES,
				terrainFloorBuffer!!.getIndicesCount(),
				GL10.GL_UNSIGNED_SHORT,
				terrainFloorBuffer!!.getIndexBuffer()
			) // draw the quad buffer
			gl.glLoadIdentity() // to make sure everything get's translated from the correct position
		}
		renderingTerrain = false
		renderingUnits = true
		if(friendlyUnitBuffer != null && !clearingUnits) {
			gl.glBindTexture(
				GL10.GL_TEXTURE_2D,
				textures[friendlyUnitBuffer!!.getTextureId()]
			) // select correct texture
			gl.glVertexPointer(
				3,
				GL10.GL_FLOAT,
				0,
				friendlyUnitBuffer!!.getVertexBuffer()
			) // set pointer to our buffer
			gl.glColor4f(
				1f,
				1f,
				1f,
				1f
			) // set vertex rendering color, since we use textures not really necessary
			gl.glTranslatef(
				friendlyUnitBuffer!!.getXOffset(),
				friendlyUnitBuffer!!.getYOffset(),
				0f
			) // translate the quad to it's correct position
			gl.glRotatef(0f, 0f, 0f, 1f)
			gl.glTexCoordPointer(
				2, GL10.GL_FLOAT, 0, friendlyUnitBuffer!!.getTextureBuffer()
			) // set texture pointer to texcoord buffer
			gl.glDrawElements(
				GL10.GL_TRIANGLES,
				friendlyUnitBuffer!!.getIndicesCount(),
				GL10.GL_UNSIGNED_SHORT,
				friendlyUnitBuffer!!.getIndexBuffer()
			) // draw the quad buffer
			gl.glLoadIdentity() // to make sure everything get's translated from the correct position
		}
		if(enemyUnitBuffer != null && !clearingUnits) {
			gl.glBindTexture(
				GL10.GL_TEXTURE_2D,
				textures[enemyUnitBuffer!!.getTextureId()]
			) // select correct texture
			gl.glVertexPointer(
				3,
				GL10.GL_FLOAT,
				0,
				enemyUnitBuffer!!.getVertexBuffer()
			) // set pointer to our buffer
			gl.glColor4f(
				1f,
				1f,
				1f,
				1f
			) // set vertex rendering color, since we use textures not really necessary
			gl.glTranslatef(
				enemyUnitBuffer!!.getXOffset(), enemyUnitBuffer!!.getYOffset
					(), 0f
			) // translate the quad to it's correct position
			gl.glRotatef(0f, 0f, 0f, 1f)
			gl.glTexCoordPointer(
				2, GL10.GL_FLOAT, 0, enemyUnitBuffer!!.getTextureBuffer()
			) // set texture pointer to texcoord buffer
			gl.glDrawElements(
				GL10.GL_TRIANGLES,
				enemyUnitBuffer!!.getIndicesCount(),
				GL10.GL_UNSIGNED_SHORT,
				enemyUnitBuffer!!.getIndexBuffer()
			) // draw the quad buffer
			gl.glLoadIdentity() // to make sure everything get's translated from the correct position
		}
		renderingUnits = false
		renderingTerrain = true
		if(terrainWallBuffer != null && !clearingTerrain) {
			gl.glBindTexture(
				GL10.GL_TEXTURE_2D,
				textures[terrainWallBuffer!!.getTextureId()]
			) // select correct texture
			gl.glVertexPointer(
				3,
				GL10.GL_FLOAT,
				0,
				terrainWallBuffer!!.getVertexBuffer()
			) // set pointer to our buffer
			gl.glColor4f(
				1f,
				1f,
				1f,
				1f
			) // set vertex rendering color, since we use textures not really necessary
			gl.glTranslatef(
				terrainFloorBuffer!!.getXOffset(),
				terrainWallBuffer!!.getYOffset(),
				0f
			) // translate the quad to it's correct position
			gl.glRotatef(0f, 0f, 0f, 1f)
			gl.glTexCoordPointer(
				2, GL10.GL_FLOAT, 0, terrainWallBuffer!!.getTextureBuffer()
			) // set texture pointer to texcoord buffer
			gl.glDrawElements(
				GL10.GL_TRIANGLES,
				terrainWallBuffer!!.getIndicesCount(),
				GL10.GL_UNSIGNED_SHORT,
				terrainWallBuffer!!.getIndexBuffer()
			) // draw the quad buffer
			gl.glLoadIdentity() // to make sure everything get's translated from the correct position
		}
		renderingTerrain = false
		renderingUI = true				// TODO: ADD UI RENDERING HERE
		if(uiTextBuffer.isNotEmpty() && !clearingUI) {
			gl.glBindTexture(
				GL10.GL_TEXTURE_2D,
				textures[3]
			) // select correct texture
			for(i in 0 until uiTextBuffer.size) {
				gl.glVertexPointer(
					3, GL10.GL_FLOAT, 0, uiTextBuffer[i].getVertexBuffer()
				) // set pointer to our buffer
				gl.glColor4f(
					1f,
					1f,
					1f,
					1f
				) // set vertex rendering color, since we use textures not really necessary
				gl.glTranslatef(
					uiTextBuffer[i].getXOffset(),
					uiTextBuffer[i].getYOffset(),
					0f
				) // translate the quad to it's correct position
				gl.glRotatef(0f, 0f, 0f, 1f)
				gl.glTexCoordPointer(
					2, GL10.GL_FLOAT, 0, uiTextBuffer[i].getTextureBuffer()
				) // set texture pointer to texcoord buffer
				gl.glDrawElements(
					GL10.GL_TRIANGLES,
					uiTextBuffer[i].getIndicesCount(),
					GL10.GL_UNSIGNED_SHORT,
					uiTextBuffer[i].getIndexBuffer()
				) // draw the quad buffer
				gl.glLoadIdentity() // to make sure everything get's translated from the correct position
			}
		}
		renderingUI =
			false				//gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	// disable stuff we enabled earlier
		//gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D) // continue disabling stuff
		gl.glDisable(GL10.GL_CULL_FACE)
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG = true // Sets debug mode on or off
		private const val NUM_TEXTURES = 4 // constant for texture amount
	}
}
