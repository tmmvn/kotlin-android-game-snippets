package dragon.craneam.com;

import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.Log;

public class GLRenderer
  implements Renderer
  {
    private static final String TAG = "Dragon Squad";  // Game tag in logs
    private static final boolean DEBUG = true;  // Sets debug mode on or off
    private Buffers terrainFloorBuffer;
    private Buffers terrainWallBuffer;
    private Buffers friendlyUnitBuffer;
    private Buffers enemyUnitBuffer;
    private Buffers uiBuffer;
    private List<Buffers> uiTextBuffer = new ArrayList<Buffers>();
    private Context context;  // context, needed for image loading
    private final static int NUM_TEXTURES = 4;  // constant for texture amount
    private Bitmap bitmap;  // used to load textures
    private int[] textures = new int[NUM_TEXTURES];  // texture id array
    private int[] bitmaps = new int[NUM_TEXTURES];  // bitmaps id array
    private boolean renderingTerrain = false;
    private boolean renderingUnits = false;
    private boolean renderingUI = false;
    private boolean clearingTerrain = false;
    private boolean clearingUnits = false;
    private boolean clearingUI = false;

    /**
     Constructor
     @param passedContext
     Description Context to get resources.
     */
    public GLRenderer(Context passedContext)
      {
        context = passedContext;
        try
          {
            uiTextBuffer.clear();
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GLRenderer: Error clearing text item list");
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer created initialized succesfully.");
          }
      }

    /**
     Loads textures
     @param gl
     Description OpenGL reference to use.
     */
    private boolean LoadTextures(GL10 gl)
      {
        if (DEBUG)
          {
            Log.d(TAG, "Loading textures.");
          }
        bitmaps[0] = R.drawable.grass;
        bitmaps[1] = R.drawable.desert;
        bitmaps[2] = R.drawable.soldier1;
        bitmaps[3] = R.drawable.font01;
        gl.glGenTextures(NUM_TEXTURES, textures, 0);  // generate gl-texture indexes
        // loop through bitmaps, decode the bitmap and assign it to the correct gl-texture index
        for (int i = 0; i < NUM_TEXTURES; i++)
          {
            bitmap = BitmapFactory.decodeResource(context.getResources(), bitmaps[i]);
            if (bitmap == null)
              {
                if (DEBUG)
                  {
                    Log.w(TAG, "Bitmap decoding failed, aborting.");
                    return false;
                  }
              }
            // bind textures and set paramete1rs
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();  // done, recycle the texture to free memory
            if (DEBUG)
              {
                Log.d(TAG, "Bitmap loading succesfully");
              }
          }
        return true;
      }

    /**
     Called when the surface is created
     @param gl
     Description OpenGL-reference.
     @param config
     Description Config to use.
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
      {
        //gl.glEnable(GL10.GL_LIGHTING);	// enable lightning
        //gl.glEnable(GL10.GL_LIGHT0);	// enable default light
        gl.glEnable(GL10.GL_TEXTURE_2D);  // enable textures
        gl.glClearDepthf(1.0f);      // set the default depht when clearing screen
        gl.glEnable(GL10.GL_DEPTH_TEST);  // enable depth testing
        gl.glDepthFunc(GL10.GL_LEQUAL);  // depth function to use
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // some nice perspective correction
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Surface created.");
          }
      }

    /**
     Called when the surface is changed
     @param gl
     Description OpenGL-reference
     @param width
     Description Width of the screen.
     @param height
     Description Height of the screen.
     */
    public void onSurfaceChanged(GL10 gl, int width, int height)
      {
        if (LoadTextures(gl))  // load textures
          {
            if (DEBUG)
              {
                Log.d(TAG, "Textures loaded succesfully.");
              }
          }
        else
          {
            if (DEBUG)
              {
                Log.w(TAG, "Texture loading failed.");
              }
          }
	    
	    /*if(CreateLight(gl))	// create the default light
	    {	
	    	if(DEBUG)
	    		Log.d(TAG, "Lights created succesfully.");
	    }	
	    else
	    	if(DEBUG)
	    		Log.w(TAG, "Light creation failed.");*/
        gl.glViewport(0, 0, width, height);  // set viewport again
        gl.glMatrixMode(GL10.GL_PROJECTION);  // set matrix mode
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  // enable usage of vertex arrays
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // enable usage of texture coordinates
        gl.glLoadIdentity();  // load default matrix
        gl.glOrthof(0, 800, 480, 0, -1, 1); // orthogonal perspective works better for 2D
        gl.glMatrixMode(GL10.GL_MODELVIEW);  // set matrix mode to model view and reset also
        gl.glLoadIdentity();
      }

    public boolean ClearTerrainBuffers()
      {
        if (!renderingTerrain)
          {
            clearingTerrain = true;
            terrainFloorBuffer = null;
            terrainWallBuffer = null;
            clearingTerrain = false;
            return true;
          }
        else
          {
            return false;
          }
      }

    public boolean ClearUnitBuffers()
      {
        if (!renderingUnits)
          {
            clearingUnits = true;
            friendlyUnitBuffer = null;
            enemyUnitBuffer = null;
            clearingUnits = false;
            return true;
          }
        else
          {
            return false;
          }
      }

    public boolean ClearUIBuffers()
      {
        if (!renderingUI)
          {
            clearingUI = true;
            uiBuffer = null;
            try
              {
                uiTextBuffer.clear();
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GLRenderer: Error clearing UI buffer: " + e);
                  }
                return false;
              }
            clearingUI = false;
            return true;
          }
        else
          {
            return false;
          }
      }

    public void SetTerrainFloorBuffer(Buffers buffer)
      {
        terrainFloorBuffer = buffer;
      }

    public void SetTerrainWallBuffer(Buffers buffer)
      {
        terrainWallBuffer = buffer;
      }

    public void SetFriendlyUnitBuffer(Buffers buffer)
      {
        friendlyUnitBuffer = buffer;
      }

    public void SetEnemyUnitBuffer(Buffers buffer)
      {
        enemyUnitBuffer = buffer;
      }

    public void SetUIBuffer(UI ui)
      {
        uiBuffer = ui.GetGraphicsBuffer();
        for (int i = 0; i < ui.textFields.size(); i++)
          {
            try
              {
                uiTextBuffer.add(ui.textFields.get(i)
                  .GetBuffer());
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GLRenderer: Error adding text element: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GLRenderer: Error adding text element: " + e);
                  }
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GLRenderer: Error adding text element: " + e);
                  }
              }
          }
      }

    /**
     Draw's the scene
     @param gl
     Description OpenGL-reference.
     */
    public void onDrawFrame(GL10 gl)
      {
        gl.glClearColor(0f, 0f, 0f, 1.0f);  // set clear color to black
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  // clear buffers
        gl.glFrontFace(GL10.GL_CCW);  // set culling direction
        gl.glEnable(GL10.GL_CULL_FACE);  // enable back face culling
        gl.glCullFace(GL10.GL_FRONT);
        gl.glEnable(GL10.GL_TEXTURE_2D);  // enable textures
        gl.glEnable(GL10.GL_BLEND);  // enable blending
        gl.glBlendFunc(GL10.GL_ONE,
          GL10.GL_ONE_MINUS_SRC_ALPHA);  // let's use additive blending since it's nice for this type of game
        renderingTerrain = true;
        if (terrainFloorBuffer != null && !clearingTerrain)
          {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[terrainFloorBuffer.GetTextureId()]); // select correct texture
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, terrainFloorBuffer.GetVertexBuffer());  // set pointer to our buffer
            gl.glColor4f(1f, 1f, 1f, 1f);  // set vertex rendering color, since we use textures not really necessary
            gl.glTranslatef(terrainFloorBuffer.GetXOffset(),
              terrainFloorBuffer.GetYOffset(),
              0);  // translate the quad to it's correct position
            gl.glRotatef(0, 0, 0, 1);
            gl.glTexCoordPointer(2,
              GL10.GL_FLOAT,
              0,
              terrainFloorBuffer.GetTextureBuffer()); // set texture pointer to texcoord buffer
            gl.glDrawElements(GL10.GL_TRIANGLES,
              terrainFloorBuffer.GetIndicesCount(),
              GL10.GL_UNSIGNED_SHORT,
              terrainFloorBuffer.GetIndexBuffer());  // draw the quad buffer
            gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
          }
        renderingTerrain = false;
        renderingUnits = true;
        if (friendlyUnitBuffer != null && !clearingUnits)
          {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[friendlyUnitBuffer.GetTextureId()]); // select correct texture
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, friendlyUnitBuffer.GetVertexBuffer());  // set pointer to our buffer
            gl.glColor4f(1f, 1f, 1f, 1f);  // set vertex rendering color, since we use textures not really necessary
            gl.glTranslatef(friendlyUnitBuffer.GetXOffset(),
              friendlyUnitBuffer.GetYOffset(),
              0);  // translate the quad to it's correct position
            gl.glRotatef(0, 0, 0, 1);
            gl.glTexCoordPointer(2,
              GL10.GL_FLOAT,
              0,
              friendlyUnitBuffer.GetTextureBuffer()); // set texture pointer to texcoord buffer
            gl.glDrawElements(GL10.GL_TRIANGLES,
              friendlyUnitBuffer.GetIndicesCount(),
              GL10.GL_UNSIGNED_SHORT,
              friendlyUnitBuffer.GetIndexBuffer());  // draw the quad buffer
            gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
          }
        if (enemyUnitBuffer != null && !clearingUnits)
          {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[enemyUnitBuffer.GetTextureId()]); // select correct texture
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, enemyUnitBuffer.GetVertexBuffer());  // set pointer to our buffer
            gl.glColor4f(1f, 1f, 1f, 1f);  // set vertex rendering color, since we use textures not really necessary
            gl.glTranslatef(enemyUnitBuffer.GetXOffset(),
              enemyUnitBuffer.GetYOffset(),
              0);  // translate the quad to it's correct position
            gl.glRotatef(0, 0, 0, 1);
            gl.glTexCoordPointer(2,
              GL10.GL_FLOAT,
              0,
              enemyUnitBuffer.GetTextureBuffer()); // set texture pointer to texcoord buffer
            gl.glDrawElements(GL10.GL_TRIANGLES,
              enemyUnitBuffer.GetIndicesCount(),
              GL10.GL_UNSIGNED_SHORT,
              enemyUnitBuffer.GetIndexBuffer());  // draw the quad buffer
            gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
          }
        renderingUnits = false;
        renderingTerrain = true;
        if (terrainWallBuffer != null && !clearingTerrain)
          {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[terrainWallBuffer.GetTextureId()]); // select correct texture
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, terrainWallBuffer.GetVertexBuffer());  // set pointer to our buffer
            gl.glColor4f(1f, 1f, 1f, 1f);  // set vertex rendering color, since we use textures not really necessary
            gl.glTranslatef(terrainFloorBuffer.GetXOffset(),
              terrainWallBuffer.GetYOffset(),
              0);  // translate the quad to it's correct position
            gl.glRotatef(0, 0, 0, 1);
            gl.glTexCoordPointer(2,
              GL10.GL_FLOAT,
              0,
              terrainWallBuffer.GetTextureBuffer()); // set texture pointer to texcoord buffer
            gl.glDrawElements(GL10.GL_TRIANGLES,
              terrainWallBuffer.GetIndicesCount(),
              GL10.GL_UNSIGNED_SHORT,
              terrainWallBuffer.GetIndexBuffer());  // draw the quad buffer
            gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
          }
        renderingTerrain = false;
        renderingUI = true;
        // TODO: ADD UI RENDERING HERE
        if (!uiTextBuffer.isEmpty() && !clearingUI)
          {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[3]); // select correct texture
            for (int i = 0; i < uiTextBuffer.size(); i++)
              {
                gl.glVertexPointer(3,
                  GL10.GL_FLOAT,
                  0,
                  uiTextBuffer.get(i)
                    .GetVertexBuffer());  // set pointer to our buffer
                gl.glColor4f(1f, 1f, 1f, 1f);  // set vertex rendering color, since we use textures not really necessary
                gl.glTranslatef(uiTextBuffer.get(i)
                    .GetXOffset(),
                  uiTextBuffer.get(i)
                    .GetYOffset(),
                  0);  // translate the quad to it's correct position
                gl.glRotatef(0, 0, 0, 1);
                gl.glTexCoordPointer(2,
                  GL10.GL_FLOAT,
                  0,
                  uiTextBuffer.get(i)
                    .GetTextureBuffer()); // set texture pointer to texcoord buffer
                gl.glDrawElements(GL10.GL_TRIANGLES,
                  uiTextBuffer.get(i)
                    .GetIndicesCount(),
                  GL10.GL_UNSIGNED_SHORT,
                  uiTextBuffer.get(i)
                    .GetIndexBuffer());  // draw the quad buffer
                gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
              }
          }
        renderingUI = false;
        //gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);	// disable stuff we enabled earlier
        //gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);  // continue disabling stuff
        gl.glDisable(GL10.GL_CULL_FACE);
      }
  }