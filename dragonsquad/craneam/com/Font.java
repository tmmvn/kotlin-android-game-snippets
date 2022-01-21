package dragon.craneam.com;

public class Font
  extends Quad
  {
    public Font(int size)
      {
        super(size, size);
        textureWidth = 256;
        textureHeight = 256;
      }

    public void SetTextureLocation(int location)
      {
        // texture coordinate presets for tiles
        switch (location)
          {
          case 0:
            SetTextureCoordinates(31, 0, 0, 31);  // a
            break;
          case 1:
            SetTextureCoordinates(63, 32, 0, 31);  // b
            break;
          case 2:
            SetTextureCoordinates(95, 64, 0, 31);  // c
            break;
          case 3:
            SetTextureCoordinates(127, 96, 0, 31);  // d
            break;
          case 4:
            SetTextureCoordinates(159, 128, 0, 31);  // e
            break;
          case 5:
            SetTextureCoordinates(191, 160, 0, 31);  // f
            break;
          case 6:
            SetTextureCoordinates(223, 192, 0, 31);  // g
            break;
          case 7:
            SetTextureCoordinates(255, 224, 0, 31);  // h
            break;
          case 8:
            SetTextureCoordinates(31, 0, 32, 63);  // i
            break;
          case 9:
            SetTextureCoordinates(63, 32, 32, 63);  // j
            break;
          case 10:
            SetTextureCoordinates(95, 64, 32, 63);  // k
            break;
          case 11:
            SetTextureCoordinates(127, 96, 32, 63);  // l
            break;
          case 12:
            SetTextureCoordinates(159, 128, 32, 63);  // m
            break;
          case 13:
            SetTextureCoordinates(191, 160, 32, 63);  // n
            break;
          case 14:
            SetTextureCoordinates(223, 192, 32, 63);  // o
            break;
          case 15:
            SetTextureCoordinates(255, 224, 32, 63);  // p
            break;
          case 16:
            SetTextureCoordinates(31, 0, 64, 95);  // q
            break;
          case 17:
            SetTextureCoordinates(63, 32, 64, 95);  // r
            break;
          case 18:
            SetTextureCoordinates(95, 64, 64, 95);  // s
            break;
          case 19:
            SetTextureCoordinates(127, 96, 64, 95);  // t
            break;
          case 20:
            SetTextureCoordinates(159, 128, 64, 95);  // u
            break;
          case 21:
            SetTextureCoordinates(191, 160, 64, 95);  // v
            break;
          case 22:
            SetTextureCoordinates(223, 192, 64, 95);  // w
            break;
          case 23:
            SetTextureCoordinates(255, 224, 64, 95);  // x
            break;
          case 24:
            SetTextureCoordinates(31, 0, 96, 127);  // y
            break;
          case 25:
            SetTextureCoordinates(63, 32, 96, 127);  // z
            break;
          case 26:
            SetTextureCoordinates(95, 64, 96, 127);  // 1
            break;
          case 27:
            SetTextureCoordinates(127, 96, 96, 127);  // 2
            break;
          case 28:
            SetTextureCoordinates(159, 128, 96, 127);  // 3
            break;
          case 29:
            SetTextureCoordinates(191, 160, 96, 127);  // 4
            break;
          case 30:
            SetTextureCoordinates(223, 192, 96, 127);  // 5
            break;
          case 31:
            SetTextureCoordinates(255, 224, 96, 127);  // 6
            break;
          case 32:
            SetTextureCoordinates(31, 0, 128, 159);  // 7
            break;
          case 33:
            SetTextureCoordinates(63, 32, 128, 159);  // 8
            break;
          case 34:
            SetTextureCoordinates(95, 64, 128, 159);  // 9
            break;
          case 35:
            SetTextureCoordinates(127, 96, 128, 159);  // 0
            break;
          default:
            SetTextureCoordinates(159, 128, 128, 159);  // Bottom right corner as default
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
