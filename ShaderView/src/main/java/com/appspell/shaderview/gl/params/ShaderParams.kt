package com.appspell.shaderview.gl.params

import android.content.res.Resources

const val UNKNOWN_LOCATION = -1

interface ShaderParams {

    fun updateParam(paramName: String, param: Param)
    fun updateValue(paramName: String, value: Float)
    fun updateValue(paramName: String, value: Int)
    fun updateValue(paramName: String, value: Boolean)
    fun updateValue(paramName: String, value: FloatArray)
    fun updateValue(paramName: String, value: IntArray)

    fun getParamShaderLocation(paramName: String): Int?
    fun getParamValue(paramName: String): Any?

    fun pushValuesToProgram()
    fun bindParams(shaderProgram: Int, resources: Resources?)

    fun release()

    fun newBuilder(): ShaderParamsBuilder
}