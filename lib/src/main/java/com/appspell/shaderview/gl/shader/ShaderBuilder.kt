package com.appspell.shaderview.gl.shader

import android.content.Context
import androidx.annotation.RawRes
import com.appspell.shaderview.R
import com.appspell.shaderview.ext.getRawTextFile
import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import com.appspell.shaderview.log.LibLog

class ShaderBuilder {
    private var shader: GLShader

    constructor() {
        this.shader = GLShaderImpl(params = ShaderParamsBuilder().build())
    }

    internal constructor(shader: GLShader) {
        this.shader = shader
    }

    fun fragmentShader(
        context: Context,
        @RawRes fragmentShaderRawResId: Int
    ): ShaderBuilder {
        val vsh = context.resources.getRawTextFile(R.raw.quad_vert)
        val fsh = context.resources.getRawTextFile(fragmentShaderRawResId)
        if (!shader.createProgram(vsh, fsh)) {
            LibLog.e(TAG, "shader program wasn't created")
        }
        return this
    }

    fun create(
        context: Context,
        @RawRes vertexShaderRawResId: Int,
        @RawRes fragmentShaderRawResId: Int
    ): ShaderBuilder {
        val vsh = context.resources.getRawTextFile(vertexShaderRawResId)
        val fsh = context.resources.getRawTextFile(fragmentShaderRawResId)
        if (!shader.createProgram(vsh, fsh)) {
            LibLog.e(TAG, "shader program wasn't created")
        }
        return this
    }
    fun create(
        vertexShader: String,
        fragmentShader: String
    ): ShaderBuilder {
        if (!shader.createProgram(vertexShader, fragmentShader)) {
            LibLog.e(TAG, "shader program wasn't created")
        }
        return this
    }

    fun params(shaderParams: ShaderParams): ShaderBuilder {
        shader.params = shaderParams
        return this
    }

    fun build() = shader
}