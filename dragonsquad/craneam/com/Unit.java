package dragon.craneam.com;

import android.util.Log;

public class Unit
  extends Quad
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = true;  // if debug mode is enabled for this class or not
    private Buffers buffer;
    private int id;
    private int responseLevel;
    private int currentCommand;
    private int targetX;
    private int targetY;
    private int wayPointX;
    private int wayPointY;
    private int focusX;
    private int focusY;
    private float focusXOffset;
    private float focusYOffset;
    private int viewWidth;
    private int viewHeight;
    private int xPosition;
    private int yPosition;
    private int xTilePosition;
    private int yTilePosition;
    private int lookDirection;
    private int animationFrame;
    private int currentAnimation;
    private String name;
    private int xp;
    private int lvl;
    private int perception;
    private int marksmanship;
    private int medical;
    private int toughness;
    private int agility;
    private int stance;
    private int morale;
    private int[] specialSkills = new int[3];
    private boolean needToMove = false;

    public Unit(int trooperId)
      {
        super(48, 48);
        id = trooperId;
        buffer = new Buffers();
        textureWidth = 256;
        textureHeight = 256;
      }

    public int GetXPosition()
      {
        return xPosition;
      }

    public int GetYPosition()
      {
        return yPosition;
      }

    public float GetTileXPosition()
      {
        return xTilePosition;
      }

    public float GetTileYPosition()
      {
        return yTilePosition;
      }

    public Buffers GetBuffer()
      {
        return buffer;
      }

    public void SetTargetWaypoint(int x, int y)
      {
        wayPointX = x;
        wayPointY = y;
      }

    public void SetTarget(int x, int y)
      {
        targetX = x;
        targetY = y;
      }

    public void Move()
      {
        if (wayPointX != xPosition || wayPointY != yPosition)
          {
            needToMove = true;
          }
        else
          {
            needToMove = false;
          }
        if (needToMove)
          {
            if (DEBUG)
              {
                Log.d(TAG, "Unit: going to target x:" + wayPointX + " y:" + wayPointY);
              }
            if (wayPointX != xPosition)
              {
                if (wayPointX > xPosition)
                  {
                    xTilePosition--;
                  }
                else
                  {
                    xTilePosition++;
                  }
              }
            if (wayPointY != yPosition)
              {
                if (wayPointY > yPosition)
                  {
                    yTilePosition--;
                  }
                else
                  {
                    yTilePosition++;
                  }
              }
            if (xTilePosition < -264)
              {
                xTilePosition = 0;
                xPosition++;
                yPosition--;
              }
            if (xTilePosition > 264)
              {
                xTilePosition = 0;
                xPosition--;
                yPosition++;
              }
            if (yTilePosition < -132)
              {
                yTilePosition = 0;
                yPosition++;
                xPosition++;
              }
            if (yTilePosition > 132)
              {
                yTilePosition = 0;
                yPosition--;
                xPosition--;
              }
            if (DEBUG)
              {
                Log.d(TAG, "Unit: location now x:" + xPosition + " y:" + yPosition);
              }
          }
        UpdateLocation();
      }

    public void SetView(int width, int height)
      {
        viewWidth = width;
        viewHeight = height;
      }

    public void SetViewFocus(int x, int y, float xOffset, float yOffset)
      {
        focusX = x;
        focusY = y;
        focusXOffset = xOffset;
        focusYOffset = yOffset;
      }

    public void UpdateLocation()
      {
        float xPos = 0;
        float yPos = 0;
        xPos = ( ( xPosition - focusX ) - ( yPosition - focusY ) ) * 132 - xTilePosition - focusXOffset;
        yPos = ( ( xPosition - focusX ) + ( yPosition - focusY ) ) * 66 - yTilePosition - focusYOffset;
        buffer.SetOffsets(-xPos, -yPos);
        //if(DEBUG)
        //Log.d(TAG, "Trooper: Updated location to x: " + xPos + " y: " + yPos);
      }

    public void Render()
      {
        SetTextureCoordinates(22, 0, 0, 22);
        this.AddIndices(buffer);
        this.AddVertexes(buffer);
        this.AddTextureCoordinates(buffer);
        buffer.CreateBuffers(true, true, true);
      }

    private void Animate()
      {
        switch (currentAnimation)
          {
          case 0:
            if (animationFrame < 4)  // walking west
              {
                animationFrame++;
              }
            else
              {
                animationFrame = 0;
              }
            break;
          default:
            break;
          }
      }

    public void SetPosition(int x, int y)
      {
        xPosition = x;
        yPosition = y;
      }

    public void SetSpecificPosition(int x, int y)
      {
        xTilePosition = x;
        yTilePosition = y;
      }
  }
