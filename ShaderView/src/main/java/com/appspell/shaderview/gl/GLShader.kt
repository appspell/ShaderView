package com.appspell.shaderview.gl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import androidx.annotation.RawRes
import com.appspell.shaderview.R
import com.appspell.shaderview.ext.getRawTextFile
import java.nio.FloatBuffer

const val UNKNOWN_PROGRAM = 0

internal class GLShader {
    companion object {
        private const val TAG = "GLShader"
    }

    var program = UNKNOWN_PROGRAM

    val isReady: Boolean
        get() = program != UNKNOWN_PROGRAM

    var uniforms = ShaderParams()
        set(value) {
            field = value
            bindUniforms()
        }

    fun createProgram(context: Context, @RawRes fragmentShaderRaw: Int): Boolean {
        val vsh = context.resources.getRawTextFile(R.raw.quad_vert)
        val fsh = context.resources.getRawTextFile(fragmentShaderRaw)
        return createProgram(vsh, fsh)
    }

    fun createProgram(
        context: Context,
        @RawRes vertexShaderRaw: Int,
        @RawRes fragmentShaderRaw: Int
    ): Boolean {
        val vsh = context.resources.getRawTextFile(vertexShaderRaw)
        val fsh = context.resources.getRawTextFile(fragmentShaderRaw)
        return createProgram(vsh, fsh)
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Boolean {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return false
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return false
        }
        program = GLES20.glCreateProgram()
        if (program != UNKNOWN_PROGRAM) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ")
                Log.e(TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = UNKNOWN_PROGRAM
                return false
            }
            bindUniforms()
        }
        return true
    }

    fun onDrawFrame() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        pushValuesToProgram()
    }

    fun updateValue(attrName: String, value: Float) {
        uniforms.updateValue(attrName, value)
    }

    fun updateValue(attrName: String, value: Int) {
        uniforms.updateValue(attrName, value)
    }

    fun updateValue(attrName: String, value: Boolean) {
        uniforms.updateValue(attrName, value)
    }

    fun updateValue(attrName: String, value: FloatArray) {
        uniforms.updateValue(attrName, value)
    }

    fun updateValue(attrName: String, value: IntArray) {
        uniforms.updateValue(attrName, value)
    }

    private fun bindUniforms() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        uniforms.bindAttrs(program)
    }

    private fun pushValuesToProgram() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        uniforms.pushValuesToProgram()
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != UNKNOWN_PROGRAM) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == UNKNOWN_PROGRAM) {
                Log.e(TAG, "Could not compile shader $shaderType:")
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = UNKNOWN_PROGRAM
            }
        }
        return shader
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }

}