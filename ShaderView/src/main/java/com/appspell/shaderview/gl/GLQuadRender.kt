package com.appspell.shaderview.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.appspell.shaderview.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock

private const val TAG = "GLQuadRender"
private const val FLOAT_SIZE_BYTES = 4
private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

//private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65 // TODO remove
private const val UNKNOWN_ATTRIBUTE = -1

/**
 * Render full-screen quad render
 */
internal class GLQuadRender(
    private val context: Context // TODO remove Android dependency
) : GLTextureView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    companion object {
        const val VERTEX_SHADER_IN_POSITION = "inPosition"
        const val VERTEX_SHADER_IN_TEXTURE_COORD = "inTextureCoord"

        const val VERTEX_SHADER_UNIFORM_MATRIX_MVP = "uMVPMatrix"
        const val VERTEX_SHADER_UNIFORM_MATRIX_STM = "uSTMatrix"
    }

    private val quadVertices: FloatBuffer

    private var shader = GLShader()

    // TODO delete
//    private var mTextureID = 0

    private val matrixMVP = FloatArray(16)
    private val matrixSTM = FloatArray(16)

    // shader vertex attributes
    private var inPositionHandle = 0
    private var inTextureHandle = 0

    private var surface: SurfaceTexture? = null

    @Volatile
    private var updateSurface = false

    private val lock = ReentrantLock()

    init {
        // set array of Quad vertices
        val quadVerticesData = floatArrayOf(
            // [x,y,z, U,V]
            -1.0f, -1.0f, 0f, 0f, 0f,
            1.0f, -1.0f, 0f, 1f, 0f,
            -1.0f, 1.0f, 0f, 0f, 1f,
            1.0f, 1.0f, 0f, 1f, 1f
        )

        quadVertices = ByteBuffer
            .allocateDirect(quadVerticesData.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadVerticesData).position(0)
            }

        // initialize matrix
        Matrix.setIdentityM(matrixSTM, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // TODO move it to the upper level
        if (!shader.create(context, R.raw.simple_frag)) {
            return
        }

        // set custom uniforms
        shader.params = ShaderParams.Builder()
            .addVec3f("myUniform")
            .addBool("isEnabled", false)
            .build()

        if (!shader.isReady) {
            return
        }

        // built-in parameters
        shader.params = shader.params.newBuilder()
            .addMat4f(VERTEX_SHADER_UNIFORM_MATRIX_MVP)
            .addMat4f(VERTEX_SHADER_UNIFORM_MATRIX_STM)
            .build()

        // set attributes (input for Vertex Shader)
        inPositionHandle = glGetAttribLocation(VERTEX_SHADER_IN_POSITION)
        inTextureHandle = glGetAttribLocation(VERTEX_SHADER_IN_TEXTURE_COORD)

//        val textures = IntArray(1)
//        GLES30.glGenTextures(1, textures, 0)
//        mTextureID = textures[0]
//        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
//        checkGlError("glBindTexture mTextureID")
//        GLES30.glTexParameterf(
//            GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER,
//            GLES30.GL_NEAREST.toFloat()
//        )
//        GLES30.glTexParameterf(
//            GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER,
//            GLES30.GL_LINEAR.toFloat()
//        )
//        mSurface = SurfaceTexture(mTextureID)

        surface?.setOnFrameAvailableListener(this)
        lock.withLock { updateSurface = false }
    }

    override fun onDrawFrame(gl: GL10?) {
        lock.withLock {
            if (updateSurface) {
                surface?.updateTexImage()
                surface?.getTransformMatrix(matrixSTM)
                updateSurface = false
            }
        }
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(shader.program)
        checkGlError("glUseProgram")
//        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
//        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)

        // shader input
        // vertex shader inPosition
        quadVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES30.glVertexAttribPointer(
            inPositionHandle, 3, GLES30.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, quadVertices
        )
        checkGlError("glVertexAttribPointer $VERTEX_SHADER_IN_POSITION")
        GLES30.glEnableVertexAttribArray(inPositionHandle)
        checkGlError("glEnableVertexAttribArray iPositionHandle")

        // vertex shader inTextCoord
        quadVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES30.glVertexAttribPointer(
            inTextureHandle, 3, GLES30.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, quadVertices
        )
        checkGlError("glVertexAttribPointer iTextureHandle")
        GLES30.glEnableVertexAttribArray(inTextureHandle)
        checkGlError("glEnableVertexAttribArray iTextureHandle")

        // update uniforms
        shader.updateValue(
            "myUniform",
            floatArrayOf(
                1f,
                (System.currentTimeMillis() % 5000L) / 5000f,
                (System.currentTimeMillis() % 1000L) / 1000f
            )
        )

        Matrix.setIdentityM(matrixMVP, 0)
        shader.updateValue(VERTEX_SHADER_UNIFORM_MATRIX_MVP, matrixMVP)
        shader.updateValue(VERTEX_SHADER_UNIFORM_MATRIX_STM, matrixSTM)

        shader.onDrawFrame()
        checkGlError("onDrawFrame")

        // draw scene
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")

        GLES30.glFinish()
    }

    override fun onFrameAvailable(surface: SurfaceTexture) {
        lock.withLock {
            updateSurface = true
        }
    }

    private fun glGetAttribLocation(attrName: String): Int {
        val attr = GLES30.glGetAttribLocation(shader.program, attrName)
        checkGlError("glGetAttribLocation $attrName")
        if (inTextureHandle == UNKNOWN_ATTRIBUTE) {
            throw RuntimeException("Could not get attrib location for input '$attrName'")
        }
        return attr
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}