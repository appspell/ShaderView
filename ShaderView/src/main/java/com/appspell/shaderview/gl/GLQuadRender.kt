package com.appspell.shaderview.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.Matrix
import android.util.Log
import com.appspell.shaderview.R
import com.appspell.shaderview.ext.getRawTextFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random


/**
 * Render full-screen quad
 */
internal class GLQuadRender(val context: Context) : GLTextureView.Renderer,
    SurfaceTexture.OnFrameAvailableListener {

    companion object {
        private const val TAG = "GLQuadRender"
        private const val FLOAT_SIZE_BYTES = 4
        private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
        private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
        private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65
    }

    // array of vertices [x,y,z, U,V]
    private val mTriangleVerticesData = floatArrayOf(
        -1.0f, -1.0f, 0f, 0f, 0f,
        1.0f, -1.0f, 0f, 1f, 0f,
        -1.0f, 1.0f, 0f, 0f, 1f,
        1.0f, 1.0f, 0f, 1f, 1f
    )

    private val mTriangleVertices: FloatBuffer

    private val mMVPMatrix = FloatArray(16)
    private val mSTMatrix = FloatArray(16)

    private var shader = GLShader()

    private var mTextureID = 0
    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0
    private var maPositionHandle = 0
    private var maTextureHandle = 0

    private var mSurface: SurfaceTexture? = null
    private var updateSurface = false

    init {
        mTriangleVertices = ByteBuffer.allocateDirect(
            mTriangleVerticesData.size * FLOAT_SIZE_BYTES
        )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTriangleVertices.put(mTriangleVerticesData).position(0)
        Matrix.setIdentityM(mSTMatrix, 0)
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // set custom uniforms
        shader.uniforms = ShaderParams.Builder()
            .addVec3("myUniform", floatArrayOf(0f, 0f, 0f))
//            .add("isEnabled", false)
            .build()

        if (!shader.createProgram(context, R.raw.simple_frag)) {
            return
        }

        maPositionHandle = GLES20.glGetAttribLocation(shader.program, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        maTextureHandle = GLES20.glGetAttribLocation(shader.program, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (maTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(shader.program, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (muMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }
        muSTMatrixHandle = GLES20.glGetUniformLocation(shader.program, "uSTMatrix")
        checkGlError("glGetUniformLocation uSTMatrix")
        if (muSTMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uSTMatrix")
        }

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        checkGlError("glBindTexture mTextureID")
        GLES20.glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )

        /*
         * Create the SurfaceTexture that will feed this textureID,
         * and pass it to the MediaPlayer
         */
        mSurface = SurfaceTexture(mTextureID)
        mSurface?.setOnFrameAvailableListener(this)
//            val surface = Surface(mSurface)
        synchronized(this) { updateSurface = false }
    }


    override fun onDrawFrame(gl: GL10?) {
        synchronized(this) {
            if (updateSurface) {
                mSurface?.updateTexImage()
                mSurface?.getTransformMatrix(mSTMatrix)
                updateSurface = false
            }
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(shader.program)
        checkGlError("glUseProgram")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(maPositionHandle)
        checkGlError("glEnableVertexAttribArray maPositionHandle")
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            maTextureHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maTextureHandle")
        GLES20.glEnableVertexAttribArray(maTextureHandle)
        checkGlError("glEnableVertexAttribArray maTextureHandle")

        Matrix.setIdentityM(mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")


        // update uniforms
        shader.updateValue("myUniform", floatArrayOf(1f, (System.currentTimeMillis() % 100L) / 100f, (System.currentTimeMillis() % 1000L) / 1000f))
//        shader.updateValue("isEnabled", true)
        shader.onDrawFrame()
        checkGlError("glUniform1i isEnabled")

        GLES20.glFinish()
    }

    @Synchronized
    override fun onFrameAvailable(surface: SurfaceTexture) {
        updateSurface = true
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}