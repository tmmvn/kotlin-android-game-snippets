package dragon.craneam.com;

public class UIElement
  extends Quad
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = false;    // Debug flag
    private int width;
    private int height;
    private int xPosition;
    private int yPosition;

    public UIElement(int w, int h)
      {
        super(w, h);
        width = w;
        height = h;
        textureWidth = 256;
        textureHeight = 256;
      }

    public int GetWidth()
      {
        return width;
      }

    public int GetHeight()
      {
        return height;
      }

    public int GetX()
      {
        return xPosition;
      }

    public int GetY()
      {
        return yPosition;
      }

    public void SetXPosition(int value)
      {
        for (int i = 0; i < 12; i += 3)
          {
            vertices[i] += value;
          }
        xPosition = value;
      }

    public void SetYPosition(int value)
      {
        for (int i = 1; i < 12; i += 3)
          {
            vertices[i] += value;
          }
        yPosition = value;
      }

    public void SetTextureLocation(int id)
      {
        switch (id)
          {
          case 0:
            SetTextureCoordinates(0, 255, 0, 255);
            break;
          }
      }
  }
