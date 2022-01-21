package dragon.craneam.com;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;

public class Portal
  extends Thread
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = true;    // Debug flag
    GLRenderer renderer;
    Display display;
    private boolean scrolling = false;
    private int scrollCounter = 0;
    private boolean prepareScrolling = false;
    private boolean running = true;
    // variables to be used to get constant FPS
    private long beginTime;
    private long timeDiff;
    private int sleepTime;
    private final static int MAX_FPS = 50;  // Max FPS
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;  // How time each frame takes
    TerrainMap test;
    Unit friendly;
    UI ui = new UI();
    Text testi;

    public Portal(GLRenderer rendererToUse, Display displayToUse)
      {
        renderer = rendererToUse;
        display = displayToUse;
        LoadData();
      }

    private void LoadData()
      {
        test = new TerrainMap();
        test.LoadMap(0);
        test.PrepareRenderData();
        test.UpdateMap();
        testi = new Text(50, 50, 1);
        testi.SetText("X: " + test.GetViewFocusX() + " Y: " + test.GetViewFocusY());
        testi.Render();
        ui.AddTextElement(testi);
        friendly = new Unit(0);
        friendly.GetBuffer()
          .SetTextureId(2);
        friendly.Render();
        friendly.SetPosition(12, 12);
        friendly.SetView(test.GetViewWidth(), test.GetViewHeight());
        friendly.SetTargetWaypoint(13, 13);
        friendly.UpdateLocation();
        renderer.SetUIBuffer(ui);
        renderer.SetTerrainFloorBuffer(test.GetFloorBuffer());
        renderer.SetTerrainWallBuffer(test.GetWallBuffer());
        renderer.SetFriendlyUnitBuffer(friendly.GetBuffer());
      }

    @Override public void run()
      {
        while (running)
          {
            // store current time
            beginTime = System.currentTimeMillis();
            friendly.Move();
            // calculate if sleeping is needed to have constant FPS
            timeDiff = System.currentTimeMillis() - beginTime;
            sleepTime = (int) ( FRAME_PERIOD - timeDiff );
            // need to sleep
            if (sleepTime > 0)
              {
                try
                  {
                    Thread.sleep(sleepTime);
                  }
                catch (InterruptedException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error sleeping: " + e);
                      }
                  }
              }
          }
      }

    public void PassTouchEvent(MotionEvent event)
      {
        int eventAction = event.getAction();
        switch (eventAction)
          {
          case MotionEvent.ACTION_DOWN:
            prepareScrolling = true;
            break;
          case MotionEvent.ACTION_MOVE:
            if (scrolling)
              {
                test.PanMap(( display.getWidth() / 2 - event.getX() ) / -30f,
                  ( display.getHeight() / 2 - event.getY() ) / -30f);
                friendly.SetViewFocus(test.GetViewFocusX(),
                  test.GetViewFocusY(),
                  test.GetViewXOffset(),
                  test.GetViewYOffset());
                friendly.UpdateLocation();
              }
            else
              {
                if (prepareScrolling)
                  {
                    scrollCounter++;
                  }
              }
            if (scrollCounter >= 9)
              {
                scrolling = true;
              }
            break;
          case MotionEvent.ACTION_UP:
            if (!scrolling)
              {
                friendly.SetTargetWaypoint(GetTouchedTile(event.getX(), event.getY())[0],
                  GetTouchedTile(event.getX(), event.getY())[1]);
              }
            scrolling = false;
            prepareScrolling = false;
            scrollCounter = 0;
            break;
          default:
            break;
          }
        if (DEBUG)
          {
            Log.d(TAG, "Got touch event: " + event.getX() + ", " + event.getY());
          }
      }

    private int[] GetTouchedTile(float touchX, float touchY)
      {
        int[] touchCoords = new int[2];
        touchCoords[0] = (int) ( touchX / 132 + touchY / 66 ) / 2;
        touchCoords[1] = (int) ( touchX / 132 - touchY / 66 ) / 2;
        touchCoords[0] += test.GetViewFocusX();
        touchCoords[1] += test.GetViewFocusY();
        if (DEBUG)
          {
            Log.d(TAG, "Portal: touched tile at x: " + touchCoords[0] + " y: " + touchCoords[1]);
          }
        return touchCoords;
      }
  }
