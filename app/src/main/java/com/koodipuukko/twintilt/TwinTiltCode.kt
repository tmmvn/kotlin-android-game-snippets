package com.koodipuukko.twintilt

import android.app.Activity
import android.graphics.Insets
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.*

class TwinTiltCode : Activity(), SensorEventListener {
	private var renderer: GLRenderer? = null // GL renderer
	private var gameLoop: GameLoop? = null // game loop -thread
	private var view: TTGLSurfaceView? = null // GL view
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val display =
			(getSystemService(DISPLAY_SERVICE) as DisplayManager).displays[0]
		val windowMetrics: WindowMetrics =
			this.windowManager.currentWindowMetrics
		val insets: Insets =
			windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
		val displayWidth =
			windowMetrics.bounds.width() - insets.left - insets.right
		val width =
			displayWidth / 2 // get display width and divide by two to make a		// correct coordinate space
		val displayHeight =
			windowMetrics.bounds.height() - insets.top - insets.bottom
		val height = displayHeight / 2 // same as above
		if(DEBUG) {
			Log.d(TAG, "Window size set at " + width + "x" + height)
		}
		mSensorManager =
			getSystemService(SENSOR_SERVICE) as SensorManager // set the sensormanager
		if(mSensorManager == null) {
			if(DEBUG) {
				Log.e(TAG, "No sensor service available, exit.")
			}
			this.finish()
		}
		mAccelerometer =
			mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // set the accelerometer
		if(mAccelerometer == null) {
			if(DEBUG) {
				Log.e(TAG, "No accelerometer available, exit.")
			}
			this.finish()
		}
		if(mSensorManager!!.registerListener(
				this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME
			)
		) // register a listener for sensor
		{
			if(DEBUG) {
				Log.d(TAG, "Accelerometer registered succesfully.")
			}
		} else {
			if(DEBUG) {
				Log.e(TAG, "Failed to register accelerometer, exit.")
			}
			this.finish()
		}                // Remove window title
		if(this.requestWindowFeature(Window.FEATURE_NO_TITLE)) {
			if(DEBUG) {
				Log.d(TAG, "FEATURE_NO_TITLE activated.")
			}
		}                // Set game to fullscreen and no dim while playing
		window.setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
			WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		)
		renderer = GLRenderer(this) // set the render used by the view
		view = TTGLSurfaceView(this, renderer!!) // set the GL view
		setContentView(view)
		gameLoop = GameLoop(
			this, this, renderer!!, width, height
		) // create the game loop -thread
		gameLoop!!.setRunning(false)
		gameLoop!!.setMenuRunning(true) // start the game loop
		try {
			if(DEBUG) {
				Log.d(TAG, "Starting gameloop...")
			}
			gameLoop!!.start()
		} catch(e: IllegalThreadStateException) {
			if(DEBUG) {
				Log.e(TAG, "Error starting game loop: $e")
			}
			this.finish()
		} finally {
			if(DEBUG) {
				Log.d(TAG, "Gameloop started succesfully.")
			}
		}
	}

	override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			gameLoop!!.returnToMenu()
		}
		return true
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {  // Handles touch events
		gameLoop!!.setTouchCoords(
			event.x, event.y
		) // Provide coords to game loop
		if(DEBUG) {
			Log.d(TAG, "Got touch event: " + event.x + ", " + event.y)
		}
		return true
	}

	fun ChangeGameLoop(newLoop: GameLoop?) {
		gameLoop = newLoop
	}

	override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
	}

	override fun onSensorChanged(event: SensorEvent) {
		var z = 0f        // Handles accelerometer events
		val x =
			event.values[0] / SensorManager.GRAVITY_EARTH // get accelerometer values and remove gravity component
		val y = event.values[1] / SensorManager.GRAVITY_EARTH
		z = event.values[2] / SensorManager.GRAVITY_EARTH
		gameLoop!!.setAccelValues(
			x, y, z
		) // pass the values to game loop thread
	}

	override fun onPause() {
		super.onPause()
		view!!.onPause()
		if(DEBUG) {
			Log.d(TAG, "Paused.")
		}
		gameLoop!!.pauseGame()
		gameLoop!!.pauseMusic()            //mSensorManager.unregisterListener(this); // unregister listener to save battery
	}

	override fun onResume() {
		super.onResume()
		view!!.onResume()
		if(DEBUG) {
			Log.d(TAG, "Unpaused.")
		}                /*if(mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME))
        {
        	if(DEBUG)
        		Log.d(TAG, "Sensor listener re-registerd succesfully.");
        }
        else
        {
        	if(DEBUG)
        		Log.e(TAG, "Sensor listener failed to re-register, exit.");
        	this.finish();
        }*/
		gameLoop!!.resumeGame()
		gameLoop!!.resumeMusic()
		gameLoop!!.calibrateAccelerometer()
	}

	@Deprecated("Deprecated in Java") override fun onBackPressed() {
		moveTaskToBack(true)
		return
	}

	override fun onDestroy() {
		gameLoop!!.stopMusic(true)
		var retry = true
		while(retry) {
			try {                // joins the thread when destroyed
				gameLoop!!.join()
				retry = false
			} catch(e: InterruptedException) {                // try again shutting down the thread
			}
		}
		mSensorManager!!.unregisterListener(this)
		if(DEBUG) {
			Log.d(TAG, "App destroyed succesfully.")
		}
		super.onDestroy()
	}

	companion object {
		private const val TAG = "Twintilt" // Game tag in logs
		private const val DEBUG = true
		private var mSensorManager: SensorManager? = null // Sensor manager
		private var mAccelerometer: Sensor? = null // accelerometer
	}
}


