package com.appspell.shaderview.gl.params

import android.graphics.SurfaceTexture
import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

data class SamplerOESParam(
    val surface: Surface,
    val surfaceTexture: SurfaceTexture,
    val updateSurface: AtomicBoolean = AtomicBoolean(false),
    val lock: ReentrantLock = ReentrantLock()
)