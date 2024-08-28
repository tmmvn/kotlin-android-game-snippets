package com.koodipuukko.dragonsquad

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager

class Launcher : Activity() {
	private var renderer: GLRenderer? = null // GL renderer
	var view: GLSurfaceView? = null // GL view
	private var display: Display? = null
	private var portal: Portal? = null
	/**
		* Called when the activity is first created.
	 */
	@Override override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		display =
			(getSystemService(Context.WINDOW_SERVICE) as WindowManager).getDefaultDisplay() // set display
		if(DEBUG) {
			Log.d(
				TAG,
				("Window size set at " + display!!.getWidth()).toString() + "x"
				+ display!!.getHeight()
			)
		}				// Remove window title
		if(this.requestWindowFeature(Window.FEATURE_NO_TITLE)) {
			if(DEBUG) {
				Log.d(TAG, "FEATURE_NO_TITLE activated.")
			}
		}				// Set game to fullscreen and no dim while playing
		window.setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
			WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		)
		val view: GLSurfaceView = GLSurfaceView(this) // set the GL view
		renderer = GLRenderer(this) // set the render used by the view
		view.setRenderer(renderer)
		setContentView(view)
		portal = Portal(renderer!!, display!!)
		portal?.start()
	}

	@Override override fun onTouchEvent(event: MotionEvent?): Boolean {  // Handles touch events
		portal?.passTouchEvent(event!!)
		return true
	}

	companion object {
		private const val TAG = "Dragon Squad" // Game tag in logs
		private const val DEBUG = false // Debug flag
	}
}
