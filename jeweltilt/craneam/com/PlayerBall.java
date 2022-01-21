package jeweltilt.craneam.com;

import android.util.Log;

public class PlayerBall
  extends Quad
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;
    private int MAX_WIDTH, MAX_HEIGHT, MIN_WIDTH, MIN_HEIGHT;  // movement bounds
    private float[] coords = new float[3];  // current position
    private int type;

    public PlayerBall(float size, float scale)
      {
        super(size, scale);
        type = 1;
      }

    public void ChangeType(int newType)
      {
        type = newType;
      }

    public int GetType()
      {
        return type;
      }

    // Function to set position
    public void SetCoordinates(float x, float y, float z)
      {
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        if (DEBUG)
          {
            Log.d(TAG, "Player ball coordinates set to: " + x + ", " + y + ", " + z);
          }
      }

    public void SetMovemenetBounds(int maxX, int maxY, int minX, int minY)
      {
        MAX_WIDTH = maxX;  // Set bounds for movement
        MAX_HEIGHT = maxY;
        MIN_WIDTH = minX;
        MIN_HEIGHT = minY;
      }

    // Functions to return coordinates
    public float GetXCoordinate()
      {
        return coords[0];
      }

    public float GetYCoordinate()
      {
        return coords[1];
      }

    public float GetZCoordinate()
      {
        return coords[2];
      }

    public void Move(float x, float y, float z)
      {
        // Check if x is to be changed
        if (x != 0)
          {
            // Move and if out of bounds set to limits
            coords[0] += x;
            if (coords[0] < MIN_WIDTH)
              {
                coords[0] = MIN_WIDTH;
              }
            if (coords[0] > MAX_WIDTH)
              {
                coords[0] = MAX_WIDTH;
              }
          }
        // Same as x
        if (y != 0)
          {
            coords[1] += y;
            if (coords[1] < MIN_HEIGHT)
              {
                coords[1] = MIN_HEIGHT;
              }
            if (coords[1] > MAX_HEIGHT)
              {
                coords[1] = MAX_HEIGHT;
              }
          }
        // Same as y
        if (z != 0)
          {
            coords[2] += z;
          }
      }
  }
