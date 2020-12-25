package com.appspell.shaderview.ext

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import androidx.annotation.DrawableRes

/**
 * Load texture from DrawableResId and write it to the video memory
 */
@Throws(RuntimeException::class)
fun Resources.loadTexture(@DrawableRes drawableRes: Int, textureSlot: Int = GLES30.GL_TEXTURE0): Int {
    val options = BitmapFactory.Options()
    options.inScaled = false // true by default. false if we need scalable image

    // load from resources
    val bitmap = BitmapFactory.decodeResource(this, drawableRes, options)

    return bitmap.toGlTexture(true, textureSlot)
}

/**
 * Load texture from Bitmap and write it to the video memory
 * @needToRecycle - do we need to recycle current Bitmap when we write it GPI?
 */
@Throws(RuntimeException::class)
fun Bitmap.toGlTexture(needToRecycle: Boolean = true, textureSlot: Int = GLES30.GL_TEXTURE0): Int {
    // init textures
    val textureIds = IntArray(1)
    GLES30.glGenTextures(1, textureIds, 0) // generate ID for texture
    if (textureIds[0] == 0) {
        throw java.lang.RuntimeException("It's not possible to generate ID for texture")
    }

    GLES30.glActiveTexture(textureSlot) // activate slot #0 for texture
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]) // bind texture by ID with active slot

    // texture filters
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

    // write bitmap to GPU
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this, 0)
    // we don't need this bitmap anymore
    if (needToRecycle) {
        this.recycle()
    }

    // unbind texture from slot
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

    return textureIds[0]
}