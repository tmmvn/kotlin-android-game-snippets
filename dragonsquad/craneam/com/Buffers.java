package dragon.craneam.com;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.ShortBuffer;
import android.util.Log;

public class Buffers
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = false;
    private ByteBuffer vbb, ibb, tbb;  // Byte buffers for everything
    private FloatBuffer vertexBuffer, textureBuffer;  // Final float buffers
    private ShortBuffer indexBuffer;  // indice buffer
    private int vbCount = 0, ibCount = 0, tbCount = 0;  // to keep track of how many of everything we have
    private int textureId = 0;  // texture id
    private short[] indices;  // array for indices
    private float[] vertices;  // array for vertices
    private float[] textureCoordinates;  // array for texcoords
    private float xOffset, yOffset;

    // Constructor
    public Buffers()
      {
        vertices = new float[0];  // initilaize arrays
        indices = new short[0];
        textureCoordinates = new float[0];
        try
          {  // initialize buffers and catch any errors
            if (DEBUG)
              {
                Log.d(TAG, "Allocating vertex buffer.");
              }
            vbb = ByteBuffer.allocateDirect(indices.length * 2);
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to allocate vertex buffer: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Vertex buffer allocated.");
              }
          }
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Allocating index buffer.");
              }
            ibb = ByteBuffer.allocateDirect(vertices.length * 2);
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to allocate index buffer: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Index buffer allocated.");
              }
          }
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Allocating texture coordinates buffer.");
              }
            tbb = ByteBuffer.allocateDirect(textureCoordinates.length * 2);
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Failed to allocate texture coordinates buffer: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Vertex buffer allocated.");
              }
          }
        indexBuffer = ibb.asShortBuffer();
        vertexBuffer = vbb.asFloatBuffer();
        textureBuffer = tbb.asFloatBuffer();
      }

    public void SetOffsets(float x, float y)
      {
        xOffset = -x;
        yOffset = -y;
      }

    public float GetXOffset()
      {
        return xOffset;
      }

    public float GetYOffset()
      {
        return yOffset;
      }

    // function to set texture id, everything in a buffer is rendered with same texture
    public void SetTextureId(int id)
      {
        textureId = id;
      }

    // function to return texture id
    public int GetTextureId()
      {
        return textureId;
      }

    // function to get indices amount
    public int GetIndicesCount()
      {
        return ibCount;
      }

    // functions to return buffers
    public FloatBuffer GetVertexBuffer()
      {
        return vertexBuffer;
      }

    public ShortBuffer GetIndexBuffer()
      {
        return indexBuffer;
      }

    public FloatBuffer GetTextureBuffer()
      {
        return textureBuffer;
      }

    // functions to add arrays to buffers
    public void AddToVertexBuffer(float[] vbToAdd)
      {
        int addLength = vbToAdd.length;  // length of array to add
        if (( addLength + vbCount ) > vertices.length)  // if not enough room, increase the size of array
          {
            if (DEBUG)
              {
                Log.d(TAG, "Increased vertex buffer size.");
              }
            int newSize = vertices.length + addLength * 2;
            float[] temp = new float[newSize];
            System.arraycopy(vertices, 0, temp, 0, vertices.length);
            vertices = temp;
          }
        System.arraycopy(vbToAdd, 0, vertices, vbCount, addLength);  // add the array
        vbCount += addLength;    // increase tracker to correct amount
      }

    // Same as above
    public void AddToTextureBuffer(float[] tbToAdd)
      {
        int addLength = tbToAdd.length;
        if (( addLength + tbCount ) > textureCoordinates.length)
          {
            if (DEBUG)
              {
                Log.d(TAG, "Increased texture coordinates buffer size.");
              }
            int newSize = textureCoordinates.length + addLength * 2;
            float[] temp = new float[newSize];
            System.arraycopy(textureCoordinates, 0, temp, 0, textureCoordinates.length);
            textureCoordinates = temp;
          }
        System.arraycopy(tbToAdd, 0, textureCoordinates, tbCount, addLength);
        tbCount += addLength;
      }

    public void ChangeTextureBuffer(int index, float value)
      {
        textureCoordinates[index] = value;
        if (DEBUG)
          {
            Log.d(TAG, "Buffers: texture buffer at index " + index + " changed to " + value);
          }
      }

    // Same as above
    public void AddToIndexBuffer(short[] ibToAdd)
      {
        int addLength = ibToAdd.length;
        if (( addLength + ibCount ) > indices.length)
          {
            if (DEBUG)
              {
                Log.d(TAG, "Increased index buffer size.");
              }
            int newSize = indices.length + addLength * 2;
            short[] temp = new short[newSize];
            System.arraycopy(indices, 0, temp, 0, indices.length);
            indices = temp;
          }
        System.arraycopy(ibToAdd, 0, indices, ibCount, addLength);
        for (int i = ibCount; i < indices.length; i++)
          {
            indices[i] += ibCount * 2 / 3;
          }
        ibCount += addLength;
      }

    // function to clear arrays
    public void ClearArrays()
      {
        float[] tempFloat1 = new float[0];
        float[] tempFloat2 = new float[0];
        short[] tempShort = new short[0];
        vertices = tempFloat1;
        indices = tempShort;
        textureCoordinates = tempFloat2;
        ibCount = 0;
        vbCount = 0;
        tbCount = 0;
        if (DEBUG)
          {
            Log.d(TAG, "Cleared arrays.");
          }
      }

    // function to clear buffers
    public void ClearBuffers(boolean i, boolean v, boolean t)
      {
        if (v)
          {
            vbb.clear();
            vertexBuffer.clear();
            try
              {
                vertexBuffer.compact();
              }
            catch (ReadOnlyBufferException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Vertex buffer compact failed: " + e);
                  }
              }
          }
        if (i)
          {
            ibb.clear();
            indexBuffer.clear();
            try
              {
                indexBuffer.compact();  // apparently required due to some odd short/int buffer bug
              }
            catch (ReadOnlyBufferException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Index buffer compact failed: " + e);
                  }
              }
          }
        if (t)
          {
            tbb.clear();
            textureBuffer.clear();
            try
              {
                textureBuffer.compact();
              }
            catch (ReadOnlyBufferException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "Texture coordinates buffer compact failed: " + e);
                  }
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "Cleared buffers.");
          }
      }

    // Creates all the buffers
    public void CreateBuffers(boolean i, boolean v, boolean t)
      {
        ClearBuffers(i, v, t);
        if (i)
          {
            CreateIndexBuffer();
          }
        if (v)
          {
            CreateVertexBuffer();
          }
        if (t)
          {
            CreateTextureBuffer();
          }
      }

    // Creates index buffer by allocating enough space to accomodate the array and then add the array in to the final
    // buffer
    private void CreateIndexBuffer()
      {
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Allocating index buffer.");
              }
            ibb = ByteBuffer.allocateDirect(indices.length * 2);
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Index buffer allocation failed: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Index buffer allocation succesful.");
              }
          }
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Adding array to buffer (index).");
              }
            indexBuffer.put(indices);
          }
        catch (BufferOverflowException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Index buffer overflow: " + e);
              }
          }
        catch (ReadOnlyBufferException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Index buffer put failed: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Array to buffer (index) succesful.");
              }
          }
        indexBuffer.position(0);
      }

    // same as above
    private void CreateVertexBuffer()
      {
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Allocating vertex buffer.");
              }
            vbb = ByteBuffer.allocateDirect(vertices.length * 4);
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Vertex buffer allocation failed: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Vertex buffer allocation succesful.");
              }
          }
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Adding array to buffer (vertex).");
              }
            vertexBuffer.put(vertices);
          }
        catch (BufferOverflowException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Vertex buffer overflow: " + e);
              }
          }
        catch (ReadOnlyBufferException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Vertex buffer put failed: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Array to buffer (vertex) succesful.");
              }
          }
        vertexBuffer.position(0);
      }

    // same as above
    private void CreateTextureBuffer()
      {
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Allocating texture coordinates buffer.");
              }
            tbb = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Texture coordinates buffer allocation failed: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Texture coordinates buffer allocation succesful.");
              }
          }
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer = tbb.asFloatBuffer();
        try
          {
            if (DEBUG)
              {
                Log.d(TAG, "Adding array to buffer (texture coordinates).");
              }
            textureBuffer.put(textureCoordinates);
          }
        catch (BufferOverflowException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Texture coordinates buffer overflow: " + e);
              }
          }
        catch (ReadOnlyBufferException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Texture coordinates buffer put failed: " + e);
              }
          }
        finally
          {
            if (DEBUG)
              {
                Log.d(TAG, "Array to buffer (texture coordinates) succesful.");
              }
          }
        textureBuffer.position(0);
      }
  }
