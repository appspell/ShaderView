package com.appspell.shaderview.gl

import android.opengl.GLES30
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

const val UNKNOWN_LOCATION = -1

class ShaderParams {
    private data class Attr(
        val type: AttrType,
        var location: Int = UNKNOWN_LOCATION,
        var value: Any? = null
    )

    enum class AttrType {
        FLOAT, INT, BOOL,
        FLOAT_VEC2, FLOAT_VEC3, FLOAT_VEC4,
        INT_VEC2, INT_VEC3, INT_VEC4,
        MAT3, MAT4, MAT3x4,
        SAMPLER_2D
    }

    private val map = HashMap<String, Attr>()

    fun updateValue(attrName: String, value: Float) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: Int) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: Boolean) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: FloatArray) {
        map[attrName]?.value = value
    }

    fun updateValue(attrName: String, value: IntArray) {
        map[attrName]?.value = value
    }

    fun updateAttrLocation(attrName: String, shaderProgram: Int) {
        map[attrName]?.apply {
            this.location = GLES30.glGetUniformLocation(shaderProgram, attrName)
        }
    }

    fun bindAttrs(shaderProgram: Int) {
        for (key in map.keys) {
            updateAttrLocation(key, shaderProgram)
        }
    }

    fun pushValuesToProgram() {
        for (key in map.keys) {
            val attr = map[key]
            if (attr == null || attr.location == UNKNOWN_LOCATION) {
                continue
            }
            when (attr.type) {
                AttrType.FLOAT -> GLES30.glUniform1f(attr.location, attr.value as Float)
                AttrType.INT -> GLES30.glUniform1i(attr.location, attr.value as Int)
                AttrType.BOOL -> GLES30.glUniform1i(attr.location, if (attr.value as Boolean) 1 else 0)
                AttrType.FLOAT_VEC2 -> GLES30.glUniform2fv(
                    attr.location,
                    1,
                    (attr.value as FloatArray),
                    0
                )
                AttrType.FLOAT_VEC3 -> GLES30.glUniform3fv(
                    attr.location,
                    1,
                    (attr.value as FloatArray),
                    0
                )
                AttrType.FLOAT_VEC4 -> GLES30.glUniform4fv(
                    attr.location,
                    1,
                    (attr.value as FloatArray),
                    0
                )
                AttrType.INT_VEC2 -> GLES30.glUniform2iv(
                    attr.location,
                    1,
                    (attr.value as IntArray),
                    0
                )
                AttrType.INT_VEC3 -> GLES30.glUniform3iv(
                    attr.location,
                    1,
                    (attr.value as IntArray),
                    0
                )
                AttrType.INT_VEC4 -> GLES30.glUniform4iv(
                    attr.location,
                    1,
                    (attr.value as IntArray),
                    0
                )
                AttrType.MAT3 -> GLES30.glUniformMatrix3fv(
                    attr.location,
                    1,
                    false,
                    (attr.value as FloatArray),
                    0
                )
                AttrType.MAT4 -> GLES30.glUniformMatrix4fv(
                    attr.location,
                    1,
                    false,
                    (attr.value as FloatArray),
                    0
                )
                AttrType.MAT3x4 -> GLES30.glUniformMatrix3x4fv(
                    attr.location,
                    1,
                    false,
                    (attr.value as FloatArray),
                    0
                )
                AttrType.SAMPLER_2D -> TODO()
            }
        }
    }

    class Builder {
        private val result = ShaderParams()

        fun addFloat(attrName: String, value: Float? = null): Builder {
            val attr = Attr(type = AttrType.FLOAT, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addInt(attrName: String, value: Int? = null): Builder {
            val attr = Attr(type = AttrType.INT, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addBool(attrName: String, value: Boolean? = null): Builder {
            val attr = Attr(type = AttrType.BOOL, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec2f(attrName: String, value: FloatArray? = null): Builder {
            val attr = Attr(type = AttrType.FLOAT_VEC2, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec3f(attrName: String, value: FloatArray? = null): Builder {
            val attr = Attr(type = AttrType.FLOAT_VEC3, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec4f(attrName: String, value: FloatArray? = null): Builder {
            val attr = Attr(type = AttrType.FLOAT_VEC4, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec2i(attrName: String, value: IntArray? = null): Builder {
            val attr = Attr(type = AttrType.INT_VEC2, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec3i(attrName: String, value: IntArray? = null): Builder {
            val attr = Attr(type = AttrType.INT_VEC3, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addVec4i(attrName: String, value: IntArray? = null): Builder {
            val attr = Attr(type = AttrType.INT_VEC4, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addMat3f(attrName: String, value: FloatArray? = null): Builder {
            val attr = Attr(type = AttrType.MAT3, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addMat4f(attrName: String, value: FloatArray? = null): Builder {
            val attr = Attr(type = AttrType.MAT4, value = value)
            result.map[attrName] = attr
            return this
        }

        fun addMat3x4f(attrName: String, value: FloatArray? = null): Builder {
            val attr = Attr(type = AttrType.MAT3x4, value = value)
            result.map[attrName] = attr
            return this
        }

        fun build() = result
    }
}
