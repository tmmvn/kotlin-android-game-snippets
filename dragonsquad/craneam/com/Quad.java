package dragon.craneam.com;

import android.util.Log;

public class Quad
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = false;  // if debug mode is enabled for this class or not
    protected float[] vertices = new float[12];  // vertices of the quad
    private short[] indices = { 0, 1, 2, 0, 2, 3 };  // indices draw order
    private float[] texCoords = {  // texture coordinates
      1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, };
    protected int textureWidth;  // width of the texture used
    protected int textureHeight;  // height of the texture used
    private int textureId = 0;  // texture id to be used in rendering
    private float rotation;  // rotation value
    private int listIndex;  // index of the quad in any lists

    /**
     Constructor for the quad class
     @param width
     Description Width of the quad.
     @param height
     Description Height of the quad.
     */
    public Quad(int width, int height)
      {
        vertices[0] = -width / 2f;  // Create vertex coordinates according to given dimensions
        vertices[1] = height / 2f;
        vertices[2] = 0;
        vertices[3] = -width / 2f;
        vertices[4] = -height / 2f;
        vertices[5] = 0;
        vertices[6] = width / 2f;
        vertices[7] = -height / 2f;
        vertices[8] = 0;
        vertices[9] = width / 2f;
        vertices[10] = height / 2f;
        vertices[11] = 0;
        rotation = 0f;  // set rotation to default
        if (DEBUG)
          {
            Log.d(TAG, "Quad: Created quad succesfully.");
          }
      }

    /**
     Sets texture coordinates for texture atlases and such
     @param xMin
     Description Starting x-coordinate of texture.
     @param xMax
     Description Final x-coordinate of texture.
     @param yMin
     Description Starting y-coordinate of texture
     @param yMax
     Description Ending y-coordinate of texture
     */
    public void SetTextureCoordinates(int xMin, int xMax, int yMin, int yMax)
      {
        // We divide the given coordinates with texture dimensions to get the correct values
        texCoords[4] = (float) xMin / textureWidth;
        texCoords[5] = (float) yMin / textureHeight;
        texCoords[6] = (float) xMin / textureWidth;
        texCoords[7] = (float) yMax / textureHeight;
        texCoords[0] = (float) xMax / textureWidth;
        texCoords[1] = (float) yMax / textureHeight;
        texCoords[2] = (float) xMax / textureWidth;
        texCoords[3] = (float) yMin / textureHeight;
        if (DEBUG)
          {
            Log.d(TAG, "Quad: Changed texture coords succesfully");
          }
      }

    /**
     Sets the index of the quad in any lists
     @param indexToSet
     Description Index of the item in the list.
     */
    public void SetListIndex(int indexToSet)
      {
        listIndex = indexToSet;
      }

    /**
     Returns current list index
     @return The quad's current index in the list it is in.
     */
    public int GetListIndex()
      {
        return listIndex;
      }

    /**
     Rotates the quad
     @param amount
     Description Amount to rotate in degrees.
     */
    public void Rotate(float amount)
      {
        rotation += amount;
        // make sure we stay within +-360 range
        if (rotation > 360)
          {
            rotation = 0;
          }
        if (rotation < -360)
          {
            rotation = 0;
          }
      }

    /**
     Return's the current amount of rotation
     @return The current amount of rotation in degrees
     */
    public float GetRotation()
      {
        return rotation;
      }

    /**
     Sets the current texture id of the quad
     @param id
     Description The texture id to use.
     */
    public void SetTexture(int id)
      {
        textureId = id;
        if (DEBUG)
          {
            Log.d(TAG, "Quad: texture id set to " + id);
          }
      }

    public void AddIndices(Buffers buffers)
      {
        buffers.AddToIndexBuffer(indices);
      }

    public void AddTextureCoordinates(Buffers buffers)
      {
        buffers.AddToTextureBuffer(texCoords);
      }

    public void AddVertexes(Buffers buffers)
      {
        buffers.AddToVertexBuffer(vertices);
      }

    public void SendTexture(Buffers buffers)
      {
        buffers.SetTextureId(textureId);
      }

    public float GetTextureCoordinate(int index)
      {
        return texCoords[index];
      }
  }
