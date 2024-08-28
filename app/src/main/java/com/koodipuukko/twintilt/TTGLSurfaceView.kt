package com.koodipuukko.twintilt

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class TTGLSurfaceView(context: Context?, private val mRenderer: GLRenderer) :
	GLSurfaceView(context) {
	init {
		setRenderer(mRenderer)
		renderMode = RENDERMODE_CONTINUOUSLY
	}

	constructor(context: Context?) : this(context, GLRenderer(context!!)) {
	}

	constructor(context: Context?, attrs: AttributeSet?) : this(context, GLRenderer(context!!)) {
	}

	constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : this(context, GLRenderer(context!!)) {
	}
}
