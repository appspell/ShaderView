package com.appspell.shaderview.ext

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES32
import android.opengl.GLUtils
import androidx.annotation.DrawableRes
import java.nio.IntBuffer

/**
 * Create External texture
 * can be useful for Video Players
 * for Fragment shader you have to use two things:
 * 1. #extension GL_OES_EGL_image_external_essl3 : require
 * 2. uniform samplerExternalOES uVideoTexture;
 *
 * shader example: video_shader.fsh in demo project https://github.com/appspell/ShaderView/tree/main/demo/src/main/res/raw
 */
fun createExternalTexture(): Int {
    val textureIds = IntArray(1)
    GLES32.glGenTextures(1, IntBuffer.wrap(textureIds))
    if (textureIds[0] == 0) {
        throw java.lang.RuntimeException("It's not possible to generate ID for texture")
    }
    GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIds[0])
    GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
    GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)

    GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
    GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)
    return textureIds[0]
}

fun Resources.loadBitmapForTexture(@DrawableRes drawableRes: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inScaled = false // true by default. false if we need scalable image

    // load from resources
    return BitmapFactory.decodeResource(this, drawableRes, options)
}

/**
 * Load texture from Bitmap and write it to the video memory
 * @needToRecycle - do we need to recycle current Bitmap when we write it GPI?
 */
@Throws(RuntimeException::class)
fun Bitmap.toGlTexture(needToRecycle: Boolean = true, textureSlot: Int = GLES32.GL_TEXTURE0): Int {
    // init textures
    val textureIds = IntArray(1)
    GLES32.glGenTextures(1, textureIds, 0) // generate ID for texture
    if (textureIds[0] == 0) {
        throw java.lang.RuntimeException("It's not possible to generate ID for texture")
    }

    GLES32.glActiveTexture(textureSlot) // activate slot #0 for texture
    GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureIds[0]) // bind texture by ID with active slot

    // texture filters
    GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
    GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)

    // write bitmap to GPU
    GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, this, 0)
    // we don't need this bitmap anymore
    if (needToRecycle) {
        this.recycle()
    }

    // unbind texture from slot
    GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0)

    return textureIds[0]
}