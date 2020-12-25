package com.appspell.shaderview.gl

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import androidx.annotation.RawRes
import com.appspell.shaderview.R
import com.appspell.shaderview.ext.getRawTextFile

const val UNKNOWN_PROGRAM = 0
private const val TAG = "GLShader"

// TODO builder
internal class GLShader {

    val isReady: Boolean
        get() = program != UNKNOWN_PROGRAM

    var params = ShaderParams()
        set(value) {
            field = value
            bindParams()
        }

    var program = UNKNOWN_PROGRAM

    fun create(context: Context, @RawRes fragmentShaderRaw: Int): Boolean {
        val vsh = context.resources.getRawTextFile(R.raw.quad_vert)
        val fsh = context.resources.getRawTextFile(fragmentShaderRaw)
        return create(vsh, fsh)
    }

    fun create(
        context: Context,
        @RawRes vertexShaderRaw: Int,
        @RawRes fragmentShaderRaw: Int
    ): Boolean {
        val vsh = context.resources.getRawTextFile(vertexShaderRaw)
        val fsh = context.resources.getRawTextFile(fragmentShaderRaw)
        return create(vsh, fsh)
    }

    fun create(vertexSource: String, fragmentSource: String): Boolean {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return false
        }
        val pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return false
        }
        program = GLES30.glCreateProgram()
        if (program != UNKNOWN_PROGRAM) {
            GLES30.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES30.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES30.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES30.GL_TRUE) {
                Log.e(TAG, "Could not link program: ")
                Log.e(TAG, GLES30.glGetProgramInfoLog(program))
                GLES30.glDeleteProgram(program)
                program = UNKNOWN_PROGRAM
                return false
            }
            bindParams()
        }
        return true
    }

    fun onDrawFrame() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        pushValuesToProgram()
    }

    fun updateValue(paramName: String, value: Float) {
        params.updateValue(paramName, value)
    }

    fun updateValue(paramName: String, value: Int) {
        params.updateValue(paramName, value)
    }

    fun updateValue(paramName: String, value: Boolean) {
        params.updateValue(paramName, value)
    }

    fun updateValue(paramName: String, value: FloatArray) {
        params.updateValue(paramName, value)
    }

    fun updateValue(paramName: String, value: IntArray) {
        params.updateValue(paramName, value)
    }

    private fun bindParams() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        params.bindParams(program)
    }

    private fun pushValuesToProgram() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        params.pushValuesToProgram()
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES30.glCreateShader(shaderType)
        if (shader != UNKNOWN_PROGRAM) {
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == UNKNOWN_PROGRAM) {
                Log.e(TAG, "Could not compile shader $shaderType:")
                Log.e(TAG, GLES30.glGetShaderInfoLog(shader))
                GLES30.glDeleteShader(shader)
                shader = UNKNOWN_PROGRAM
            }
        }
        return shader
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}