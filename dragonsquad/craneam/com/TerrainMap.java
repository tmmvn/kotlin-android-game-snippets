package dragon.craneam.com;

import android.util.Log;

public class TerrainMap
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = true;  // if debug mode is enabled for this class or not
    private int width;
    private int height;
    private int viewWidth;
    private int viewHeight;
    private int[] mapSquares;
    private int[] viewSquares;
    private Buffers bufferLayer1;
    private Buffers bufferLayer2;
    private int focusSquareX;
    private int focusSquareY;
    private float focusXOffset;
    private float focusYOffset;
    private boolean scrollUp;
    private boolean scrollLeft;
    private boolean scrollRight;
    private boolean scrollDown;

    public TerrainMap()
      {
        bufferLayer1 = new Buffers();
        bufferLayer2 = new Buffers();
        viewWidth = 10;
        viewHeight = 10;
        viewSquares = new int[viewWidth * viewHeight];
      }

    public int GetViewFocusX()
      {
        return focusSquareX;
      }

    public float GetViewXOffset()
      {
        return focusXOffset;
      }

    public float GetViewYOffset()
      {
        return focusYOffset;
      }

    public int GetViewFocusY()
      {
        return focusSquareY;
      }

    public int GetViewWidth()
      {
        return viewWidth;
      }

    public int GetViewHeight()
      {
        return viewHeight;
      }

    public Buffers GetFloorBuffer()
      {
        return bufferLayer1;
      }

    public Buffers GetWallBuffer()
      {
        return bufferLayer2;
      }

    public void SetFocusSquare(int x, int y)
      {
        focusSquareX = x;
        focusSquareY = y;
        if (DEBUG)
          {
            Log.d(TAG, "TerrainMap: Focus square changed: " + x + "," + y);
          }
      }

    public void PanMap(float xAmount, float yAmount)
      {
        boolean needMapUpdate = false;
        if (DEBUG)
          {
            Log.d(TAG, "X: " + xAmount + " Y: " + yAmount);
          }
        if (( scrollLeft && scrollRight ) || ( scrollLeft && xAmount < 0 ) || ( scrollRight && xAmount > 0 ))
          {
            focusXOffset += xAmount;
          }
        if (( scrollUp && scrollDown ) || ( scrollDown && yAmount < 0 ) || ( scrollUp && yAmount > 0 ))
          {
            focusYOffset += yAmount;
          }
        if (focusYOffset > 132f || focusYOffset < -132f)
          {
            if (focusYOffset < 0)
              {
                focusSquareY--;
                focusSquareX--;
                scrollUp = true;
                focusYOffset = 0f;
              }
            else
              {
                focusSquareY++;
                focusSquareX++;
                scrollDown = true;
                focusYOffset = -0f;
              }
            needMapUpdate = true;
          }
        if (focusXOffset > 264f || focusXOffset < -264f)
          {
            if (focusXOffset < 0)
              {
                focusSquareX--;
                focusSquareY++;
                scrollRight = true;
                focusXOffset = 0f;
              }
            else
              {
                focusSquareX++;
                focusSquareY--;
                scrollLeft = true;
                focusXOffset = -0f;
              }
            needMapUpdate = true;
          }
        if (focusSquareX < -1)
          {
            focusSquareX = -1;
            scrollLeft = false;
          }
        else if (focusSquareX > width - viewWidth - 1)
          {
            focusSquareX = width - viewWidth - 1;
            scrollRight = false;
          }
        if (focusSquareY < viewHeight / 2)
          {
            focusSquareY = viewHeight / 2;
            scrollDown = false;
          }
        else if (focusSquareY > height - viewHeight)
          {
            focusSquareY = height - viewHeight;
            scrollUp = false;
          }
        if (DEBUG)
          {
            Log.d(TAG, "TerrainMap: Focus X: " + focusSquareX + " Y: " + focusSquareY);
          }
        bufferLayer1.SetOffsets(focusXOffset, focusYOffset);
        bufferLayer2.SetOffsets(focusXOffset, focusYOffset);
        if (needMapUpdate)
          {
            UpdateMap();
          }
      }

    public boolean PrepareRenderData()
      {
        TerrainSquare square;
        int xPos, yPos;
        int viewTileCount = 0;
        for (int y = -viewHeight / 2 + 1; y < viewHeight / 2 + 1; y++)
          {
            for (int x = -1; x < viewWidth - 1; x++)
              {
                square = new TerrainSquare();
                square.SetTextureLocation(0);
                xPos = ( x - y ) * 132;
                yPos = ( x + y ) * 66;
                square.Offset(xPos, yPos);
                square.AddIndices(bufferLayer1);
                square.AddVertexes(bufferLayer1);
                square.AddTextureCoordinates(bufferLayer1);
                square.SetTextureLocation(7);
                square.AddIndices(bufferLayer2);
                square.AddVertexes(bufferLayer2);
                square.AddTextureCoordinates(bufferLayer2);
                viewTileCount++;
              }
          }
        bufferLayer1.CreateBuffers(true, true, true);
        bufferLayer2.CreateBuffers(true, true, true);
        if (DEBUG)
          {
            Log.d(TAG, "TerrainMap: Render data prepared succesfully with " + viewTileCount + "tiles.");
          }
        return true;
      }

    public boolean LoadMap(int mapId)
      {
        int numTiles;
        boolean even = false;
        focusXOffset = 0;
        focusYOffset = 0;
        scrollUp = true;
        scrollDown = true;
        scrollLeft = true;
        scrollRight = true;
        bufferLayer1.SetTextureId(0);
        bufferLayer2.SetTextureId(0);
        width = 30;
        height = 30;
        focusSquareX = 10;
        focusSquareY = 10;
        numTiles = width * height;
        mapSquares = new int[numTiles];
        for (int i = 0; i < numTiles; i++)
          {
            if (even)
              {
                mapSquares[i] = 0;
                even = false;
              }
            else
              {
                mapSquares[i] = 0;
                even = true;
              }
          }
        mapSquares[430] = 1;
        if (DEBUG)
          {
            Log.d(TAG, "TerrainMap: Map Loaded Succesfully with " + numTiles + " tiles");
          }
        return true;
      }

    public void UpdateMap()
      {
        int n = 0;
        int mapPosition;
        TerrainSquare square = new TerrainSquare();
        for (int y = focusSquareY; y < focusSquareY + viewHeight; y++)
          {
            for (int x = focusSquareX; x < focusSquareX + viewWidth; x++)
              {
                mapPosition = x + y * width;
                if (mapSquares[mapPosition] != viewSquares[n])
                  {
                    if (mapSquares[mapPosition] != 0)
                      {
                        square.SetTextureLocation(mapSquares[mapPosition]);
                      }
                    else
                      {
                        square.SetTextureLocation(7);
                      }
                    for (int j = 0; j < 8; j++)
                      {
                        bufferLayer2.ChangeTextureBuffer(n * 8 + j, square.GetTextureCoordinate(j));
                      }
                    viewSquares[n] = mapSquares[mapPosition];
                  }
                n++;
              }
          }
        bufferLayer2.CreateBuffers(false, false, true);
      }
  }
