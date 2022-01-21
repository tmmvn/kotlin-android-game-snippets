package jeweltilt.craneam.com;

import android.util.Log;

public class Sparkle
  extends Quad
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;
    private int type;
    private int lifeTime;
    private boolean larger;
    private float scale = 1;
    private float[] coords = new float[3];  // current position

    public Sparkle(float size, int sparkleType, float scale)
      {
        super(size, scale);
        type = sparkleType;
        lifeTime = 0;
        larger = true;
      }

    public float GetScale()
      {
        return scale;
      }

    // Function to set position
    public void SetCoordinates(float x, float y, float z)
      {
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        if (DEBUG)
          {
            Log.d(TAG, "Sparkle coordinates set to: " + x + ", " + y + ", " + z);
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

    public boolean Animate()
      {
        if (lifeTime <= 25)
          {
            lifeTime++;
            if (type == 1)
              {
                this.Rotate(10);
                if (scale < 1.8f && larger)
                  {
                    this.scale += 0.1f;
                  }
                else
                  {
                    larger = false;
                  }
                if (!larger)
                  {
                    this.scale -= 0.1f;
                  }
              }
            if (type == 2)
              {
                this.Rotate(-10);
                if (scale < 1.8f && larger)
                  {
                    this.scale += 0.1f;
                  }
                else
                  {
                    larger = false;
                  }
                if (!larger)
                  {
                    this.scale -= 0.1f;
                  }
              }
            if (type == 3)
              {
                this.Rotate(-10);
                this.scale -= 0.1f;
              }
            if (type == 4)
              {
                this.Rotate(10);
                this.scale -= 0.1f;
              }
            return true;
          }
        else
          {
            lifeTime = 0;
            return false;
          }
      }
  }
