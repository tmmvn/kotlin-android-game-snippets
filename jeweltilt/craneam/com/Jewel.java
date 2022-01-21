package jeweltilt.craneam.com;

import android.util.Log;

public class Jewel
  extends Quad
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;
    private int scoreValue;
    private int type;
    private float[] coords = new float[3];  // current position

    public Jewel(float size, int points, int jewelType, float scale)
      {
        super(size, scale);
        scoreValue = points;
        type = jewelType;
      }

    public int GetType()
      {
        return type;
      }

    public int GetScoreValue()
      {
        return scoreValue;
      }

    // Function to set position
    public void SetCoordinates(float x, float y, float z)
      {
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        if (DEBUG)
          {
            Log.d(TAG, "Jewel coordinates set to: " + x + ", " + y + ", " + z);
          }
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
  }
