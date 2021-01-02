package com.appspell.shaderview.ext

import android.graphics.SurfaceTexture
import android.view.Surface
import com.appspell.shaderview.gl.ShaderParams
import com.appspell.shaderview.gl.params.SamplerOESParam

fun ShaderParams.getTexture2dOESSurfaceTexture(parameterName: String) =
    (this.getParamValue(parameterName) as? SamplerOESParam)?.surfaceTexture

fun ShaderParams.getTexture2dOESSurface(parameterName: String) =
    (this.getParamValue(parameterName) as? SamplerOESParam)?.surface