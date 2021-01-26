package com.appspell.shaderview.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.R
import com.appspell.shaderview.databinding.ActivityUiBinding
import com.appspell.shaderview.gl.ShaderParams


class UiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ui.apply {
            updateContinuously = false
            vertexShaderRawResId = R.raw.quad_tangent_space_vert
            fragmentShaderRawResId = R.raw.gui_element_fair_light
            shaderParams = ShaderParams.Builder()
                .addVec2f("uViewSize", floatArrayOf(0f, 0f))
                .build()
            debugMode = true
            // TODO override onLayout
            viewTreeObserver.addOnGlobalLayoutListener {
                // when view is ready
                shaderParams = shaderParams
                    ?.newBuilder()
                    ?.addVec2f("uViewSize", floatArrayOf(width.toFloat(), height.toFloat()))
                    ?.build()
                requestRender()
            }
        }
    }
}