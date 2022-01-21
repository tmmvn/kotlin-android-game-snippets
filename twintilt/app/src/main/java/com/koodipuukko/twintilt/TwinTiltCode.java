package com.koodipuukko.twintilt;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class TwinTiltCode
  extends Activity
  implements SensorEventListener
  {
    private static final String TAG = "Twintilt";  // Game tag in logs
    private static final boolean DEBUG = true;
    private GLRenderer renderer;  // GL renderer
    private GameLoop gameLoop;  // game loop -thread
    private TTGLSurfaceView view;  // GL view
    private static SensorManager mSensorManager;  // Sensor manager
    private static Sensor mAccelerometer;  // accelerometer

    protected void onCreate(Bundle savedInstanceState)
      {
        super.onCreate(savedInstanceState);
        Display display
          = ( (WindowManager) getSystemService(Context.WINDOW_SERVICE) ).getDefaultDisplay(); // set display
        int width = display.getWidth() / 2;  // get display width and divide by two to make a correct coordinate space
        int height = display.getHeight() / 2;  // same as above
        if (DEBUG)
          {
            Log.d(TAG, "Window size set at " + width + "x" + height);
          }
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  // set the sensormanager
        if (mSensorManager == null)
          {
            if (DEBUG)
              {
                Log.e(TAG, "No sensor service available, exit.");
              }
            this.finish();
          }
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  // set the accelerometer
        if (mAccelerometer == null)
          {
            if (DEBUG)
              {
                Log.e(TAG, "No accelerometer available, exit.");
              }
            this.finish();
          }
        if (mSensorManager.registerListener(this,
          mAccelerometer,
          SensorManager.SENSOR_DELAY_GAME))  // register a listener for sensor
          {
            if (DEBUG)
              {
                Log.d(TAG, "Accelerometer registered succesfully.");
              }
          }
        else
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to register accelerometer, exit.");
              }
            this.finish();
          }
        // Remove window title
        if (this.requestWindowFeature(Window.FEATURE_NO_TITLE))
          {
            if (DEBUG)
              {
                Log.d(TAG, "FEATURE_NO_TITLE activated.");
              }
          }
        // Set game to fullscreen and no dim while playing
        getWindow().setFlags(
          WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
          WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        renderer = new GLRenderer(this);        // set the render used by the view
        view = new TTGLSurfaceView(this, renderer);  // set the GL view
        setContentView(view);
        gameLoop = new GameLoop(this, this, renderer, width, height);  // create the game loop -thread
        gameLoop.SetRunning(false);
        gameLoop.SetMenuRunning(true);  // start the game loop
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Starting gameloop...");
              }
            gameLoop.start();
          }
        catch (IllegalThreadStateException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error starting game loop: " + e);
              }
            this.finish();
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Gameloop started succesfully.");
              }
          }
      }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event)
      {
        if (keyCode == KeyEvent.KEYCODE_MENU)
          {
            gameLoop.ReturnToMenu();
          }
        return true;
      }

    @Override public boolean onTouchEvent(MotionEvent event)
      {  // Handles touch events
        gameLoop.SetTouchCoords(event.getX(), event.getY());  // Provide coords to game loop
        if (DEBUG)
          {
            Log.d(TAG, "Got touch event: " + event.getX() + ", " + event.getY());
          }
        return true;
      }

    public void ChangeGameLoop(GameLoop newLoop)
      {
        gameLoop = newLoop;
      }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
      {
      }

    public void onSensorChanged(SensorEvent event)
      {  // Handles accelerometer events
        float x, y, z = 0;
        x = event.values[0] / SensorManager.GRAVITY_EARTH;  // get accelerometer values and remove gravity component
        y = event.values[1] / SensorManager.GRAVITY_EARTH;
        z = event.values[2] / SensorManager.GRAVITY_EARTH;
        gameLoop.SetAccelValues(x, y, z);  // pass the values to game loop thread
      }

    @Override protected void onPause()
      {
        super.onPause();
        view.onPause();
        if (DEBUG)
          {
            Log.d(TAG, "Paused.");
          }
        gameLoop.PauseGame();
        gameLoop.PauseMusic();
        //mSensorManager.unregisterListener(this); // unregister listener to save battery
      }

    @Override protected void onResume()
      {
        super.onResume();
        view.onResume();
        if (DEBUG)
          {
            Log.d(TAG, "Unpaused.");
          }
        /*if(mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME))
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
        gameLoop.ResumeGame();
        gameLoop.ResumeMusic();
        gameLoop.CalibrateAccelerometer();
      }

    @Override public void onBackPressed()
      {
        moveTaskToBack(true);
        return;
      }

    @Override protected void onDestroy()
      {
        gameLoop.StopMusic(true);
        boolean retry = true;
        while (retry)
          {
            try
              {
                // joins the thread when destroyed
                gameLoop.join();
                retry = false;
              }
            catch (InterruptedException e)
              {
                // try again shutting down the thread
              }
          }
        mSensorManager.unregisterListener(this);
        if (DEBUG)
          {
            Log.d(TAG, "App destroyed succesfully.");
          }
      }
  }


