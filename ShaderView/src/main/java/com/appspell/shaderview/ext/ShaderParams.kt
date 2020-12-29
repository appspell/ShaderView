package com.appspell.shaderview.ext

import android.graphics.SurfaceTexture
import android.view.Surface
import com.appspell.shaderview.gl.ShaderParams

fun ShaderParams.getTexture2dOESSurfaceTexture(parameterName: String) =
    this.getParamValue(parameterName) as? SurfaceTexture

fun ShaderParams.getTexture2dOESSurface(parameterName: String) = this.getParamAdditionalField(parameterName) as? Surface