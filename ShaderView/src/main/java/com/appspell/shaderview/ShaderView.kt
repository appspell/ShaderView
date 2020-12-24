package com.appspell.shaderview

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import com.appspell.shaderview.gl.GLQuadRender
import com.appspell.shaderview.gl.GLTextureView

private const val OPENGL_VERSION = 3

class ShaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    GLTextureView(context, attrs, defStyleAttr),
    SurfaceTextureListener,
    View.OnLayoutChangeListener {

    /**
     * should we re-render this view all the time
     */
    var updateContinuously: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
            } else {
                setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
            }
        }

    init {
        setEGLContextClientVersion(OPENGL_VERSION)
        val renderer = GLQuadRender(context)
        setRenderer(renderer)
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
    }
}