package com.koodipuukko.twintilt;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class TTGLSurfaceView
  extends GLSurfaceView
  {
    private final GLRenderer mRenderer;

    public TTGLSurfaceView(Context context, GLRenderer renderer)
      {
        super(context);
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = renderer;
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
      }
  }
