package com.appspell.shaderview

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.RawRes
import androidx.annotation.StyleRes
import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.gl.params.ShaderParamsImpl
import com.appspell.shaderview.gl.render.GLQuadRender
import com.appspell.shaderview.gl.render.GLQuadRenderImpl
import com.appspell.shaderview.gl.shader.GLShader
import com.appspell.shaderview.gl.shader.GLShaderImpl
import com.appspell.shaderview.gl.view.GLTextureView
import com.appspell.shaderview.log.LibLog

private const val OPENGL_VERSION = 3

private const val BIT_PER_CHANEL = 8
private const val DEPTH_BIT_PER_CHANEL = 16
private const val STENCIL_BIT_PER_CHANEL = 0

private val DEFAULT_VERTEX_SHADER_RESOURCE = R.raw.quad_vert
private val DEFAULT_FRAGMENT_SHADER_RESOURCE = R.raw.default_frag

class ShaderView @JvmOverloads constructor(
    context: Context,
    @AttrRes attrs: AttributeSet? = null,
    @StyleRes defStyleAttr: Int = 0
) : GLTextureView(context, attrs, defStyleAttr) {

    @RawRes
    var vertexShaderRawResId: Int? = null
        set(value) {
            needToRecreateShaders = true
            field = value
        }

    @RawRes
    var fragmentShaderRawResId: Int? = null
        set(value) {
            needToRecreateShaders = true
            field = value
        }

    var vertexShader: String? = null
        set(value) {
            needToRecreateShaders = true
            field = value
        }
    var fragmentShader: String? = null
        set(value) {
            needToRecreateShaders = true
            field = value
        }

    var shaderParams: ShaderParams? = null
        set(value) {
            field = value
            updateShaderParams()
        }
    var onViewReadyListener: ((shader: GLShader) -> Unit)? = null
    var onDrawFrameListener: ((shaderParams: ShaderParams) -> Unit)? = null

    private var needToRecreateShaders = false

    /**
     * Enable or disable logging for all of ShaderView globally
     * TODO it need to enable logs for this view only
     */
    var debugMode = false
        set(value) {
            field = value
            LibLog.isEnabled = value // TODO should be enabled for particular view only
            if (value) {
                setDebugFlags(DEBUG_CHECK_GL_ERROR.and(DEBUG_LOG_GL_CALLS))
                enableLogPauseResume = true
                enableLogEgl = true
                enableLogSurface = true
            }
        }

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

    /**
     * how many frames the shader should be drawn per second
     */
    var framerate: Int
        set(value) {
            setFPS(value)
        }
        get(): Int {
            return getFPS()
        }

    private val rendererListener = object : GLQuadRender.ShaderViewListener {
        override fun onSurfaceCreated() {
            initShaders()
            post {
                onViewReadyListener?.invoke(renderer.shader)
            }
        }

        override fun onDrawFrame(shaderParams: ShaderParams) {
            post {
                onDrawFrameListener?.invoke(shaderParams)
            }
        }
    }

    private val renderer: GLQuadRender = GLQuadRenderImpl(shader = GLShaderImpl(params = ShaderParamsImpl()))

    init {
        initAttr(attrs)

        setEGLContextClientVersion(OPENGL_VERSION)
        renderer.listener = rendererListener

        // use RGBA_8888 buffer to support transparency
        setEGLConfigChooser(
            BIT_PER_CHANEL,
            BIT_PER_CHANEL,
            BIT_PER_CHANEL,
            BIT_PER_CHANEL,
            DEPTH_BIT_PER_CHANEL,
            STENCIL_BIT_PER_CHANEL
        )

        setRenderer(renderer)

        // make this view transparent
        isOpaque = false

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

    private fun updateShaderParams() {
        if (needToRecreateShaders) {
            return
        }
        shaderParams?.apply { renderer.shader.params = this }
    }

    private fun initShaders() {
        if (needToRecreateShaders) {
            fragmentShaderRawResId?.also { fragmentShader ->
                // delete existing shader if we have some
                renderer.shader.release()

                vertexShader?.let {
                    // create a new shader from text
                    renderer.shader = renderer.shader.newBuilder()
                        .create(
                            vertexShader = this.vertexShader!!,
                            fragmentShader = this.fragmentShader!!
                        )
                        .apply {
                            // if we have some ShaderParams to set
                            shaderParams?.apply { params(this) }
                        }
                        .build()
                        .also {
                            needToRecreateShaders = true
                        }
                } ?: run {
                    // create a new shader from resources
                    renderer.shader = renderer.shader.newBuilder()
                        .create(
                            context = context,
                            vertexShaderRawResId = vertexShaderRawResId
                                ?: DEFAULT_VERTEX_SHADER_RESOURCE,
                            fragmentShaderRawResId = fragmentShader
                        )
                        .apply {
                            // if we have some ShaderParams to set
                            shaderParams?.apply { params(this) }
                        }
                        .build()
                        .also {
                            needToRecreateShaders = true
                        }
                }
            }
        }

        // bind shader params.
        // note: we have to pass [android.content.res.Resources] to be able to load textures from Resources
        renderer.shader.bindParams(resources)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderer.shader.release()
        return super.onSurfaceTextureDestroyed(surface)
    }
}