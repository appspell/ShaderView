package com.appspell.shaderview.gl.shader

import android.content.res.Resources
import android.opengl.GLES32
import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.log.LibLog

class GLShaderImpl constructor(
    override var params: ShaderParams
) : GLShader {

    override val isReady: Boolean
        get() = program != UNKNOWN_PROGRAM

    override var program = UNKNOWN_PROGRAM

    override fun createProgram(vertexSource: String, fragmentSource: String): Boolean {
        if (program != UNKNOWN_PROGRAM) {
            release()
        }
        val vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == UNKNOWN_PROGRAM) {
            return false
        }
        val pixelShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == UNKNOWN_PROGRAM) {
            return false
        }
        program = GLES32.glCreateProgram()
        if (program != UNKNOWN_PROGRAM) {
            GLES32.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader: vertex")
            GLES32.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader: pixel")
            return linkProgram()
        }
        return true
    }

    private fun linkProgram(): Boolean {
        if (program == UNKNOWN_PROGRAM) {
            return false
        }
        GLES32.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES32.GL_TRUE) {
            LibLog.e(TAG, "Could not link program: ")
            LibLog.e(TAG, GLES32.glGetProgramInfoLog(program))
            GLES32.glDeleteProgram(program)
            program = UNKNOWN_PROGRAM
            return false
        }
        return true
    }

    override fun onDrawFrame() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        pushValuesToProgram()
    }

    override fun release() {
        if (program != UNKNOWN_PROGRAM) {
            GLES32.glDeleteProgram(program)
            program = UNKNOWN_PROGRAM
        }
        params.release()
    }

    override fun newBuilder() = ShaderBuilder(this)

    /**
     * Bind params to shaders, (when you just set ShaderParams to the shaders)
     *
     * Do not forget to apply parameters for shaders before render
     * Call it when shader program is created
     *
     * This method gets the location of uniform params and upload textures to GPU
     *
     * @param resources - we need to upload textures from resources
     * if you don't need to load textures from android resources, you may omit such parameter
     */
    override fun bindParams(resources: Resources?) {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        params.bindParams(program, resources)
    }

    private fun pushValuesToProgram() {
        if (program == UNKNOWN_PROGRAM) {
            return
        }
        params.pushValuesToProgram()
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES32.glCreateShader(shaderType)
        if (shader != UNKNOWN_PROGRAM) {
            GLES32.glShaderSource(shader, source)
            GLES32.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == UNKNOWN_PROGRAM) {
                LibLog.e(TAG, "Could not compile shader $shaderType:")
                LibLog.e(TAG, GLES32.glGetShaderInfoLog(shader))
                GLES32.glDeleteShader(shader)
                shader = UNKNOWN_PROGRAM
            }
        }
        return shader
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES32.glGetError().also { error = it } != GLES32.GL_NO_ERROR) {
            LibLog.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}