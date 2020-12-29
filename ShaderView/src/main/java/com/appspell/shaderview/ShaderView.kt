package com.appspell.shaderview

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import androidx.annotation.RawRes
import com.appspell.shaderview.gl.GLQuadRender
import com.appspell.shaderview.gl.GLShader
import com.appspell.shaderview.gl.GLTextureView
import com.appspell.shaderview.gl.ShaderParams

private const val OPENGL_VERSION = 3
private val DEFAULT_VERTEX_SHADER_RESOURCE = R.raw.quad_vert
private val DEFAULT_FRAGMENT_SHADER_RESOURCE = R.raw.default_frag

class ShaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    GLTextureView(context, attrs, defStyleAttr),
    SurfaceTextureListener,
    View.OnLayoutChangeListener {

    @RawRes
    var vertexShaderRawResId: Int? = null

    @RawRes
    var fragmentShaderRawResId: Int? = null

    var shaderParams: ShaderParams? = null
    var onViewReadyListener: ((shader: GLShader) -> Unit)? = null
    var onDrawFrameListener: ((shaderParams: ShaderParams) -> Unit)? = null

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

    private val renderer = GLQuadRender()

    private val rendererListener = object : GLQuadRender.ShaderViewListener {
        override fun onSurfaceCreated() {
            initShaders()
            onViewReadyListener?.invoke(renderer.shader)
        }

        override fun onDrawFrame(shaderParams: ShaderParams) {
            onDrawFrameListener?.invoke(shaderParams)
        }
    }

    init {
        initAttr(attrs)

        setEGLContextClientVersion(OPENGL_VERSION)
        renderer.listener = rendererListener
        setRenderer(renderer)
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
    }

    private fun initAttr(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ShaderView, 0, 0)
            .apply {
                try {
                    fragmentShaderRawResId =
                        getResourceId(
                            R.styleable.ShaderView_fragment_shader_raw_res_id,
                            DEFAULT_FRAGMENT_SHADER_RESOURCE
                        )
                    vertexShaderRawResId =
                        getResourceId(R.styleable.ShaderView_vertex_shader_raw_res_id, DEFAULT_VERTEX_SHADER_RESOURCE)
                } finally {
                    recycle()
                }
            }
    }

    private fun initShaders() {
        fragmentShaderRawResId?.let { fragmentShader ->
            renderer.shader = renderer.shader.newBuilder()
                .create(
                    context = context,
                    vertexShaderRawResId = vertexShaderRawResId ?: DEFAULT_VERTEX_SHADER_RESOURCE,
                    fragmentShaderRawResId = fragmentShader
                )
                .apply { shaderParams?.apply { params(this) } }
                .build()
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderer.shader.release()
        return super.onSurfaceTextureDestroyed(surface)
    }
}