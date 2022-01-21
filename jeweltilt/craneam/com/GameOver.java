package jeweltilt.craneam.com;

import android.content.Context;
import android.util.Log;

public class GameOver
  extends Thread
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;  // Sets debug mode on or off
    private GameLoop gameLoop;
    private JewelTilt mainThread;
    private Context context;
    private GLRenderer renderer;
    private int MAX_WIDTH;
    private int MAX_HEIGHT;

    public GameOver(JewelTilt main, GameLoop gameThread, Context passedContext, GLRenderer rend, int width, int height)
      {
        gameLoop = gameThread;
        context = passedContext;
        renderer = rend;
        MAX_WIDTH = width;
        MAX_HEIGHT = height;
        mainThread = main;
      }

    @Override public void run()
      {
        try
          {
            Thread.sleep(5000);
          }
        catch (InterruptedException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameOver: Error sleeping: " + e);
              }
          }
        try
          {
            gameLoop.interrupt();
          }
        catch (SecurityException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameOver: Error stopping gameloop: " + e);
              }
          }
        gameLoop = null;
        gameLoop = new GameLoop(mainThread, context, renderer, MAX_WIDTH, MAX_HEIGHT);
        mainThread.ChangeGameLoop(gameLoop);
        gameLoop.SetMenuRunning(true);
        gameLoop.SetRunning(false);
        gameLoop.run();
        try
          {
            this.interrupt();
          }
        catch (SecurityException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameOver: Error stopping me: " + e);
              }
          }
      }
  }
