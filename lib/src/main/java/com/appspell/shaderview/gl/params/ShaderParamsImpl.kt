package com.appspell.shaderview.gl.params

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES32
import android.view.Surface
import com.appspell.shaderview.annotations.ShaderExperimentalApi
import com.appspell.shaderview.ext.createExternalTexture
import com.appspell.shaderview.ext.loadBitmapForTexture
import com.appspell.shaderview.ext.toGlTexture
import kotlin.concurrent.withLock

class ShaderParamsImpl : ShaderParams {

    private val map = HashMap<String, Param>()

    override fun updateParam(paramName: String, param: Param) {
        map[paramName] = param
    }

    override fun updateValue(paramName: String, value: Float) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: Int) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: Boolean) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: FloatArray) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: IntArray) {
        map[paramName]?.value = value
    }

    @ShaderExperimentalApi
    override fun updateValue2D(paramName: String, value: Bitmap?, needToRecycleWhenUploaded: Boolean) {
        map[paramName]?.value = (map[paramName]?.value as? TextureParam)?.copy(
            bitmap = value,
            needToRecycleWhenUploaded = needToRecycleWhenUploaded
        )
    }

    @ShaderExperimentalApi
    override fun updateValue2D(paramName: String, res: Int) {
        map[paramName]?.value = (map[paramName]?.value as? TextureParam)?.copy(
            textureResourceId = res,
            needToRecycleWhenUploaded = true
        )
    }

    /**
     * Usually it returns uniform shader ID of particaluar parameter (if initialized)
     */
    override fun getParamShaderLocation(paramName: String): Int? = map[paramName]?.location

    override fun getParamValue(paramName: String): Any? = map[paramName]?.value

    private fun updateUniformLocation(paramName: String, shaderProgram: Int) {
        map[paramName]?.apply {
            location = GLES32.glGetUniformLocation(shaderProgram, paramName)
        }
    }

    override fun release() {
        for (key in map.keys) {
            map[key]?.apply {
                when (valeType) {
                    Param.ValueType.SAMPLER_OES -> {
                        (value as? TextureOESParam)?.apply {
                            lock.withLock {
                                surfaceTexture.release()
                                surface.release()
                            }
                        }
                        value = null
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    override fun bindParams(shaderProgram: Int, resources: Resources?) {
        for (key in map.keys) {
            updateUniformLocation(key, shaderProgram)
            resources?.also { bindTextures(key, resources) }
        }
    }

    override fun newBuilder() = ShaderParamsBuilder(this)

    override fun pushValuesToProgram() {
        for (key in map.keys) {
            val param = map[key]
            if (param == null || param.location == UNKNOWN_LOCATION || param.value == null) {
                continue
            }
            when (param.valeType) {
                Param.ValueType.FLOAT -> GLES32.glUniform1f(param.location, param.value as Float)
                Param.ValueType.INT -> GLES32.glUniform1i(param.location, param.value as Int)
                Param.ValueType.BOOL -> GLES32.glUniform1i(param.location, if (param.value as Boolean) 1 else 0)
                Param.ValueType.FLOAT_VEC2 -> GLES32.glUniform2fv(param.location, 1, (param.value as FloatArray), 0)
                Param.ValueType.FLOAT_VEC3 -> GLES32.glUniform3fv(param.location, 1, (param.value as FloatArray), 0)
                Param.ValueType.FLOAT_VEC4 -> GLES32.glUniform4fv(param.location, 1, (param.value as FloatArray), 0)
                Param.ValueType.INT_VEC2 -> GLES32.glUniform2iv(param.location, 1, (param.value as IntArray), 0)
                Param.ValueType.INT_VEC3 -> GLES32.glUniform3iv(param.location, 1, (param.value as IntArray), 0)
                Param.ValueType.INT_VEC4 -> GLES32.glUniform4iv(param.location, 1, (param.value as IntArray), 0)
                Param.ValueType.MAT3 -> GLES32.glUniformMatrix3fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.MAT4 -> GLES32.glUniformMatrix4fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.MAT3x4 -> GLES32.glUniformMatrix3x4fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.SAMPLER_2D -> {
                    (param.value as? TextureParam)?.apply {
                        GLES32.glUniform1i(param.location, textureSlot.convertTextureSlotToIndex())
                        GLES32.glActiveTexture(textureSlot)
                        textureId?.also { GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, it) }
                    }
                }
                Param.ValueType.SAMPLER_OES -> {
                    // update texture (as far as we stored SurfaceTexture to value in updateParams() method)
                    (param.value as? TextureOESParam)?.apply {
                        lock.withLock {
                            if (updateSurface.get()) {
                                surfaceTexture.updateTexImage()
                                updateSurface.set(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindTextures(paramName: String, resources: Resources) {
        map[paramName]?.apply {
            when (valeType) {
                // We have a different flow for Textures.
                // At first, we get a bitmap from params and when OpenGL context is ready we convert it to Texture
                Param.ValueType.SAMPLER_2D -> {
                    // if it is a Bitmap let's upload it to the GPU
                    (value as? TextureParam)?.let { textureParam ->
                        // create Bitmap
                        val bitmap = textureParam.bitmap ?: textureParam.textureResourceId?.let {
                            resources.loadBitmapForTexture(it)
                        }

                        // upload bitmap to GPU
                        bitmap?.toGlTexture(
                            needToRecycle = textureParam.needToRecycleWhenUploaded,
                            textureSlot = textureParam.textureSlot
                        )
                    }.also { textureId ->
                        value = (value as? TextureParam)?.copy(
                            textureId = textureId
                        ) ?: value
                    }
                }
                // create Surface for External Texture
                Param.ValueType.SAMPLER_OES -> {
                    if (value == null) {
                        // if it's not initialized
                        location = createExternalTexture()
                        val surfaceTexture = SurfaceTexture(location)
                        value = TextureOESParam(
                            surfaceTexture = surfaceTexture,
                            surface = Surface(surfaceTexture)
                        ).apply {
                            surfaceTexture.setOnFrameAvailableListener {
                                lock.withLock {
                                    updateSurface.set(true)
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Do Nothing for the other types
                }
            }
        }
    }

    private fun Int.convertTextureSlotToIndex(): Int =
        when (this) {
            GLES32.GL_TEXTURE0 -> 0
            GLES32.GL_TEXTURE1 -> 1
            GLES32.GL_TEXTURE2 -> 2
            GLES32.GL_TEXTURE3 -> 3
            GLES32.GL_TEXTURE4 -> 4
            GLES32.GL_TEXTURE5 -> 5
            GLES32.GL_TEXTURE6 -> 6
            GLES32.GL_TEXTURE7 -> 7
            GLES32.GL_TEXTURE8 -> 8
            GLES32.GL_TEXTURE9 -> 9
            GLES32.GL_TEXTURE10 -> 10
            GLES32.GL_TEXTURE11 -> 11
            GLES32.GL_TEXTURE12 -> 12
            GLES32.GL_TEXTURE13 -> 13
            GLES32.GL_TEXTURE14 -> 14
            GLES32.GL_TEXTURE15 -> 15
            GLES32.GL_TEXTURE16 -> 16
            GLES32.GL_TEXTURE17 -> 17
            GLES32.GL_TEXTURE18 -> 18
            GLES32.GL_TEXTURE19 -> 19
            GLES32.GL_TEXTURE20 -> 20
            GLES32.GL_TEXTURE21 -> 21
            GLES32.GL_TEXTURE22 -> 22
            GLES32.GL_TEXTURE23 -> 23
            GLES32.GL_TEXTURE24 -> 24
            GLES32.GL_TEXTURE25 -> 25
            GLES32.GL_TEXTURE26 -> 26
            GLES32.GL_TEXTURE27 -> 27
            GLES32.GL_TEXTURE28 -> 28
            GLES32.GL_TEXTURE29 -> 29
            GLES32.GL_TEXTURE30 -> 30
            GLES32.GL_TEXTURE31 -> 31
            else -> 0
        }
}