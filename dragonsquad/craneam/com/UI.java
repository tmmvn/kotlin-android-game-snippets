package dragon.craneam.com;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class UI
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = true;    // Debug flag
    private Buffers graphicsBuffer;
    public List<Text> textFields = new ArrayList<Text>();

    public UI()
      {
        graphicsBuffer = new Buffers();
        try
          {
            textFields.clear();
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "UI: Error clearing text fields");
              }
          }
      }

    public void AddGraphicElement(UIElement element)
      {
        element.AddIndices(graphicsBuffer);
        element.AddVertexes(graphicsBuffer);
        element.AddTextureCoordinates(graphicsBuffer);
      }

    public void AddTextElement(Text element)
      {
        try
          {
            textFields.add(element);
            if (DEBUG)
              {
                Log.d(TAG, "UI: Added text field succesfully.");
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "UI: Error adding text element: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "UI: Error adding text element: " + e);
              }
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "UI: Error adding text element: " + e);
              }
          }
      }

    public Buffers GetGraphicsBuffer()
      {
        return graphicsBuffer;
      }
  }
