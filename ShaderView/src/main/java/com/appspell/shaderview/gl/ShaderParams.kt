package com.appspell.shaderview.gl

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.view.Surface
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.appspell.shaderview.ext.createExternalTexture
import com.appspell.shaderview.ext.getTexture2dOESSurfaceTexture
import com.appspell.shaderview.ext.loadBitmapForTexture
import com.appspell.shaderview.ext.toGlTexture
import java.util.*

const val UNKNOWN_LOCATION = -1

class ShaderParams {

    private data class Param(
        val valeType: ValueType,
        var location: Int = UNKNOWN_LOCATION,
        var value: Any? = null,
        var addtional: Any? = null
    ) {
        enum class ValueType {
            FLOAT, INT, BOOL,
            FLOAT_VEC2, FLOAT_VEC3, FLOAT_VEC4,
            INT_VEC2, INT_VEC3, INT_VEC4,
            MAT3, MAT4, MAT3x4,
            SAMPLER_2D, SAMPLER_OES
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

    /**
     * Usually it returns uniform shader ID of particaluar parameter (if initialized)
     */
    fun getParamShaderLocation(paramName: String): Int? = map[paramName]?.location

    fun getParamValue(paramName: String): Any? = map[paramName]?.value

    fun getParamAdditionalField(paramName: String): Any? = map[paramName]?.addtional

    private fun updateParams(paramName: String, shaderProgram: Int) {
        map[paramName]?.apply {
            location = GLES30.glGetUniformLocation(shaderProgram, paramName)

            when (valeType) {
                // We have a different flow for Textures.
                // At first, we get a bitmap from params and when OpenGL context is ready we convert it to Texture
                Param.ValueType.SAMPLER_2D -> {
                    // if it is a Bitmap let's upload it to the GPU
                    value = value
                        ?.takeIf { it is Bitmap }
                        ?.run { (this as? Bitmap)?.toGlTexture(needToRecycle = true, addtional as Int) }
                        ?: value
                }
                // create Surface for External Texture
                Param.ValueType.SAMPLER_OES -> {
                    if (value == null) {
                        // if it's not initialized
                        location = createExternalTexture()
                        value = SurfaceTexture(location)
                        addtional = Surface(value as SurfaceTexture)
                    }
                }
                else -> {
                    // Do Nothing for the other types
                }
            }

        }
    }


    fun release() {
        for (key in map.keys) {
            map[key]?.apply {
                when (valeType) {
                    Param.ValueType.SAMPLER_OES -> {
                        (value as? SurfaceTexture)?.release()
                        (addtional as? SurfaceTexture)?.release()
                    }
                }
            }
        }
    }

    fun bindParams(shaderProgram: Int) {
        for (key in map.keys) {
            updateParams(key, shaderProgram)
        }
    }

    fun newBuilder() = Builder(this)

    fun pushValuesToProgram() {
        for (key in map.keys) {
            val param = map[key]
            if (param == null || param.location == UNKNOWN_LOCATION || param.value == null) {
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
                Param.ValueType.SAMPLER_2D -> {
                    GLES30.glUniform1i(param.location, (param.addtional as Int).convertTextureSlotToIndex())
                    GLES30.glActiveTexture(param.addtional as Int)
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, param.value as Int)
                }
                Param.ValueType.SAMPLER_OES -> {
                    // update texture (as far as we stored SurfaceTexture to value in updateParams() method
                    (param.value as? SurfaceTexture)?.updateTexImage()
                }
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

        /**
         * Pass color form android resources to shader as vec4
         */
        fun addColor(paramName: String, @ColorRes colorRes: Int, resources: Resources): Builder {
            val color = ResourcesCompat.getColor(resources, colorRes, null)
            addColor(paramName, color)
            return this
        }

        /**
         * Pass color integer as color to shader as vec4
         */
        fun addColor(paramName: String, @ColorInt color: Int): Builder {
            addVec4f(
                paramName, floatArrayOf(
                    Color.red(color) / 255f,
                    Color.green(color) / 255f,
                    Color.blue(color) / 255f,
                    Color.alpha(color) / 255f
                )
            )
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

        /**
         * Set 2d texture
         * set texture to GL_TEXTURE0 slot by default
         */
        fun addTexture2D(
            paramName: String,
            bitmap: Bitmap? = null,
            textureSlot: Int = GLES30.GL_TEXTURE0
        ): Builder {
            val param = Param(
                valeType = Param.ValueType.SAMPLER_2D,
                value = bitmap,
                addtional = textureSlot
            )
            result.map[paramName] = param
            return this
        }

        /**
         * Set 2d texture
         * set texture to GL_TEXTURE0 slot by default
         */
        fun addTexture2D(
            paramName: String,
            @DrawableRes textureResourceId: Int,
            resources: Resources,
            textureSlot: Int = GLES30.GL_TEXTURE0
        ): Builder {
            val param = Param(
                valeType = Param.ValueType.SAMPLER_2D,
                value = resources.loadBitmapForTexture(textureResourceId),
                addtional = textureSlot
            )
            result.map[paramName] = param
            return this
        }

        /**
         * Use external texture. Usually for video stream
         * Currently we support only one instance of such texture per shader
         *
         * more info: https://www.khronos.org/registry/OpenGL/extensions/OES/OES_EGL_image_external.txt
         */
        fun addTextureOES(paramName: String): Builder {
            val param = Param(
                valeType = Param.ValueType.SAMPLER_OES
            )
            result.map[paramName] = param
            return this
        }

        fun build() = result
    }

    private fun Int.convertTextureSlotToIndex(): Int =
        when (this) {
            GLES30.GL_TEXTURE0 -> 0
            GLES30.GL_TEXTURE1 -> 1
            GLES30.GL_TEXTURE2 -> 2
            GLES30.GL_TEXTURE3 -> 3
            GLES30.GL_TEXTURE4 -> 4
            GLES30.GL_TEXTURE5 -> 5
            GLES30.GL_TEXTURE6 -> 6
            GLES30.GL_TEXTURE7 -> 7
            GLES30.GL_TEXTURE8 -> 8
            GLES30.GL_TEXTURE9 -> 9
            GLES30.GL_TEXTURE10 -> 10
            GLES30.GL_TEXTURE11 -> 11
            GLES30.GL_TEXTURE12 -> 12
            GLES30.GL_TEXTURE13 -> 13
            GLES30.GL_TEXTURE14 -> 14
            GLES30.GL_TEXTURE15 -> 15
            GLES30.GL_TEXTURE16 -> 16
            GLES30.GL_TEXTURE17 -> 17
            GLES30.GL_TEXTURE18 -> 18
            GLES30.GL_TEXTURE19 -> 19
            GLES30.GL_TEXTURE20 -> 20
            GLES30.GL_TEXTURE21 -> 21
            GLES30.GL_TEXTURE22 -> 22
            GLES30.GL_TEXTURE23 -> 23
            GLES30.GL_TEXTURE24 -> 24
            GLES30.GL_TEXTURE25 -> 25
            GLES30.GL_TEXTURE26 -> 26
            GLES30.GL_TEXTURE27 -> 27
            GLES30.GL_TEXTURE28 -> 28
            GLES30.GL_TEXTURE29 -> 29
            GLES30.GL_TEXTURE30 -> 30
            GLES30.GL_TEXTURE31 -> 31
            else -> 0
        }
}
