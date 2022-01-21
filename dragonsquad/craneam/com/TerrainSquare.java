package dragon.craneam.com;

public class TerrainSquare
  extends Quad
  {
    public TerrainSquare()
      {
        super(384, 192);
        textureWidth = 256;
        textureHeight = 256;
      }

    public void SetTextureLocation(int location)
      {
        // texture coordinate presets for tiles
        switch (location)
          {
          case 0:
            SetTextureCoordinates(0, 127, 0, 63);  // top left
            break;
          case 1:
            SetTextureCoordinates(128, 255, 0, 63);  // top right
            break;
          case 2:
            SetTextureCoordinates(0, 127, 64, 127);  // second left
            break;
          case 3:
            SetTextureCoordinates(128, 255, 64, 127);  // second right
            break;
          case 4:
            SetTextureCoordinates(0, 128, 128, 191);  // third left
            break;
          case 5:
            SetTextureCoordinates(128, 255, 128, 191);  // third right
            break;
          case 6:
            SetTextureCoordinates(0, 128, 192, 255);  // bottom left
            break;
          case 7:
            SetTextureCoordinates(128, 255, 192, 255);  // bottom right
            break;
          }
      }

    public void Offset(int x, int y)
      {
        for (int i = 0; i < 12; i += 3)
          {
            vertices[i] += x;
          }
        for (int i = 1; i < 12; i += 3)
          {
            vertices[i] += y;
          }
      }
  }
