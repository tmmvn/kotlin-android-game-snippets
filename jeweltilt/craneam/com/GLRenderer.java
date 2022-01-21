package jeweltilt.craneam.com;

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
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;  // Sets debug mode on or off
    private List<PlayerBall> playerRenderList = new ArrayList<PlayerBall>();
    private List<Jewel> jewelRenderList = new ArrayList<Jewel>();
    private List<UIElement> uiRenderList = new ArrayList<UIElement>();
    private List<Sparkle> sparkleRenderList = new ArrayList<Sparkle>();
    private Context context;  // context, needed for image loading
    private final static int NUM_TEXTURES = 46;  // constant for texture amount
    private Bitmap bitmap;  // used to load textures
    private int[] textures = new int[NUM_TEXTURES];  // texture id array
    private int[] bitmaps = new int[NUM_TEXTURES];  // bitmaps id array
    private boolean renderingPlayer = false;
    private boolean renderingJewels = false;
    private boolean renderingUI = false;
    private boolean renderingSparkle = false;
    private boolean syncPlayer = false;
    private boolean syncJewels = false;
    private boolean syncUI = false;
    private boolean syncSparkle = false;
    private boolean clearPlayer = false;
    private boolean clearJewels = false;
    private boolean clearUI = false;
    private boolean clearSparkle = false;
    private float red, green, blue;

    // constructor, just sets the context for bitmap loading
    public GLRenderer(Context passedContext)
      {
        context = passedContext;
        red = 0;
        green = 0;
        blue = 0;
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer created initialized succesfully.");
          }
      }

    // function to load textures
    private boolean LoadTextures(GL10 gl)
      {
        if (DEBUG)
          {
            Log.d(TAG, "Loading textures.");
          }
        bitmaps[0] = R.drawable.player01;  // set id's from resources
        bitmaps[1] = R.drawable.player02;
        bitmaps[2] = R.drawable.diamond;
        bitmaps[3] = R.drawable.stone01;
        bitmaps[4] = R.drawable.stone02;
        bitmaps[5] = R.drawable.score;
        bitmaps[6] = R.drawable.lives;
        bitmaps[7] = R.drawable.zero;
        bitmaps[8] = R.drawable.one;
        bitmaps[9] = R.drawable.two;
        bitmaps[10] = R.drawable.three;
        bitmaps[11] = R.drawable.four;
        bitmaps[12] = R.drawable.five;
        bitmaps[13] = R.drawable.six;
        bitmaps[14] = R.drawable.seven;
        bitmaps[15] = R.drawable.eight;
        bitmaps[16] = R.drawable.nine;
        bitmaps[17] = R.drawable.menubg;
        bitmaps[18] = R.drawable.play;
        bitmaps[19] = R.drawable.twinkle;
        bitmaps[20] = R.drawable.high;
        bitmaps[21] = R.drawable.gameover;
        bitmaps[22] = R.drawable.pad1;
        bitmaps[23] = R.drawable.pad2;
        bitmaps[24] = R.drawable.play01;
        bitmaps[25] = R.drawable.play02;
        bitmaps[26] = R.drawable.gz;
        bitmaps[27] = R.drawable.newhs;
        bitmaps[28] = R.drawable.tap;
        bitmaps[29] = R.drawable.barbg;
        bitmaps[30] = R.drawable.barfill;
        bitmaps[31] = R.drawable.play03;
        bitmaps[32] = R.drawable.chadone;
        bitmaps[33] = R.drawable.helpc;
        bitmaps[34] = R.drawable.helptag;
        bitmaps[35] = R.drawable.help1;
        bitmaps[36] = R.drawable.help2;
        bitmaps[37] = R.drawable.help3;
        bitmaps[38] = R.drawable.help4;
        bitmaps[39] = R.drawable.help5;
        bitmaps[40] = R.drawable.help6;
        bitmaps[41] = R.drawable.help7;
        bitmaps[42] = R.drawable.sfx;
        bitmaps[43] = R.drawable.sfxno;
        bitmaps[44] = R.drawable.music;
        bitmaps[45] = R.drawable.musicno;
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
            // bind textures and set parameters
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();  // done, recycle the texture to free memory
          }
        return true;
      }

    // Called when surface is first created
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
	    
	/*private boolean CreateLight(GL10 gl)
	{
		// arrays for light colors, position and such
	   	float lightAmbient[] = { 0.2f, 0.0f, 0.0f, 1.0f };
	   	float lightDiffuse[] = { 0.5f, 0.0f, 0.0f, 1.0f };
	   	float lightSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	   	float matAmbient[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	   	float matDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	   	float matSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	   	float lightPosition[] = { 0.0f, 0.0f, 3.0f, 0.0f };
	   	float lightDirection[] = { 0.0f, 0.0f, -5.0f };
	   	
	   	// buffers for lights and such
	   	FloatBuffer ambientBuffer, diffuseBuffer, specularBuffer;
	   	FloatBuffer ambientLBuffer, diffuseLBuffer, specularLBuffer;
	   	FloatBuffer lightPos, lightDir;
	   	ByteBuffer temp;
	   	
	   	// basically loop through each light by using the temp byte buffer
	   	// copy the array to buffer and set the gl-material
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	ambientBuffer = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		ambientBuffer.put(matAmbient);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}	   	
	   	ambientBuffer.position(0);
	   	gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, ambientBuffer);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	diffuseBuffer = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		diffuseBuffer.put(matDiffuse);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	diffuseBuffer.position(0);
	   	gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, diffuseBuffer);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	specularBuffer = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		specularBuffer.put(matSpecular);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	specularBuffer.position(0);
	   	gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specularBuffer);
	   	gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 20.0f);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	ambientLBuffer = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		ambientLBuffer.put(lightAmbient);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	ambientLBuffer.position(0);
	   	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLBuffer);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	diffuseLBuffer = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		diffuseLBuffer.put(lightDiffuse);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	diffuseLBuffer.position(0);
	   	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseLBuffer);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	specularLBuffer = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		specularLBuffer.put(lightSpecular);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	specularLBuffer.position(0);
	   	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, specularLBuffer);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(16);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	lightPos = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		lightPos.put(lightPosition);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	lightPos.position(0);
	   	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos);
	   	
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Allocating buffer (lights).");
	   		temp = ByteBuffer.allocateDirect(12);
	   	}
	   	catch (IllegalArgumentException e) {
	   		temp = null;
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating buffer (lights): " + e);
	   		return false;
	   	}
	   	temp.order(ByteOrder.nativeOrder());
	   	lightDir = temp.asFloatBuffer();
	   	try {
	   		if(DEBUG)
	   			Log.d(TAG, "Copying array to buffer (lights).");
	   		lightDir.put(lightDirection);
	   	}
	   	catch (BufferOverflowException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Buffer overflow (lights): " + e);
	   		return false;
	   	}
	   	catch (ReadOnlyBufferException e) {
	   		if(DEBUG)
	   			Log.e(TAG, "Error allocating lights: " + e);
	   		return false;
	   	}
	   	lightDir.position(0);
	   	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDir);
	   	gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 1.2f);
	   	gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_EXPONENT, 20.0f);
	   	return true;
	}*/

    public void ClearPlayerList()
      {
        boolean clearing = true;
        clearPlayer = true;
        while (clearing)
          {
            if (renderingPlayer == false && syncPlayer == false)
              {
                try
                  {
                    playerRenderList.clear();
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing player list: " + e);
                      }
                  }
                clearing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Player list cleared succesfully.");
          }
        clearPlayer = false;
      }

    public void ClearSparkleList()
      {
        boolean clearing = true;
        clearSparkle = true;
        while (clearing)
          {
            if (renderingSparkle == false && syncSparkle == false)
              {
                try
                  {
                    sparkleRenderList.clear();
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing sparkle list: " + e);
                      }
                  }
                clearing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Sparkle list cleared succesfully.");
          }
        clearSparkle = false;
      }

    public void ClearJewelList()
      {
        boolean clearing = true;
        clearJewels = true;
        while (clearing)
          {
            if (renderingJewels == false && syncJewels == false)
              {
                try
                  {
                    jewelRenderList.clear();
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing jewel list: " + e);
                      }
                  }
                clearing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Jewel list cleared succesfully.");
          }
        clearJewels = false;
      }

    public void ClearUIList()
      {
        boolean clearing = true;
        clearUI = true;
        while (clearing)
          {
            if (renderingUI == false && syncUI == false)
              {
                try
                  {
                    uiRenderList.clear();
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing UI list: " + e);
                      }
                  }
                clearing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: UI list cleared succesfully.");
          }
        clearUI = false;
      }

    public void SynchronizePlayer(List<PlayerBall> listToCopy)
      {
        boolean syncing = true;
        syncPlayer = true;
        while (syncing)
          {
            if (!renderingPlayer && !clearPlayer)
              {
                try
                  {
                    playerRenderList.clear();
                    if (DEBUG)
                      {
                        Log.d(TAG, "GLRenderer: Sychronized player list cleared.");
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing player list: " + e);
                      }
                  }
                for (int i = 0; i < listToCopy.size(); i++)
                  {
                    try
                      {
                        playerRenderList.add(null);
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Null element added succesfully.");
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to player list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to player list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to player list: " + e);
                          }
                      }
                    try
                      {
                        playerRenderList.set(i, listToCopy.get(i));
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Sychronized player list copied succesfully.");
                          }
                      }
                    catch (IndexOutOfBoundsException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to player list: " + e);
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to player list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to player list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to player list: " + e);
                          }
                      }
                  }
                syncing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Sychronized player list succesfully.");
          }
        syncPlayer = false;
      }

    public void SynchronizeSparkle(List<Sparkle> listToCopy)
      {
        boolean syncing = true;
        syncSparkle = true;
        while (syncing)
          {
            if (!renderingSparkle && !clearSparkle)
              {
                try
                  {
                    sparkleRenderList.clear();
                    if (DEBUG)
                      {
                        Log.d(TAG, "GLRenderer: Sychronized sparkle list cleared.");
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing sparkle list: " + e);
                      }
                  }
                for (int i = 0; i < listToCopy.size(); i++)
                  {
                    try
                      {
                        sparkleRenderList.add(null);
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Null element added succesfully.");
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to sparkle list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to sparkle list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to sparkle list: " + e);
                          }
                      }
                    try
                      {
                        sparkleRenderList.set(i, listToCopy.get(i));
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Sychronized sparkle list copied succesfully.");
                          }
                      }
                    catch (IndexOutOfBoundsException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to sparkle list: " + e);
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to sparkle list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to sparkle list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to sparkle list: " + e);
                          }
                      }
                  }
                syncing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Sychronized sparkle list succesfully.");
          }
        syncSparkle = false;
      }

    public void SynchronizeJewels(List<Jewel> listToCopy)
      {
        boolean syncing = true;
        syncJewels = true;
        while (syncing)
          {
            if (!renderingJewels && !clearJewels)
              {
                try
                  {
                    jewelRenderList.clear();
                    if (DEBUG)
                      {
                        Log.d(TAG, "GLRenderer: Sychronized jewel list cleared.");
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing jewel list: " + e);
                      }
                  }
                for (int i = 0; i < listToCopy.size(); i++)
                  {
                    try
                      {
                        jewelRenderList.add(null);
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Null element added succesfully.");
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to jewel list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to jewel list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to jewel list: " + e);
                          }
                      }
                    try
                      {
                        jewelRenderList.set(i, listToCopy.get(i));
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Sychronized jewel copied succesfully.");
                          }
                      }
                    catch (IndexOutOfBoundsException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to jewel list: " + e);
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to jewel list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to jewel list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to jewel list: " + e);
                          }
                      }
                  }
                syncing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Sychronized Jewels list succesfully.");
          }
        syncJewels = false;
      }

    public void SynchronizeUI(List<UIElement> listToCopy)
      {
        boolean syncing = true;
        syncUI = true;
        while (syncing)
          {
            if (!renderingUI && !clearUI)
              {
                try
                  {
                    uiRenderList.clear();
                    if (DEBUG)
                      {
                        Log.d(TAG, "GLRenderer: Sychronized UI list cleared.");
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GLRenderer: Error clearing UI list: " + e);
                      }
                  }
                for (int i = 0; i < listToCopy.size(); i++)
                  {
                    try
                      {
                        uiRenderList.add(null);
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Null element added to UI list.");
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to UI list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to UI list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error adding null elements to UI list: " + e);
                          }
                      }
                    try
                      {
                        uiRenderList.set(i, listToCopy.get(i));
                        if (DEBUG)
                          {
                            Log.d(TAG, "GLRenderer: Sychronized UI item copied succesfully.");
                          }
                      }
                    catch (IndexOutOfBoundsException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to UI list: " + e);
                          }
                      }
                    catch (ClassCastException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to UI list: " + e);
                          }
                      }
                    catch (UnsupportedOperationException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to UI list: " + e);
                          }
                      }
                    catch (IllegalArgumentException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GLRenderer: Error copying data to UI list: " + e);
                          }
                      }
                  }
                syncing = false;
              }
          }
        if (DEBUG)
          {
            Log.d(TAG, "GLRenderer: Sychronized UI Element list succesfully.");
          }
        syncUI = false;
      }

    // Function to be called when surface is changed
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
        gl.glLoadIdentity();  // load default matrix
        gl.glOrthof(-width / 2f,
          width / 2f,
          -height / 2f,
          height / 2f,
          -1,
          1); // orthogonal perspective works better for 2D
        gl.glMatrixMode(GL10.GL_MODELVIEW);  // set matrix mode to model view and reset also
        gl.glLoadIdentity();
      }

    public void Flash(float redAmount, float greenAmount, float blueAmount)
      {
        red += redAmount;
        if (red > 1)
          {
            red = 1;
          }
        if (red < 0)
          {
            red = 0;
          }
        green += greenAmount;
        if (green > 1)
          {
            green = 1;
          }
        if (green < 0)
          {
            green = 0;
          }
        blue += blueAmount;
        if (blue > 1)
          {
            blue = 1;
          }
        if (blue < 0)
          {
            blue = 0;
          }
      }

    // The draw function
    public void onDrawFrame(GL10 gl)
      {
        gl.glClearColor(red, green, blue, 1.0f);  // set clear color to black
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  // clear buffers
        gl.glFrontFace(GL10.GL_CCW);  // set culling direction
        gl.glEnable(GL10.GL_CULL_FACE);  // enable back face culling
        gl.glCullFace(GL10.GL_BACK);
        gl.glEnable(GL10.GL_TEXTURE_2D);  // enable textures
        gl.glEnable(GL10.GL_BLEND);  // enable blending
        gl.glBlendFunc(GL10.GL_ONE,
          GL10.GL_ONE_MINUS_SRC_ALPHA);  // let's use additive blending since it's nice for this type of game
        gl.glNormal3f(0.0f, 0.0f, 1.0f);  // just a default normal that lights use, basically useless atm
        if (!syncPlayer && !clearPlayer && playerRenderList.size() > 0) // if data is not modified, render
          {
            renderingPlayer = true;  // set flag for rendering
            for (PlayerBall i: playerRenderList)
              {
                if (i != null)
                  {
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  // enable usage of vertex arrays
                    gl.glVertexPointer(3,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetVertexBuffer());  // set pointer to our buffer
                    gl.glColor4f(1f,
                      1f,
                      1f,
                      1f);  // set vertex rendering color, since we use textures not really necessary
                    gl.glTranslatef(i.GetXCoordinate(),
                      i.GetYCoordinate(),
                      i.GetZCoordinate());  // translate the quad to it's correct position
                    gl.glRotatef(i.GetRotation(), 0, 0, 1);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D,
                      textures[i.GetBuffers()
                        .GetTextureId()]); // select correct texture
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // enable usage of texture coordinates
                    gl.glTexCoordPointer(2,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetTextureBuffer()); // set texture pointer to texcoord buffer
                    gl.glDrawElements(GL10.GL_TRIANGLES,
                      i.GetBuffers()
                        .GetIndicesCount(),
                      GL10.GL_UNSIGNED_SHORT,
                      i.GetBuffers()
                        .GetIndexBuffer());  // draw the quad buffer
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // disable stuff we enabled earlier
                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
                  }
              }
            renderingPlayer = false;
          }
        if (!syncSparkle && !clearSparkle && sparkleRenderList.size() > 0) // if data is not modified, render
          {
            renderingSparkle = true;  // set flag for rendering
            for (Sparkle i: sparkleRenderList)
              {
                if (i != null)
                  {
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  // enable usage of vertex arrays
                    gl.glVertexPointer(3,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetVertexBuffer());  // set pointer to our buffer
                    gl.glColor4f(1f,
                      1f,
                      1f,
                      1f);  // set vertex rendering color, since we use textures not really necessary
                    gl.glTranslatef(i.GetXCoordinate(),
                      i.GetYCoordinate(),
                      i.GetZCoordinate());  // translate the quad to it's correct position
                    gl.glRotatef(i.GetRotation(), 0, 0, 1);
                    gl.glScalef(i.GetScale(), i.GetScale(), 1);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D,
                      textures[i.GetBuffers()
                        .GetTextureId()]); // select correct texture
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // enable usage of texture coordinates
                    gl.glTexCoordPointer(2,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetTextureBuffer()); // set texture pointer to texcoord buffer
                    gl.glDrawElements(GL10.GL_TRIANGLES,
                      i.GetBuffers()
                        .GetIndicesCount(),
                      GL10.GL_UNSIGNED_SHORT,
                      i.GetBuffers()
                        .GetIndexBuffer());  // draw the quad buffer
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // disable stuff we enabled earlier
                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
                  }
              }
            renderingSparkle = false;
          }
        if (!syncJewels && !clearJewels && jewelRenderList.size() > 0) // if data is not modified, render
          {
            renderingJewels = true;  // set flag for rendering
            for (Jewel i: jewelRenderList)
              {
                if (i != null)
                  {
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  // enable usage of vertex arrays
                    gl.glVertexPointer(3,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetVertexBuffer());  // set pointer to our buffer
                    gl.glColor4f(1f,
                      1f,
                      1f,
                      1f);  // set vertex rendering color, since we use textures not really necessary
                    gl.glTranslatef(i.GetXCoordinate(),
                      i.GetYCoordinate(),
                      i.GetZCoordinate());  // translate the quad to it's correct position
                    gl.glRotatef(i.GetRotation(), 0, 0, 1.0f);
                    gl.glBindTexture(GL10.GL_TEXTURE_2D,
                      textures[i.GetBuffers()
                        .GetTextureId()]); // select correct texture
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // enable usage of texture coordinates
                    gl.glTexCoordPointer(2,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetTextureBuffer()); // set texture pointer to texcoord buffer
                    gl.glDrawElements(GL10.GL_TRIANGLES,
                      i.GetBuffers()
                        .GetIndicesCount(),
                      GL10.GL_UNSIGNED_SHORT,
                      i.GetBuffers()
                        .GetIndexBuffer());  // draw the quad buffer
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // disable stuff we enabled earlier
                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
                  }
              }
            renderingJewels = false;
          }
        if (!syncUI && !clearUI && uiRenderList.size() > 0) // if data is not modified, render
          {
            renderingUI = true;  // set flag for rendering
            for (UIElement i: uiRenderList)
              {
                if (i != null)
                  {
                    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);  // enable usage of vertex arrays
                    gl.glVertexPointer(3,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetVertexBuffer());  // set pointer to our buffer
                    gl.glColor4f(1f,
                      1f,
                      1f,
                      1f);  // set vertex rendering color, since we use textures not really necessary
                    gl.glTranslatef(i.GetXCoordinate(),
                      i.GetYCoordinate(),
                      i.GetZCoordinate());  // translate the quad to it's correct position
                    gl.glBindTexture(GL10.GL_TEXTURE_2D,
                      textures[i.GetBuffers()
                        .GetTextureId()]); // select correct texture
                    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // enable usage of texture coordinates
                    gl.glTexCoordPointer(2,
                      GL10.GL_FLOAT,
                      0,
                      i.GetBuffers()
                        .GetTextureBuffer()); // set texture pointer to texcoord buffer
                    gl.glDrawElements(GL10.GL_TRIANGLES,
                      i.GetBuffers()
                        .GetIndicesCount(),
                      GL10.GL_UNSIGNED_SHORT,
                      i.GetBuffers()
                        .GetIndexBuffer());  // draw the quad buffer
                    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // disable stuff we enabled earlier
                    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                    gl.glLoadIdentity();  // to make sure everything get's translated from the correct position
                  }
              }
            renderingUI = false;
          }
        gl.glLoadIdentity();  // make sure we're at the beginning for translations
        gl.glDisable(GL10.GL_TEXTURE_2D);  // continue disabling stuff
        gl.glDisable(GL10.GL_CULL_FACE);
      }
  }