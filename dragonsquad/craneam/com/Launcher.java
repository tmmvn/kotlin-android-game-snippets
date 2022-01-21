package dragon.craneam.com;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class Launcher
  extends Activity
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = false;    // Debug flag
    private GLRenderer renderer;  // GL renderer
    GLSurfaceView view;  // GL view
    Display display;
    Portal portal;

    /**
     Called when the activity is first created.
     */
    @Override public void onCreate(Bundle savedInstanceState)
      {
        super.onCreate(savedInstanceState);
        display = ( (WindowManager) getSystemService(Context.WINDOW_SERVICE) ).getDefaultDisplay(); // set display
        if (DEBUG)
          {
            Log.d(TAG, "Window size set at " + display.getWidth() + "x" + display.getHeight());
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
        GLSurfaceView view = new GLSurfaceView(this);  // set the GL view
        renderer = new GLRenderer(this);        // set the render used by the view
        view.setRenderer(renderer);
        setContentView(view);
        portal = new Portal(renderer, display);
        portal.start();
      }

    @Override public boolean onTouchEvent(MotionEvent event)
      {  // Handles touch events
        portal.PassTouchEvent(event);
        return true;
      }
  }