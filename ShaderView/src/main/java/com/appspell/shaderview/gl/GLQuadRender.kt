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

/**
 * Render full-screen quad
 */
internal class GLQuadRender(
    private val context: Context // TODO remove Android dependency
) : GLTextureView.Renderer,
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

    @Volatile
    private var updateSurface = false

    private val lock = ReentrantLock()

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
        if (!shader.createProgram(context, R.raw.simple_frag)) {
            return
        }

        // set custom uniforms
        shader.uniforms = ShaderParams.Builder()
            .addVec3("myUniform", floatArrayOf(0f, 0f, 0f))
            .add("isEnabled", false)
            .build()

        maPositionHandle = GLES30.glGetAttribLocation(shader.program, "inPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        maTextureHandle = GLES30.glGetAttribLocation(shader.program, "inTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (maTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }
        muMVPMatrixHandle = GLES30.glGetUniformLocation(shader.program, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (muMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }
        muSTMatrixHandle = GLES30.glGetUniformLocation(shader.program, "uSTMatrix")
        checkGlError("glGetUniformLocation uSTMatrix")
        if (muSTMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uSTMatrix")
        }

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

        /*
         * Create the SurfaceTexture that will feed this textureID,
         * and pass it to the MediaPlayer
         */
//        mSurface = SurfaceTexture(mTextureID)
        mSurface?.setOnFrameAvailableListener(this)
//            val surface = Surface(mSurface)
        lock.withLock { updateSurface = false }
    }


    override fun onDrawFrame(gl: GL10?) {
        lock.withLock {
            if (updateSurface) {
                mSurface?.updateTexImage()
                mSurface?.getTransformMatrix(mSTMatrix)
                updateSurface = false
            }
        }
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(shader.program)
        checkGlError("glUseProgram")
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES30.glVertexAttribPointer(
            maPositionHandle, 3, GLES30.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer aPosition")
        GLES30.glEnableVertexAttribArray(maPositionHandle)
        checkGlError("glEnableVertexAttribArray maPositionHandle")
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES30.glVertexAttribPointer(
            maTextureHandle, 3, GLES30.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maTextureHandle")
        GLES30.glEnableVertexAttribArray(maTextureHandle)
        checkGlError("glEnableVertexAttribArray maTextureHandle")

        Matrix.setIdentityM(mMVPMatrix, 0)
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES30.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)


        // update uniforms
        shader.updateValue(
            "myUniform",
            floatArrayOf(
                1f,
                (System.currentTimeMillis() % 5000L) / 5000f,
                (System.currentTimeMillis() % 1000L) / 1000f
            )
        )
        shader.updateValue("isEnabled", false)
        shader.onDrawFrame()
        checkGlError("onDrawFrame")

        GLES30.glFinish()
    }

    override fun onFrameAvailable(surface: SurfaceTexture) {
        lock.withLock {
            updateSurface = true
        }
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}