package dragon.craneam.com;

public class Text
  {
    String text;
    Buffers buffer;
    int x;
    int y;
    int fontId;

    public Text(int xPos, int yPos, int fontToUse)
      {
        buffer = new Buffers();
        x = xPos;
        y = yPos;
        fontId = fontToUse;
      }

    public void SetXPosition(int value)
      {
        x = value;
      }

    public void SetYPosition(int value)
      {
        y = value;
      }

    public Buffers GetBuffer()
      {
        return buffer;
      }

    public void SetText(String contents)
      {
        text = contents;
      }

    public void Render()
      {
        Font font;
        if (fontId == 1)
          {
            font = new Font(24);
          }
        else
          {
            font = new Font(15);
          }
        for (int i = 0; i < text.length(); i++)
          {
            switch (text.charAt(i))
              {
              case 'a':
                font.SetTextureLocation(0);
                break;
              case 'A':
                font.SetTextureLocation(0);
                break;
              case 'b':
                font.SetTextureLocation(1);
                break;
              case 'B':
                font.SetTextureLocation(1);
                break;
              case 'c':
                font.SetTextureLocation(2);
                break;
              case 'C':
                font.SetTextureLocation(2);
                break;
              case 'd':
                font.SetTextureLocation(3);
                break;
              case 'D':
                font.SetTextureLocation(3);
                break;
              case 'e':
                font.SetTextureLocation(4);
                break;
              case 'E':
                font.SetTextureLocation(4);
                break;
              case 'f':
                font.SetTextureLocation(5);
                break;
              case 'F':
                font.SetTextureLocation(5);
                break;
              case 'g':
                font.SetTextureLocation(6);
                break;
              case 'G':
                font.SetTextureLocation(6);
                break;
              case 'h':
                font.SetTextureLocation(7);
                break;
              case 'H':
                font.SetTextureLocation(7);
                break;
              case 'i':
                font.SetTextureLocation(8);
                break;
              case 'I':
                font.SetTextureLocation(8);
                break;
              case 'j':
                font.SetTextureLocation(9);
                break;
              case 'J':
                font.SetTextureLocation(9);
                break;
              case 'k':
                font.SetTextureLocation(10);
                break;
              case 'K':
                font.SetTextureLocation(10);
                break;
              case 'l':
                font.SetTextureLocation(11);
                break;
              case 'L':
                font.SetTextureLocation(11);
                break;
              case 'm':
                font.SetTextureLocation(12);
                break;
              case 'M':
                font.SetTextureLocation(12);
                break;
              case 'n':
                font.SetTextureLocation(13);
                break;
              case 'N':
                font.SetTextureLocation(13);
                break;
              case 'o':
                font.SetTextureLocation(14);
                break;
              case 'O':
                font.SetTextureLocation(14);
                break;
              case 'p':
                font.SetTextureLocation(15);
                break;
              case 'P':
                font.SetTextureLocation(15);
                break;
              case 'q':
                font.SetTextureLocation(16);
                break;
              case 'Q':
                font.SetTextureLocation(16);
                break;
              case 'r':
                font.SetTextureLocation(17);
                break;
              case 'R':
                font.SetTextureLocation(17);
                break;
              case 's':
                font.SetTextureLocation(18);
                break;
              case 'S':
                font.SetTextureLocation(18);
                break;
              case 't':
                font.SetTextureLocation(19);
                break;
              case 'T':
                font.SetTextureLocation(19);
                break;
              case 'u':
                font.SetTextureLocation(20);
                break;
              case 'U':
                font.SetTextureLocation(20);
                break;
              case 'v':
                font.SetTextureLocation(21);
                break;
              case 'V':
                font.SetTextureLocation(21);
                break;
              case 'w':
                font.SetTextureLocation(22);
                break;
              case 'W':
                font.SetTextureLocation(22);
                break;
              case 'x':
                font.SetTextureLocation(23);
                break;
              case 'X':
                font.SetTextureLocation(23);
                break;
              case 'y':
                font.SetTextureLocation(24);
                break;
              case 'Y':
                font.SetTextureLocation(24);
                break;
              case 'z':
                font.SetTextureLocation(25);
                break;
              case 'Z':
                font.SetTextureLocation(25);
                break;
              case '1':
                font.SetTextureLocation(26);
                break;
              case '2':
                font.SetTextureLocation(27);
                break;
              case '3':
                font.SetTextureLocation(28);
                break;
              case '4':
                font.SetTextureLocation(29);
                break;
              case '5':
                font.SetTextureLocation(30);
                break;
              case '6':
                font.SetTextureLocation(31);
                break;
              case '7':
                font.SetTextureLocation(32);
                break;
              case '8':
                font.SetTextureLocation(33);
                break;
              case '9':
                font.SetTextureLocation(34);
                break;
              case '0':
                font.SetTextureLocation(35);
                break;
              }
            if (i == 0)
              {
                font.Offset(x, y);
              }
            else
              {
                font.Offset(x - 34, 0);
              }
            font.AddIndices(buffer);
            font.AddVertexes(buffer);
            font.AddTextureCoordinates(buffer);
          }
        buffer.SetTextureId(3);
        buffer.CreateBuffers(true, true, true);
      }
  }
