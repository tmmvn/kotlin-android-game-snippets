package jeweltilt.craneam.com;

import android.util.Log;

public class Quad
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;
    private int textureId = 0;  // texture id to be used in rendering
    private Buffers buffers = new Buffers();  // buffers used for rendering
    private float[] vertices = new float[12];  // vertices of the quad
    private short[] indices = { 0, 1, 2, 0, 2, 3 };  // indices draw order
    private float[] texCoords = {  // texture coordinates
      0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f };
    private float rotation;  // rotation value
    private float radius;  // radius, used in collision tests
    private int listIndex;

    // Constructor
    public Quad(float size, float scale)
      {
        vertices[0] = scale * size * -1;  // Create vertex coordinates and scale by size
        vertices[1] = scale * size * 1;
        vertices[2] = scale * size * 0;
        vertices[3] = scale * size * -1;
        vertices[4] = scale * size * -1;
        vertices[5] = scale * size * 0;
        vertices[6] = scale * size * 1;
        vertices[7] = scale * size * -1;
        vertices[8] = scale * size * 0;
        vertices[9] = scale * size * 1;
        vertices[10] = scale * size * 1;
        vertices[11] = scale * size * 0;
        radius = ( (float) Math.sqrt(8) * scale * size ) / 2;  // Calculate radius for bounding sphere
        rotation = 0f;
        if (DEBUG)
          {
            Log.d(TAG, "Quad of size " + scale * size + " created.");
          }
      }

    public void SetListIndex(int indexToSet)
      {
        listIndex = indexToSet;
      }

    public int GetListIndex()
      {
        return listIndex;
      }

    // Function to distort
    public void Distort(float x, float y)
      {
        for (int i = 0; i < 12; i += 3)
          {
            vertices[i] *= x;
          }
        texCoords[4] *= x;
        texCoords[6] *= x;
        for (int i = 1; i < 12; i += 3)
          {
            vertices[i] *= y;
          }
        texCoords[5] *= y;
        texCoords[3] *= y;
        radius *= ( x * y );
      }

    // Function to rotate
    public void Rotate(float amount)
      {
        rotation += amount;
        if (rotation > 360)
          {
            rotation = 0;
          }
        if (rotation < -360)
          {
            rotation = 0;
          }
      }

    //Function to return rotation amount
    public float GetRotation()
      {
        return rotation;
      }

    // Function to return radius
    public float GetRadius()
      {
        return radius;
      }

    // Function to return buffers
    public Buffers GetBuffers()
      {
        return buffers;
      }

    // Function to return texture id
    public void SetTexture(int id)
      {
        textureId = id;
        buffers.SetTextureId(textureId);
        if (DEBUG)
          {
            Log.d(TAG, "Quad texture id set to " + id);
          }
      }

    // Function to clear buffer-arrays
    public void ClearBuffer()
      {
        buffers.ClearArrays();
      }

    // Function to draw the quad to its determined buffers
    public void Draw()
      {
        buffers.AddToIndexBuffer(indices);
        buffers.AddToTextureBuffer(texCoords);
        buffers.AddToVertexBuffer(vertices);
        buffers.SetTextureId(textureId);
        if (DEBUG)
          {
            Log.d(TAG, "Quad drawn to buffers.");
          }
      }
  }
