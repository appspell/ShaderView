package com.appspell.shaderview.gl

import android.opengl.GLES30
import java.util.*

const val UNKNOWN_LOCATION = -1

class ShaderParams {

    private data class Param(
        val valeType: ValueType,
        var location: Int = UNKNOWN_LOCATION,
        var value: Any? = null
    ) {
        enum class ValueType {
            FLOAT, INT, BOOL,
            FLOAT_VEC2, FLOAT_VEC3, FLOAT_VEC4,
            INT_VEC2, INT_VEC3, INT_VEC4,
            MAT3, MAT4, MAT3x4,
            SAMPLER_2D
        }
    }

    private val map = HashMap<String, Param>()

    fun updateValue(paramName: String, value: Float) {
        map[paramName]?.value = value
    }

    fun updateValue(paramName: String, value: Int) {
        map[paramName]?.value = value
    }

    fun updateValue(paramName: String, value: Boolean) {
        map[paramName]?.value = value
    }

    fun updateValue(paramName: String, value: FloatArray) {
        map[paramName]?.value = value
    }

    fun updateValue(paramName: String, value: IntArray) {
        map[paramName]?.value = value
    }

    fun updateParamsLocation(paramName: String, shaderProgram: Int) {
        map[paramName]?.apply {
            this.location = GLES30.glGetUniformLocation(shaderProgram, paramName)
        }
    }

    fun bindParams(shaderProgram: Int) {
        for (key in map.keys) {
            updateParamsLocation(key, shaderProgram)
        }
    }

    fun newBuilder() = Builder(this)

    fun pushValuesToProgram() {
        for (key in map.keys) {
            val param = map[key]
            if (param == null || param.location == UNKNOWN_LOCATION) {
                continue
            }
            when (param.valeType) {
                Param.ValueType.FLOAT -> GLES30.glUniform1f(param.location, param.value as Float)
                Param.ValueType.INT -> GLES30.glUniform1i(param.location, param.value as Int)
                Param.ValueType.BOOL -> GLES30.glUniform1i(param.location, if (param.value as Boolean) 1 else 0)
                Param.ValueType.FLOAT_VEC2 -> GLES30.glUniform2fv(
                    param.location,
                    1,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.FLOAT_VEC3 -> GLES30.glUniform3fv(
                    param.location,
                    1,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.FLOAT_VEC4 -> GLES30.glUniform4fv(
                    param.location,
                    1,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.INT_VEC2 -> GLES30.glUniform2iv(
                    param.location,
                    1,
                    (param.value as IntArray),
                    0
                )
                Param.ValueType.INT_VEC3 -> GLES30.glUniform3iv(
                    param.location,
                    1,
                    (param.value as IntArray),
                    0
                )
                Param.ValueType.INT_VEC4 -> GLES30.glUniform4iv(
                    param.location,
                    1,
                    (param.value as IntArray),
                    0
                )
                Param.ValueType.MAT3 -> GLES30.glUniformMatrix3fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.MAT4 -> GLES30.glUniformMatrix4fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.MAT3x4 -> GLES30.glUniformMatrix3x4fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.SAMPLER_2D -> TODO()
            }
        }
    }

    class Builder {

        private val result: ShaderParams

        constructor() {
            this.result = ShaderParams()
        }

        internal constructor(result: ShaderParams) {
            this.result = result
        }

        fun addFloat(paramName: String, value: Float? = null): Builder {
            val param = Param(valeType = Param.ValueType.FLOAT, value = value)
            result.map[paramName] = param
            return this
        }

        fun addInt(paramName: String, value: Int? = null): Builder {
            val param = Param(valeType = Param.ValueType.INT, value = value)
            result.map[paramName] = param
            return this
        }

        fun addBool(paramName: String, value: Boolean? = null): Builder {
            val param = Param(valeType = Param.ValueType.BOOL, value = value)
            result.map[paramName] = param
            return this
        }

        fun addVec2f(paramName: String, value: FloatArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.FLOAT_VEC2, value = value)
            result.map[paramName] = param
            return this
        }

        fun addVec3f(paramName: String, value: FloatArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.FLOAT_VEC3, value = value)
            result.map[paramName] = param
            return this
        }

        fun addVec4f(paramName: String, value: FloatArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.FLOAT_VEC4, value = value)
            result.map[paramName] = param
            return this
        }

        fun addVec2i(paramName: String, value: IntArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.INT_VEC2, value = value)
            result.map[paramName] = param
            return this
        }

        fun addVec3i(paramName: String, value: IntArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.INT_VEC3, value = value)
            result.map[paramName] = param
            return this
        }

        fun addVec4i(paramName: String, value: IntArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.INT_VEC4, value = value)
            result.map[paramName] = param
            return this
        }

        fun addMat3f(paramName: String, value: FloatArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.MAT3, value = value)
            result.map[paramName] = param
            return this
        }

        fun addMat4f(paramName: String, value: FloatArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.MAT4, value = value)
            result.map[paramName] = param
            return this
        }

        fun addMat3x4f(paramName: String, value: FloatArray? = null): Builder {
            val param = Param(valeType = Param.ValueType.MAT3x4, value = value)
            result.map[paramName] = param
            return this
        }

        fun build() = result
    }
}
